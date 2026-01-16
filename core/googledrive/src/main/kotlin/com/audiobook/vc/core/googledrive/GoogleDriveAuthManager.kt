@file:Suppress("DEPRECATION")
package com.audiobook.vc.core.googledrive

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import com.audiobook.vc.core.logging.api.Logger

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "google_drive_auth")

interface GoogleDriveAuthManager {
  val isSignedIn: Flow<Boolean>
  val accountEmail: Flow<String?>
  fun getSignInIntent(): Intent
  suspend fun handleSignInResult(data: Intent?): Result<Unit>
  suspend fun signOut()
  suspend fun getAccessToken(): String?
  suspend fun silentSignIn(): Boolean
  /** 
   * Get access token, refreshing if needed. Uses caching to avoid repeated fetches.
   * Returns null if not signed in or token refresh fails.
   */
  suspend fun getAccessTokenCached(): String?
  /**
   * Force refresh the access token, bypassing cache.
   * Useful when receiving HTTP 401 from API.
   */
  suspend fun refreshToken(): String?
  /**
   * Check if the cached token is still valid (not expired).
   */
  suspend fun isTokenValid(): Boolean
}

@Inject
@ContributesBinding(AppScope::class)
class GoogleDriveAuthManagerImpl(
  private val context: Context,
) : GoogleDriveAuthManager {

  companion object {
    // Token expires ~1 hour, refresh 5 minutes before expiry
    private const val TOKEN_REFRESH_BUFFER_MS = 5 * 60 * 1000L
  }

  private val signInClient: GoogleSignInClient by lazy {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken("825586134430-fl6hpnk8vbgehbfqkp4ac0sbeul3s2i3.apps.googleusercontent.com")
      .requestEmail()
      .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
      .build()
    GoogleSignIn.getClient(context, gso)
  }

  private val emailKey = stringPreferencesKey("account_email")
  private val tokenKey = stringPreferencesKey("access_token")
  private val tokenExpiryKey = longPreferencesKey("token_expiry")

  // In-memory cache for quick access
  @Volatile
  private var cachedToken: String? = null
  @Volatile
  private var cachedTokenExpiry: Long = 0L
  private val tokenMutex = Mutex()

  override val isSignedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
    prefs[emailKey] != null
  }

  override val accountEmail: Flow<String?> = context.authDataStore.data.map { prefs ->
    prefs[emailKey]
  }

  override fun getSignInIntent(): Intent {
    return signInClient.signInIntent
  }

  override suspend fun handleSignInResult(data: Intent?): Result<Unit> {
    return try {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      val account = task.await()
      saveAccount(account)
      // Pre-fetch token after successful sign-in
      @Suppress("UNUSED_VARIABLE")
      val ignored = refreshToken()
      Result.success(Unit)
    } catch (e: Exception) {
      Logger.e(e, "Failed to sign in to Google Drive")
      Result.failure(e)
    }
  }

  override suspend fun silentSignIn(): Boolean {
    return try {
      val account = signInClient.silentSignIn().await()
      saveAccount(account)
      Logger.d("Silent sign-in successful for ${account.email}")
      // Pre-fetch token after successful silent sign-in
      @Suppress("UNUSED_VARIABLE")
      val ignored = refreshToken()
      true
    } catch (e: Exception) {
      Logger.d("Silent sign-in failed: ${e.message}")
      false
    }
  }

  private suspend fun saveAccount(account: GoogleSignInAccount) {
    context.authDataStore.edit { prefs ->
      prefs[emailKey] = account.email ?: ""
    }
  }

  override suspend fun signOut() {
    signInClient.signOut().await()
    tokenMutex.withLock {
      cachedToken = null
      cachedTokenExpiry = 0L
    }
    context.authDataStore.edit { prefs ->
      prefs.remove(emailKey)
      prefs.remove(tokenKey)
      prefs.remove(tokenExpiryKey)
    }
    Logger.d("Signed out from Google Drive")
  }

  override suspend fun getAccessToken(): String? {
    return getAccessTokenCached()
  }

  override suspend fun getAccessTokenCached(): String? {
    // Check in-memory cache first
    tokenMutex.withLock {
      val token = cachedToken
      val expiry = cachedTokenExpiry
      if (token != null && System.currentTimeMillis() < expiry - TOKEN_REFRESH_BUFFER_MS) {
        return token
      }
    }

    // Check persisted cache
    val prefs = context.authDataStore.data.first()
    val storedToken = prefs[tokenKey]
    val storedExpiry = prefs[tokenExpiryKey] ?: 0L
    
    if (storedToken != null && System.currentTimeMillis() < storedExpiry - TOKEN_REFRESH_BUFFER_MS) {
      tokenMutex.withLock {
        cachedToken = storedToken
        cachedTokenExpiry = storedExpiry
      }
      return storedToken
    }

    // Token expired or missing, refresh it
    return refreshToken()
  }

  override suspend fun refreshToken(): String? {
    val account = GoogleSignIn.getLastSignedInAccount(context) ?: run {
      Logger.w("Cannot refresh token: not signed in")
      return null
    }
    
    return try {
      val credential = com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
        .usingOAuth2(context, listOf(DriveScopes.DRIVE_READONLY))
      credential.selectedAccount = account.account
      
      val token = credential.token
      // Google OAuth tokens typically expire in 1 hour
      val expiryTime = System.currentTimeMillis() + 60 * 60 * 1000L
      
      // Cache the new token
      tokenMutex.withLock {
        cachedToken = token
        cachedTokenExpiry = expiryTime
      }
      
      // Persist to DataStore
      context.authDataStore.edit { prefs ->
        prefs[tokenKey] = token
        prefs[tokenExpiryKey] = expiryTime
      }
      
      Logger.d("Token refreshed, expires at $expiryTime")
      token
    } catch (e: Exception) {
      Logger.e(e, "Failed to refresh access token")
      null
    }
  }

  override suspend fun isTokenValid(): Boolean {
    val prefs = context.authDataStore.data.first()
    val storedExpiry = prefs[tokenExpiryKey] ?: 0L
    return System.currentTimeMillis() < storedExpiry - TOKEN_REFRESH_BUFFER_MS
  }
}

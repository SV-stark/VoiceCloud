package voice.core.googledrive

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
import kotlinx.coroutines.tasks.await
import voice.core.logging.api.Logger

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "google_drive_auth")

interface GoogleDriveAuthManager {
  val isSignedIn: Flow<Boolean>
  val accountEmail: Flow<String?>
  fun getSignInIntent(): Intent
  suspend fun handleSignInResult(data: Intent?): Boolean
  suspend fun signOut()
  suspend fun getAccessToken(): String?
  suspend fun silentSignIn(): Boolean
}

@Inject
@ContributesBinding(AppScope::class)
class GoogleDriveAuthManagerImpl(
  private val context: Context,
) : GoogleDriveAuthManager {

  private val signInClient: GoogleSignInClient by lazy {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
      .build()
    GoogleSignIn.getClient(context, gso)
  }

  private val emailKey = stringPreferencesKey("account_email")
  private val tokenKey = stringPreferencesKey("access_token")

  override val isSignedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
    prefs[emailKey] != null
  }

  override val accountEmail: Flow<String?> = context.authDataStore.data.map { prefs ->
    prefs[emailKey]
  }

  override fun getSignInIntent(): Intent {
    return signInClient.signInIntent
  }

  override suspend fun handleSignInResult(data: Intent?): Boolean {
    return try {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      val account = task.await()
      saveAccount(account)
      true
    } catch (e: Exception) {
      Logger.e(e, "Failed to sign in to Google Drive")
      false
    }
  }

  override suspend fun silentSignIn(): Boolean {
    return try {
      val account = signInClient.silentSignIn().await()
      saveAccount(account)
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
    context.authDataStore.edit { prefs ->
      prefs.remove(emailKey)
      prefs.remove(tokenKey)
    }
  }

  override suspend fun getAccessToken(): String? {
    val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
    return try {
      // Get fresh token using GoogleAccountCredential
      val credential = com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
        .usingOAuth2(context, listOf(DriveScopes.DRIVE_READONLY))
      credential.selectedAccount = account.account
      credential.token
    } catch (e: Exception) {
      Logger.e(e, "Failed to get access token")
      null
    }
  }
}

package com.audiobook.vc.core.googledrive

import android.content.Context
import com.audiobook.vc.core.logging.api.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Initializer for Google Drive silent sign-in.
 * 
 * Attempts silent sign-in on app start if:
 * - User was previously signed in
 * - Token needs refreshing
 * 
 * This ensures seamless reconnection without user intervention,
 * following AnExplorer's pattern of automatic connection restoration.
 * 
 * Usage: Call [initialize] from your Application.onCreate() or similar entry point,
 * passing the [GoogleDriveAuthManager] instance from your DI graph.
 */
object GoogleDriveInitializer {

  private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private var initialized = false

  /**
   * Initialize Google Drive with silent sign-in attempt.
   * Safe to call multiple times - will only run once.
   */
  fun initialize(authManager: GoogleDriveAuthManager) {
    if (initialized) return
    initialized = true
    
    Logger.d("GoogleDriveInitializer: Starting")
    
    initScope.launch {
      try {
        // Check if user was previously signed in
        val wasSignedIn = authManager.isSignedIn.first()
        
        if (wasSignedIn) {
          Logger.d("GoogleDriveInitializer: User was previously signed in, checking token")
          
          // Check if token is still valid
          if (!authManager.isTokenValid()) {
            Logger.d("GoogleDriveInitializer: Token expired or missing, attempting silent sign-in")
            val success = authManager.silentSignIn()
            if (success) {
              Logger.d("GoogleDriveInitializer: Silent sign-in successful")
            } else {
              Logger.w("GoogleDriveInitializer: Silent sign-in failed, interactive sign-in will be required")
            }
          } else {
            Logger.d("GoogleDriveInitializer: Token still valid")
          }
        } else {
          Logger.d("GoogleDriveInitializer: User not previously signed in, skipping")
        }
      } catch (e: Exception) {
        Logger.e(e, "GoogleDriveInitializer: Error during initialization")
      }
    }
  }
}


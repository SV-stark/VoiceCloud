package com.audiobook.vc.core.googledrive

import android.content.Context
import androidx.startup.Initializer
import com.audiobook.vc.core.logging.api.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * AndroidX App Startup Initializer for Google Drive.
 * 
 * Attempts silent sign-in on app start if:
 * - User was previously signed in
 * - Token needs refreshing
 * 
 * This ensures seamless reconnection without user intervention,
 * following AnExplorer's pattern of automatic connection restoration.
 */
class GoogleDriveInitializer : Initializer<Unit> {

  private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun create(context: Context) {
    Logger.d("GoogleDriveInitializer: Starting")
    
    // Get the auth manager from the application graph
    // This assumes the app has a way to access the DI graph from context
    val authManager = try {
      getAuthManager(context)
    } catch (e: Exception) {
      Logger.w("GoogleDriveInitializer: Could not get auth manager, skipping")
      return
    }
    
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

  override fun dependencies(): List<Class<out Initializer<*>>> {
    // No dependencies required
    return emptyList()
  }

  /**
   * Gets the GoogleDriveAuthManager from the application.
   * This uses reflection to access the app's DI graph.
   */
  private fun getAuthManager(context: Context): GoogleDriveAuthManager {
    // Try to get from application class if it exposes the graph
    val app = context.applicationContext
    
    // Use reflection to find the auth manager
    // This is a simplified approach - in production, you'd use proper DI integration
    val graphInterface = Class.forName("com.audiobook.vc.core.googledrive.GoogleDriveGraph")
    
    for (method in app.javaClass.methods) {
      val returnType = method.returnType
      if (graphInterface.isAssignableFrom(returnType)) {
        val graph = method.invoke(app)
        val authManagerGetter = graph.javaClass.getMethod("getGoogleDriveAuthManager")
        return authManagerGetter.invoke(graph) as GoogleDriveAuthManager
      }
    }
    
    throw IllegalStateException("Could not find GoogleDriveAuthManager in application graph")
  }
}

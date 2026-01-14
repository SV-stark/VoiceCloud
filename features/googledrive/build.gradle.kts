plugins {
  id("voice.android.library")
  id("voice.compose")
  alias(libs.plugins.metro)
}

android {
  namespace = "com.audiobook.vc.features.googledrive"
}

dependencies {
  implementation(projects.core.ui)
  implementation(projects.core.common)
  implementation(projects.core.googledrive)
  implementation(projects.navigation)
  implementation(projects.core.strings)
  implementation(projects.core.documentfile)
  
  implementation(libs.androidx.lifecycle.viewModelCompose)
  implementation(libs.androidx.activity.compose)
}

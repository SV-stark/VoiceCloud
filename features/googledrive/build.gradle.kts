plugins {
  id("voice.library")
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
  implementation(projects.core.data.api)
  implementation(projects.features.folderPicker)
  
  implementation(libs.lifecycle.viewmodel.compose)
  implementation(libs.compose.activity)
}

plugins {
  id("voice.library")
  alias(libs.plugins.metro)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.audiobook.vc.core.googledrive"
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.documentfile)
  implementation(projects.core.data.api)
  implementation(projects.core.logging.api)

  implementation(libs.metro.runtime)
  implementation(libs.androidxCore)
  implementation(libs.coroutines.core)
  implementation(libs.coroutines.android)
  implementation(libs.serialization.json)
  implementation(libs.datastore)

  // Google Drive API
  implementation(libs.google.api.client.android)
  implementation(libs.google.api.services.drive)
  implementation(libs.play.services.auth)
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")

  // ExoPlayer for DataSource
  implementation(libs.media3.exoplayer)

  testImplementation(libs.junit)
  testImplementation(libs.koTest.assert)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.mockk)
}

package com.audiobook.vc.app

import dev.zacsweers.metro.createGraphFactory
import com.audiobook.vc.app.di.App
import com.audiobook.vc.app.di.AppGraph

class TestApp : App() {

  override fun createGraph(): AppGraph {
    return createGraphFactory<TestGraph.Factory>()
      .create(this)
  }
}

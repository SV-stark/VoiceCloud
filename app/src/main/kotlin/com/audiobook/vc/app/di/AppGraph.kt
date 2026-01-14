package com.audiobook.vc.app.di

import com.audiobook.vc.app.features.widget.BaseWidgetProvider
import com.audiobook.vc.features.widget.WidgetGraph

interface AppGraph : WidgetGraph {

  fun inject(target: App)
  override fun inject(target: BaseWidgetProvider)
}

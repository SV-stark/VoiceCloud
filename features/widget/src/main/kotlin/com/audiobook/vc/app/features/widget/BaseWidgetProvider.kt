package com.audiobook.vc.app.features.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.zacsweers.metro.Inject
import com.audiobook.vc.core.common.rootGraph
import com.audiobook.vc.features.widget.WidgetGraph
import com.audiobook.vc.features.widget.WidgetUpdater

class BaseWidgetProvider : AppWidgetProvider() {

  @Inject
  lateinit var widgetUpdater: WidgetUpdater

  override fun onReceive(
    context: Context,
    intent: Intent?,
  ) {
    (rootGraph as WidgetGraph).inject(this)
    super.onReceive(context, intent)
  }

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    widgetUpdater.update()
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    widgetUpdater.update()
  }
}

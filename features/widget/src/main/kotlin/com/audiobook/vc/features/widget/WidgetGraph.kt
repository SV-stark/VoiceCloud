package com.audiobook.vc.features.widget

import com.audiobook.vc.app.features.widget.BaseWidgetProvider

interface WidgetGraph {
  fun inject(target: BaseWidgetProvider)
}

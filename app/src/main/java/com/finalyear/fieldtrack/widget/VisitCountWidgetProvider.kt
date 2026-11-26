package com.finalyear.fieldtrack.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.finalyear.fieldtrack.R
import com.finalyear.fieldtrack.db.DatabaseHelper

/**
 * Demonstrates "Creating Widgets". Shows the total number of logged visits
 * directly on the home screen.
 */
class VisitCountWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val count = DatabaseHelper.getInstance(context).getVisitCount()
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_visit_count)
            views.setTextViewText(R.id.widgetCountText, count.toString())
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}

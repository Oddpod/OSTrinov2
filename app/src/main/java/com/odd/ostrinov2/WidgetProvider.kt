package com.odd.ostrinov2

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.RemoteViews

import com.odd.ostrinov2.tools.DBHandler
import com.odd.ostrinov2.tools.UtilMeths
import com.squareup.picasso.Picasso

import java.io.File
import java.util.Random

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dbHandler = DBHandler(context)

        val ostList = dbHandler.allOsts
        val ostNum = Random().nextInt(ostList.size)
        val ostOfTheDay = ostList[ostNum]

        for (appWidgetId in appWidgetIds) {

            val remoteViews = RemoteViews(context.packageName,
                    R.layout.widget_layout)
            remoteViews.setTextViewText(R.id.tvOstoftheDay, ostOfTheDay.title)
            val tnFile = UtilMeths.getThumbnailLocal(ostOfTheDay.url)
            Picasso.with(context).load(tnFile).resize(500, 200).onlyScaleDown()
                    .into(remoteViews, R.id.widgetThumbnail, appWidgetIds)

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(context.getString(R.string.label_ost_of_the_day), ostNum)
            val pendingIntent = PendingIntent.getActivity(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent)
            remoteViews.setOnClickPendingIntent(R.id.widgetThumbnail, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

        }
    }

    companion object {

        val ACTION_AUTO_UPDATE = "AUTO_UPDATE"
    }
}

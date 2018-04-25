package com.odd.ostrinov2.tools

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.R

class ProgressNotification(private val mTitle: String,
                           private val mContext: Context, private val progressText: String) {

    private var mBuilder: NotificationCompat.Builder
    private val mId: Int = 0
    private var mNotifyManager: NotificationManager? = null

    init {
        mNotifyManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        mBuilder = NotificationCompat.Builder(mContext)
        setStartedNotification()
    }

    fun setStartedNotification() {
        mBuilder.setSmallIcon(R.drawable.ic_stat_name).setContentTitle(mTitle)
                .setContentText("Started")

        val resultIntent = Intent(mContext, MainActivity::class.java)

        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(mContext)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)

        mNotifyManager!!.notify(mId, mBuilder.build())
    }

    fun updateProgress(progress: Int?, totalItems: Int) {
        if (progress == 1)
            setProgressNotification()
        updateProgressNotification(progress!!, totalItems)
    }

    private fun setProgressNotification() {
        mBuilder.setContentTitle(mTitle).setContentText(progressText)
        mNotifyManager!!.notify(mId, mBuilder.build())
    }

    private fun updateProgressNotification(incr: Int, totalItems: Int) {
        mBuilder.setProgress(totalItems, incr, false)
        mNotifyManager!!.notify(mId, mBuilder.build())
    }

    fun setCompletedNotification() {
        mBuilder.setSmallIcon(R.drawable.ic_stat_name).setContentTitle(mTitle)
                .setContentText("Completed")

        val resultIntent = Intent(mContext, MainActivity::class.java)

        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(mContext)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)

        mNotifyManager!!.notify(mId, mBuilder.build())
    }
}
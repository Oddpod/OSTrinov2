package com.odd.ostrinov2.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.RemoteViews
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.tools.UtilMeths
import com.squareup.picasso.Picasso

class PlayerNotificationService(private val service: YTplayerService) {
    private var views: RemoteViews = RemoteViews(service.packageName,
            R.layout.status_bar)
    private var bigViews: RemoteViews
    private val packageName = service.packageName
    private var status: NotificationCompat.Builder
    private var mNotifyMgr: NotificationManager
    private val channedId = "PlayerNotService"

    init{
        // Using RemoteViews to bind custom layouts into Notification
        bigViews = RemoteViews(packageName,
                R.layout.status_bar_expanded)

        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE)
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE)

        val notificationIntent = Intent(service, MainActivity::class.java)
        notificationIntent.action = Constants.NOT_OPEN_ACTIVITY_ACTION
        val pendingIntent = PendingIntent.getActivity(service, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val previousIntent = Intent(service, YTplayerService::class.java)
        previousIntent.action = Constants.PREV_ACTION
        val ppreviousIntent = PendingIntent.getService(service, 0,
                previousIntent, 0)

        val playIntent = Intent(service, YTplayerService::class.java)
        playIntent.action = Constants.PLAY_ACTION
        val pplayIntent = PendingIntent.getService(service, 0,
                playIntent, 0)

        val nextIntent = Intent(service, YTplayerService::class.java)
        nextIntent.action = Constants.NEXT_ACTION
        val pnextIntent = PendingIntent.getService(service, 0,
                nextIntent, 0)

        val expandIntent = Intent(service, YTplayerService::class.java)
        expandIntent.action = Constants.EXPANDMINIMIZE_PLAYER
        val pExpandIntent = PendingIntent.getService(service, 0, expandIntent, 0)
        bigViews.setOnClickPendingIntent(R.id.status_bar_maximize, pExpandIntent)

        val closeIntent = Intent(service, YTplayerService::class.java)
        closeIntent.action = Constants.STOPFOREGROUND_ACTION
        val pcloseIntent = PendingIntent.getService(service, 0,
                closeIntent, 0)

        views.setOnClickPendingIntent(R.id.status_bar_album_art, pendingIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_album_art, pendingIntent)

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)

        views.setImageViewResource(R.id.status_bar_play,
                R.drawable.ic_pause_black_24dp)
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.drawable.ic_pause_black_24dp)

        views.setTextViewText(R.id.status_bar_track_name, "Song Title")
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title")

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name")
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name")

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name")

        mNotifyMgr = service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        status = NotificationCompat.Builder(service, channedId)
                .setCustomContentView(views)
                .setCustomBigContentView(bigViews)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
        mNotifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
    }

    fun updateNotInfo(ost: Ost) {

        loadThumbnail(ost.videoId)

        views.setTextViewText(R.id.status_bar_track_name, ost.title)
        bigViews.setTextViewText(R.id.status_bar_track_name, ost.title)

        views.setTextViewText(R.id.status_bar_artist_name, ost.show)
        bigViews.setTextViewText(R.id.status_bar_artist_name, ost.show)

        bigViews.setTextViewText(R.id.status_bar_album_name, ost.tags)

        status.setCustomContentView(views)
        status.setCustomBigContentView(bigViews)
        mNotifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
    }

    fun updateNotButtons(playing: Boolean){
        if(playing){
            Picasso.with(service).load(R.drawable.ic_pause_black_24dp).into(bigViews,
                    R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
            Picasso.with(service).load(R.drawable.ic_pause_black_24dp).into(views,
                    R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        } else{
            Picasso.with(service).load(R.drawable.ic_play_arrow_black_24dp).into(bigViews,
                    R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
            Picasso.with(service).load(R.drawable.ic_play_arrow_black_24dp).into(views,
                    R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        }
    }

    fun stopNotService(){
        mNotifyMgr.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE)
    }

    private fun loadThumbnail(videoId: String) {
        val tnFile = UtilMeths.getThumbnailLocal(videoId, service)

        //Loads thumbnail from url if not found in local storage
        if (!tnFile.exists()) {
            val tnUrl = UtilMeths.getThumbnailUrl(videoId)
            Picasso.with(service).load(tnUrl)
                    .into(bigViews, R.id.status_bar_album_art,
                            Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                            status.build())
        } else {
            Picasso.with(service).load(tnFile)
                    .into(bigViews, R.id.status_bar_album_art,
                            Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                            status.build())
        }
    }
}

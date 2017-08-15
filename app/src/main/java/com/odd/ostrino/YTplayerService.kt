package com.odd.ostrino

import android.app.*
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.view.*
import android.widget.*
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.odd.ostrino.Listeners.PlayerListener
import com.squareup.picasso.Picasso
import java.io.File
import android.app.KeyguardManager
import android.content.IntentFilter

class YTplayerService : Service(), YouTubePlayer.OnInitializedListener,
        YouTubePlayer.PlayerStateChangeListener,
        YouTubePlayer.PlaybackEventListener,
        SeekBar.OnSeekBarChangeListener {

    private val binder = LocalBinder()
    lateinit var queueHandler: QueueHandler
    lateinit var wm: WindowManager
    lateinit private var ll: LinearLayout
    lateinit private var floatingPlayer: FrameLayout
    private var floatingPlayerInitialized: Boolean = false
    var playing: Boolean = false
    var repeat: Boolean = false
    private var stoppedTime: Int = 0
    private var outsideActivity: Boolean = false
    lateinit private var mainActivity: MainActivity
    lateinit var views: RemoteViews
    lateinit var bigViews: RemoteViews
    lateinit var yTPlayerFrag: YouTubePlayerSupportFragment
    private var userPaused : Boolean = false
    override fun onCreate() {
        super.onCreate()
        registerBroadcastReceiver()
    }

    inner class LocalBinder : android.os.Binder() {
        val service: YTplayerService
            get() = this@YTplayerService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    private val LOG_TAG = "NotificationService"
    lateinit var yPlayer: YouTubePlayer

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        if (!wasRestored) {
            yPlayer = player!!
            yPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
            yPlayer.setShowFullscreenButton(false)
            /*val list = object : ArrayList<String>() {
            init {
                add("pnB_beLVOh8")
                add("ZKWQDhcqbRk")
                add("4rxzGfRHwiE")
            }
            }*/
            yPlayer.loadVideo(queueHandler.currentlyPlaying)
            yPlayer.setPlayerStateChangeListener(this)
            yPlayer.setPlaybackEventListener(this)
            mainActivity.seekBar.setOnSeekBarChangeListener(this)
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        println("oops")
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null || intent.action == null)
            return

        val action = intent.action

        if (action.equals(Constants.PLAY_ACTION, ignoreCase = true)) {
            pausePlay()

        } else if (action.equals("", ignoreCase = true)) {
            TODO()
        } else if (action.equals(Constants.PREV_ACTION, ignoreCase = true)) {
            playerPrevious()
            /*
            if (yPlayer.hasPrevious()) {
                yPlayer.previous()
                yPlayer.cueVideo("VneKjsUR1oM")
            }*/
        } else if (action.equals(Constants.NEXT_ACTION, ignoreCase = true)) {
            playerNext()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == Constants.STARTFOREGROUND_ACTION) {
            //showNotification()
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
        } else if (intent.action == Constants.PREV_ACTION) {
            Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show()
            handleIntent(intent)
            Log.i(LOG_TAG, "Clicked Previous")
        } else if (intent.action == Constants.PLAY_ACTION) {
            handleIntent(intent)
            Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show()
            Log.i(LOG_TAG, "Clicked Play")
        } else if (intent.action == Constants.NEXT_ACTION) {
            //Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show()
            handleIntent(intent)
            Log.i(LOG_TAG, "Clicked Next")
        } else if (intent.action == Constants.STOPFOREGROUND_ACTION) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent")
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
            ll.removeView(floatingPlayer)
            wm.removeView(ll)
            yPlayer.release()
            mainActivity.doUnbindService()
            mainActivity.youtubePlayerStopped()
            mNotifyMgr.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE)
            //stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    lateinit var status: NotificationCompat.Builder
    lateinit var mNotifyMgr: NotificationManager

    fun showNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        views = RemoteViews(packageName,
                R.layout.status_bar)
        bigViews = RemoteViews(packageName,
                R.layout.status_bar_expanded)

        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE)
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE)

        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this))

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val previousIntent = Intent(this, YTplayerService::class.java)
        previousIntent.action = Constants.PREV_ACTION
        val ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0)

        val playIntent = Intent(this, YTplayerService::class.java)
        playIntent.action = Constants.PLAY_ACTION
        val pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0)

        val nextIntent = Intent(this, YTplayerService::class.java)
        nextIntent.action = Constants.NEXT_ACTION
        val pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0)

        val closeIntent = Intent(this, YTplayerService::class.java)
        closeIntent.action = Constants.STOPFOREGROUND_ACTION
        val pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0)

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)

        views.setImageViewResource(R.id.status_bar_play,
                R.drawable.apollo_holo_dark_pause)
        bigViews.setImageViewResource(R.id.status_bar_play,
                R.drawable.apollo_holo_dark_pause)

        views.setTextViewText(R.id.status_bar_track_name, "Song Title")
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title")

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name")
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name")

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name")

        mNotifyMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        status = NotificationCompat.Builder(this)
                .setCustomContentView(views)
                .setCustomBigContentView(bigViews)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
        mNotifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        //startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status)
    }

    fun startQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean,
                   playerListeners: Array<PlayerListener>, youTubePlayerFragment: YouTubePlayerSupportFragment) {
        queueHandler = QueueHandler(ostList, startIndex, shuffle, playerListeners)
        yTPlayerFrag = youTubePlayerFragment
        yTPlayerFrag.retainInstance = true
        yTPlayerFrag.initialize(Constants.API_TOKEN, this)
        updateNotInfo()
    }

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        queueHandler.initiateQueue(ostList, startIndex, shuffle)
        yPlayer.loadVideo(queueHandler.currentlyPlaying)
        updateNotInfo()
    }

    fun resetPlayerListeners(playerListeners: Array<PlayerListener>) {
        queueHandler.playerListeners = playerListeners
        queueHandler.notifyPlayerListeners(false)
    }


    fun refresh() {
        yTPlayerFrag.onResume()
        outsideActivity = true
        if(userPaused){
            yPlayer.cueVideo(queueHandler.currentlyPlaying, stoppedTime)
        } else{
            yPlayer.loadVideo(queueHandler.currentlyPlaying, stoppedTime)
        }
    }

    fun updateNotInfo() {
        val ost: Ost = queueHandler.getCurrPlayingOst()
        val tnFile = File(Environment.getExternalStorageDirectory().toString()
                + "/OSTthumbnails/" + UtilMeths.urlToId(ost.url) + ".jpg")
        Picasso.with(this).load(tnFile).into(bigViews, R.id.status_bar_album_art,
                Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        views.setTextViewText(R.id.status_bar_track_name, ost.title)
        bigViews.setTextViewText(R.id.status_bar_track_name, ost.title)

        views.setTextViewText(R.id.status_bar_artist_name, ost.show)
        bigViews.setTextViewText(R.id.status_bar_artist_name, ost.show)

        bigViews.setTextViewText(R.id.status_bar_album_name, ost.tags)

        status.setCustomContentView(views)
        status.setCustomBigContentView(bigViews)
        mNotifyMgr.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
    }

    fun launchFloater(floatingPlayer: FrameLayout, activity: MainActivity) {
        this.mainActivity = activity
        if (!isSystemAlertPermissionGranted(activity)) {
            requestSystemAlertPermission(activity, 3)
        } else {
            wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            //val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val params = WindowManager.LayoutParams(
                    800,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 1
            params.y = 100

            //WindowManager.LayoutParams.WRAP_CONTENT,
            //      WindowManager.LayoutParams.WRAP_CONTENT,

            if (!floatingPlayerInitialized) {
                floatingPlayer.visibility = View.VISIBLE
                this.floatingPlayer = floatingPlayer
                this.floatingPlayer
                floatingPlayerInitialized = true
            }
            ll = LinearLayout(this) //inflater.inflate(R.layout.youtube_api, null);
            val lp = LinearLayout.LayoutParams(400,
                    800)
            ll.setBackgroundColor(Color.argb(66, 255, 0, 0))
            ll.layoutParams = lp

            val toggle: ToggleButton = ToggleButton(this)
            ll.setOnClickListener{
                    val params = WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_PHONE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT)

                    val playerParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    ll.updateViewLayout(floatingPlayer, playerParams)
                    wm.updateViewLayout(ll, params)

                }
            ll.addView(floatingPlayer)
            wm.addView(ll, params)
            //ll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            ll.setOnTouchListener(object : View.OnTouchListener {

                private val updateParams = params
                internal var X: Int = 0
                internal var Y: Int = 0
                internal var touchedX: Float = 0.toFloat()
                internal var touchedY: Float = 0.toFloat()

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            X = updateParams.x
                            Y = updateParams.y

                            touchedX = event.rawX
                            touchedY = event.rawY
                        }
                        MotionEvent.ACTION_MOVE -> {

                            updateParams.x = (X + (event.rawX - touchedX)).toInt()
                            updateParams.y = (Y + (event.rawY - touchedY)).toInt()

                            wm.updateViewLayout(ll, updateParams)
                        }

                        MotionEvent.ACTION_MASK -> run {

                        }

                        else -> {
                        }
                    }//System.out.println(X + ", " + Y);

                    return false
                }
            })

        }
    }

    fun pausePlay() {
        if (yPlayer.isPlaying) {
            userPaused = true
            yPlayer.pause()
        } else {
            userPaused = false
            yPlayer.play()
        }
    }

    fun playerNext() {
        yPlayer.loadVideo(queueHandler.next()!!)
        updateNotInfo()
    }

    fun playerPrevious() {
        yPlayer.loadVideo(queueHandler.previous()!!)
        updateNotInfo()
    }

    override fun onAdStarted() {
        //
    }

    override fun onLoading() {
    }

    override fun onVideoStarted() {
    }

    override fun onLoaded(p0: String?) {
        mainActivity.seekBar.max = yPlayer.durationMillis
    }

    override fun onVideoEnded() {
        if(repeat){
            yPlayer.seekToMillis(0)
        }else {
            if(queueHandler.hasNext()) {
                val next: String? = queueHandler.next()
                yPlayer.loadVideo(next)
            } else
                yPlayer.pause()
        }
        mainActivity.seekBar.progress = 0
    }

    override fun onError(p0: YouTubePlayer.ErrorReason?) {

    }

    override fun onSeekTo(p0: Int) {
    }

    override fun onBuffering(p0: Boolean) {
    }

    override fun onPlaying() {
        Picasso.with(this).load(R.drawable.apollo_holo_dark_pause).into(bigViews,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        playing = true
        mainActivity.pausePlay()
    }

    override fun onStopped() {
    }

    override fun onPaused() {
        Picasso.with(this).load(R.drawable.apollo_holo_dark_play).into(bigViews,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        Picasso.with(this).load(R.drawable.apollo_holo_dark_play).into(views,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        playing = false
        mainActivity.pausePlay()
        stoppedTime = yPlayer.currentTimeMillis
}

//Seekbar functions
override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    if (fromUser) {
        yPlayer.seekToMillis(progress)
    }
}

override fun onStartTrackingTouch(seekBar: SeekBar?) {
}

override fun onStopTrackingTouch(seekBar: SeekBar?) {
}

fun registerBroadcastReceiver() {
    val theFilter = IntentFilter()
    /** System Defined Broadcast  */
    theFilter.addAction(Intent.ACTION_SCREEN_ON)
    theFilter.addAction(Intent.ACTION_SCREEN_OFF)

    val screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val strAction = intent.action

            val myKM = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (strAction == Intent.ACTION_SCREEN_OFF || strAction == Intent.ACTION_SCREEN_ON) {
                if (myKM.inKeyguardRestrictedInputMode()) {
                    yPlayer.pause()
                    println("Screen off " + "LOCKED")
                } else {
                    println("Screen off " + "UNLOCKED")
                }
            }
        }
    }

    applicationContext.registerReceiver(screenOnOffReceiver, theFilter)
}
}
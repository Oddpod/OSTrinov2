package com.odd.ostrinov2

import android.app.*
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.*
import android.widget.*
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.odd.ostrinov2.Listeners.PlayerListener
import android.app.KeyguardManager
import android.content.IntentFilter
import com.google.android.youtube.player.YouTubePlayer.*


class YTplayerService : Service(), YouTubePlayer.OnInitializedListener,
        YouTubePlayer.PlayerStateChangeListener,
        YouTubePlayer.PlaybackEventListener,
        YouTubePlayer.OnFullscreenListener,
        SeekBar.OnSeekBarChangeListener {

    private val binder = LocalBinder()
    lateinit var queueHandler: QueueHandler
    lateinit var wm: WindowManager
    lateinit private var rl: RelativeLayout
    lateinit private var floatingPlayer: FrameLayout
    private var floatingPlayerInitialized: Boolean = false
    var playing: Boolean = false
    var repeat: Boolean = false
    private var stoppedTime: Int = 0
    private var outsideActivity: Boolean = false
    private var playerExpanded: Boolean = false
    lateinit private var mainActivity: MainActivity
    lateinit var yTPlayerFrag: YouTubePlayerSupportFragment
    private var userPaused: Boolean = false
    private lateinit var playerNotification: PlayerNotificationService

    private val smallWindowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)
    private val largePParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)

    private lateinit var smallPParams : RelativeLayout.LayoutParams
    private val largeWindowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)
    private val fullScreenParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT)

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

    private val NOT_LOG_TAG = "YTplayerService"
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
            yPlayer.setOnFullscreenListener(this)
            yPlayer.addFullscreenControlFlag(FULLSCREEN_FLAG_CUSTOM_LAYOUT)
            mainActivity.seekBar.setOnSeekBarChangeListener(this)

            playerNotification = PlayerNotificationService(this)
            playerNotification.updateNotInfo(queueHandler.getCurrPlayingOst())
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        println("oops")
    }

    override fun onFullscreen(p0: Boolean) {

        fullScreenParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        wm.updateViewLayout(rl, fullScreenParams)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null || intent.action == null)
            return

        val action = intent.action

        if(isScreenLocked()){
            Toast.makeText(applicationContext, "Can't play while on LockScreen! :C", Toast.LENGTH_SHORT).show()
        }
        else if (action.equals(Constants.PLAY_ACTION, ignoreCase = true)) {
            pausePlay()

        } else if (action.equals(Constants.PREV_ACTION, ignoreCase = true)) {
            playerPrevious()
            /*
            if (yPlayer.hasPrevious()) {
                yPlayer.previous()
                yPlayer.cueVideo("VneKjsUR1oM")
            }*/
        } else if (action.equals(Constants.NEXT_ACTION, ignoreCase = true)) {
            playerNext()
        } else if(action.equals(Constants.EXPANDMINIMIZE_PLAYER, ignoreCase = true)){
            expandMinimizePlayer()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == Constants.STARTFOREGROUND_ACTION) {
            //showNotification()
        } else if (intent.action == Constants.PREV_ACTION) {
            handleIntent(intent)
            Log.i(NOT_LOG_TAG, "Clicked Previous")
        } else if (intent.action == Constants.PLAY_ACTION) {
            handleIntent(intent)
            Log.i(NOT_LOG_TAG, "Clicked Play")
        } else if (intent.action == Constants.NEXT_ACTION) {
            handleIntent(intent)
            Log.i(NOT_LOG_TAG, "Clicked Next")
        } else if(intent.action == Constants.EXPANDMINIMIZE_PLAYER) {
            handleIntent(intent)
            Log.i(NOT_LOG_TAG, "Expand")
        }else if (intent.action == Constants.STOPFOREGROUND_ACTION) {
            Log.i(NOT_LOG_TAG, "Received Stop Foreground Intent")
            rl.removeView(floatingPlayer)
            wm.removeView(rl)
            yPlayer.release()
            mainActivity.youtubePlayerStopped()
            mainActivity.doUnbindService()
            playerNotification.stopNotService()
            //stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    fun startQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean,
                   playerListeners: Array<PlayerListener>, youTubePlayerFragment: YouTubePlayerSupportFragment) {
        queueHandler = QueueHandler(ostList.toMutableList(), startIndex, shuffle, playerListeners)
        yTPlayerFrag = youTubePlayerFragment
        yTPlayerFrag.retainInstance = true
        yTPlayerFrag.initialize(Constants.API_TOKEN, this)
    }

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        queueHandler.initiateQueue(ostList, startIndex, shuffle)
        yPlayer.loadVideo(queueHandler.currentlyPlaying)
        playerNotification.updateNotInfo(queueHandler.getCurrPlayingOst())
    }

    fun refresh() {
        yTPlayerFrag.onResume()
        outsideActivity = true
        if (userPaused) {
            yPlayer.cueVideo(queueHandler.currentlyPlaying, stoppedTime)
        } else {
            yPlayer.loadVideo(queueHandler.currentlyPlaying, stoppedTime)
        }
    }

    fun launchFloater(floatingPlayer: FrameLayout, activity: MainActivity) {
        this.mainActivity = activity
        if (!isSystemAlertPermissionGranted(activity)) {
            requestSystemAlertPermission(activity, 3)
        } else {
            wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = smallWindowParams
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 1
            params.y = 100

            if (!floatingPlayerInitialized) {
                floatingPlayer.visibility = View.VISIBLE
                this.floatingPlayer = floatingPlayer
                this.floatingPlayer
                floatingPlayerInitialized = true
                smallPParams = floatingPlayer.layoutParams as RelativeLayout.LayoutParams
            }
            rl = this.mainActivity.layoutInflater.inflate(R.layout.youtube_api, null) as RelativeLayout
            val toggleButton: ToggleButton = rl.findViewById(R.id.toggleButton) as ToggleButton
            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    yPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
                } else {
                    yPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
                }
            }
            rl.setBackgroundColor(Color.argb(66, 255, 0, 0))

            rl.addView(floatingPlayer)
            wm.addView(rl, params)
            //rl.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            rl.setOnTouchListener(object : View.OnTouchListener {

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

                            wm.updateViewLayout(rl, updateParams)
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

    private fun expandMinimizePlayer(){
        if (!playerExpanded) {
            Toast.makeText(applicationContext, "Expanding player", Toast.LENGTH_SHORT).show()
            rl.updateViewLayout(floatingPlayer, largePParams)
            wm.updateViewLayout(rl, largeWindowParams)
            yPlayer.setShowFullscreenButton(true)
            playerExpanded = true
        } else {
            Toast.makeText(applicationContext, "Minimizing player", Toast.LENGTH_SHORT).show()
            rl.updateViewLayout(floatingPlayer, smallPParams)
            wm.updateViewLayout(rl, smallWindowParams)
            yPlayer.setShowFullscreenButton(false)
            playerExpanded = false
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
        if(queueHandler.hasNext()){
            yPlayer.loadVideo(queueHandler.next()!!)
            playerNotification.updateNotInfo(queueHandler.getCurrPlayingOst())
        }
        else
            yPlayer.pause()
    }

    fun playerPrevious() {
        yPlayer.loadVideo(queueHandler.previous()!!)
        playerNotification.updateNotInfo(queueHandler.getCurrPlayingOst())
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
        if (repeat) {
            yPlayer.seekToMillis(0)
        } else {
                playerNext()
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
        /*Picasso.with(this).load(R.drawable.ic_pause_black_24dp).into(bigViews,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        Picasso.with(this).load(R.drawable.ic_pause_black_24dp).into(views,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())*/
        playing = true
        playerNotification.updateNotButtons(playing)
        mainActivity.pausePlay()
    }

    override fun onStopped() {
    }

    override fun onPaused() {
        /*Picasso.with(this).load(R.drawable.ic_play_arrow_black_24dp).into(bigViews,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())
        Picasso.with(this).load(R.drawable.ic_play_arrow_black_24dp).into(views,
                R.id.status_bar_play, Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status.build())*/
        playing = false
        playerNotification.updateNotButtons(playing)
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

    fun isScreenLocked(): Boolean{
        val myKM = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return myKM.inKeyguardRestrictedInputMode()
    }
}

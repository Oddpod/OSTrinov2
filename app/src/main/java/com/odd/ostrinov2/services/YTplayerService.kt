package com.odd.ostrinov2.services

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
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.odd.ostrinov2.listeners.PlayerListener
import android.app.KeyguardManager
import android.content.IntentFilter
import com.odd.ostrinov2.*
import com.odd.ostrinov2.tools.QueueHandler
import com.odd.ostrinov2.tools.YoutubePlayerHandler
import com.odd.ostrinov2.tools.isSystemAlertPermissionGranted
import com.odd.ostrinov2.tools.requestSystemAlertPermission


class YTplayerService : Service(),
        YouTubePlayer.OnFullscreenListener{

    private val binder = LocalBinder()
    lateinit var wm: WindowManager
    lateinit private var rl: RelativeLayout
    lateinit private var floatingPlayer: FrameLayout
    private var floatingPlayerInitialized: Boolean = false
    private var outsideActivity: Boolean = false
    private var playerExpanded: Boolean = false
    lateinit private var mainActivity: MainActivity
    lateinit var yTPlayerFrag: YouTubePlayerSupportFragment
    private lateinit var playerNotification: PlayerNotificationService
    private lateinit var yPlayerHandler: YoutubePlayerHandler

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

    override fun onBind(intent: Intent): IBinder? = binder

    private val NOT_LOG_TAG = "YTplayerService"

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
            yPlayerHandler.pausePlay()

        } else if (action.equals(Constants.PREV_ACTION, ignoreCase = true)) {
            yPlayerHandler.playerNext()
            /*
            if (yPlayer.hasPrevious()) {
                yPlayer.previous()
                yPlayer.cueVideo("VneKjsUR1oM")
            }*/
        } else if (action.equals(Constants.NEXT_ACTION, ignoreCase = true)) {
            yPlayerHandler.playerNext()
        } else if (action.equals(Constants.ADD_OST_TO_QUEUE, ignoreCase = true)) {
            val ost = intent.getParcelableExtra<Ost>("ost_extra")
            Toast.makeText(applicationContext, ost.title + " added to queue", Toast.LENGTH_SHORT).show()
            yPlayerHandler.getQueueHandler().addToQueue(ost)
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
        } else if (intent.action == Constants.ADD_OST_TO_QUEUE) {
            handleIntent(intent)
            Log.i(NOT_LOG_TAG, "Add ost to queue")
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
            mainActivity.saveSession()
            yPlayerHandler.stopPlayer()

            //stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    fun startQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean,
                   playerListener: PlayerListener, queueAdapter: QueueAdapter, youTubePlayerFragment: YouTubePlayerSupportFragment) {
        val queueHandler = QueueHandler(ostList.toMutableList(), startIndex, shuffle, playerListener, queueAdapter)
        yTPlayerFrag = youTubePlayerFragment
        yTPlayerFrag.retainInstance = true
        playerNotification = PlayerNotificationService(this)
        yPlayerHandler = YoutubePlayerHandler(playerNotification,queueHandler, mainActivity)
        yTPlayerFrag.initialize(Constants.API_TOKEN, yPlayerHandler)
    }

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        yPlayerHandler.initiateQueue(ostList, startIndex, shuffle)
    }

    fun refresh() {
        yTPlayerFrag.onResume()
        outsideActivity = true
        yPlayerHandler.refresh()
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
                    yPlayerHandler.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
                } else {
                    yPlayerHandler.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
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
            yPlayerHandler.yPlayer.setShowFullscreenButton(true)
            playerExpanded = true
        } else {
            Toast.makeText(applicationContext, "Minimizing player", Toast.LENGTH_SHORT).show()
            rl.updateViewLayout(floatingPlayer, smallPParams)
            wm.updateViewLayout(rl, smallWindowParams)
            yPlayerHandler.yPlayer.setShowFullscreenButton(false)
            playerExpanded = false
        }
    }

    fun playerPrevious(){
        yPlayerHandler.playerPrevious()
    }

    fun pausePlay() {
        yPlayerHandler.pausePlay()
    }

    fun playerNext() {
        yPlayerHandler.playerNext()
    }

    fun getQueueHandler(): QueueHandler = yPlayerHandler.getQueueHandler()

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
                        yPlayerHandler.yPlayer.pause()
                        println("Screen off " + "LOCKED")
                    } else {
                        println("Screen off " + "UNLOCKED")
                    }
                }
            }
        }

        applicationContext.registerReceiver(screenOnOffReceiver, theFilter)
    }

    private fun isScreenLocked(): Boolean{
        val myKM = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return myKM.inKeyguardRestrictedInputMode()
    }

    fun setRepeating(repeat : Boolean){
        yPlayerHandler.repeat = repeat
    }

    fun getPlayer() : YouTubePlayer = yPlayerHandler.yPlayer

    fun getPlayerHandler() : YoutubePlayerHandler = yPlayerHandler
}

package com.odd.ostrinov2.tools

import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.View
import android.webkit.*
import android.widget.SeekBar
import android.widget.Toast
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.services.PlayerNotificationService
import android.content.Intent
import android.net.Uri
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity




class YWebPlayer(private var jsPlayer: WebView, private var queueHandler: QueueHandler,
                 private var mContext: Context, var seekBar: SeekBar,
                 private var playerNotService: PlayerNotificationService)
    : SeekBar.OnSeekBarChangeListener {
    var repeat: Boolean = false
    private var loadLastSession: Boolean = false
    private var lastSessionTimeStamp = 0
    private var lastSessionDuration = 0
    var outsideActivity: Boolean = false
    private val client = Client()
    private var pause: Boolean = false
    private var seekBarInitialized = false
    val extraHeaders = HashMap<String, String>()

    init {
        extraHeaders["Referer"] = "http://youtube.com"
        seekBar.setOnSeekBarChangeListener(this)
        jsPlayer.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            println("Hmmmmmmmmmmmmm")
            jsPlayer.settings.mediaPlaybackRequiresUserGesture = false
            jsPlayer.addJavascriptInterface(JavaScriptInterface(this), JAVASCRIPT_OBJ)
        }
        jsPlayer.settings.mediaPlaybackRequiresUserGesture = false
        jsPlayer.settings.userAgentString = "Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            jsPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            // older android version, disable hardware acceleration
            jsPlayer.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        jsPlayer.setWebViewClient(client)
        val url = "https://www.youtube.com/embed/" + queueHandler.getCurrVideoId()

        //webView.loadUrl(url, extraHeaders)
        jsPlayer.loadUrl(url, extraHeaders)
        /*val html = IOHandler.getHtmlFile(queueHandler.getCurrVideoId(), mContext)
        jsPlayer.loadDataWithBaseURL("https://www.youtube.com/player_api", html,
                "text/html", null, null)*/
        jsPlayer.setWebChromeClient(WebChromeClient())

        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        queueHandler.initiateQueue(ostList, startIndex, shuffle)
        val url = "https://www.youtube.com/embed/" + queueHandler.getCurrVideoId() + "?autoplay=1"
        jsPlayer.loadUrl(url, extraHeaders)
        //jsPlayer.loadUrl("javascript: player.loadVideoById(\"" + queueHandler.getCurrVideoId() + "\")", extraHeaders)
        loadLastSession = false
        pause = false
        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun playerNext() {
        /*val url = "https://www.youtube.com/embed/" + queueHandler.next() + "?autoplay=1"
        jsPlayer.loadUrl(url, extraHeaders)*/
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"" + queueHandler.next() + "\")", extraHeaders)
        pause = false
        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun playerSeekTo(progress: Int) {
        jsPlayer.loadUrl("javascript: player.seekTo($progress, true)")
    }

    fun playerPrevious() {
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"" + queueHandler.previous() + "\")")
        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    private fun play() {
        jsPlayer.loadUrl("javascript: player.playVideo()")
        pause = false
        notifyPausePlay(pause)
    }

    private fun pause() {
        jsPlayer.loadUrl("javascript: player.pauseVideo()")
        pause = true
        notifyPausePlay(pause)

    }

    private fun notifyPausePlay(pause: Boolean) {
        if (outsideActivity) {
            return
        }
        if (pause) {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.action = Constants.PAUSE_ACTION
            mContext.startActivity(intent)
        } else {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.action = Constants.PLAY_ACTION
            mContext.startActivity(intent)
        }
    }

    fun playVideo(vid: String) {
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"$vid\")")
    }

    private fun injectJavaScriptFunction(my_web_view: WebView) {
        if (loadLastSession) {
            my_web_view.loadUrl("javascript: player.cueVideoById(\"${queueHandler.getCurrVideoId()}\", " +
                    " \"$lastSessionTimeStamp\")")
        } else {
            my_web_view.loadUrl("javascript: player.loadVideoById(\"${queueHandler.getCurrVideoId()}\")")
        }
        my_web_view.loadUrl("javascript: " +
                "window.androidObj.notifyPausePlay = function(state) { " +
                JAVASCRIPT_OBJ + ".notifyPausePlay(state) }")
        my_web_view.loadUrl("javascript: " +
                "window.androidObj.notifyNext = function() {" +
                JAVASCRIPT_OBJ + ".playerNext()}")
        my_web_view.loadUrl("javascript: " +
                "window.androidObj.notifySeekBar = function(duration) {" +
                JAVASCRIPT_OBJ + ".updateSeekBar(duration)}")

    }

    inner class Client : WebViewClient() {
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            Toast.makeText(mContext, error.toString(), Toast.LENGTH_SHORT).show()
            super.onReceivedError(view, request, error)
        }

        override fun onPageFinished(view: WebView, url: String) {
            injectJavaScriptFunction(view)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val uri = request?.url
                if (uri.toString().contains("https://www.youtube.com/watch")) {
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + queueHandler.getCurrVideoId()))
                    mContext.startActivity(appIntent)
                }
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    companion object {
        val JAVASCRIPT_OBJ = "Android"
        private val BASE_URL = "file:///android_asset/jsplayer.html"
        val handler = Handler()

    }

    private inner class JavaScriptInterface(private var player: YWebPlayer) {

        @JavascriptInterface
        fun notifyPausePlay(state: Int) {
            //Toast.makeText(mContext, state.toString(), Toast.LENGTH_SHORT).show()
            if (state == 1) {
                notifyPausePlay(false)
            } else {
                notifyPausePlay(true)
            }
        }

        @JavascriptInterface
        fun updateSeekBar(videoLength: Int) {
            Toast.makeText(mContext, videoLength.toString(), Toast.LENGTH_SHORT).show()
            if (loadLastSession) {
                loadLastSession = false

            } else {
                handler.post {
                    seekBar.progress = 0
                    seekBar.max = videoLength
                }
            }
        }

        @JavascriptInterface
        fun playerNext() {
            Toast.makeText(mContext, "Next", Toast.LENGTH_SHORT).show()
            if (repeat) {
                handler.post { player.playAgain() }
            } else {
                handler.post { player.playerNext() }
            }
        }
    }

    private fun playAgain() {
        jsPlayer.loadUrl("javascript: player.playVideo()")
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        handler.post { playerSeekTo(seekBar!!.progress) }
    }

    fun pausePlay() {
        if (pause) {
            handler.post { play() }
        } else {
            handler.post { pause() }
        }
    }

    fun destroy() {
        jsPlayer.destroy()
    }

    fun getQueueHandler() = queueHandler

    fun loadLastSession(playbackPos: Int, duration: Int) {
        loadLastSession = true
        seekBar.progress = playbackPos
        lastSessionTimeStamp = playbackPos
        seekBar.max = duration
        pause = true
    }
}
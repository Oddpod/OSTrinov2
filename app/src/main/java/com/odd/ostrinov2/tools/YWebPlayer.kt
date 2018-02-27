package com.odd.ostrinov2.tools

import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.View
import android.webkit.*
import android.widget.SeekBar
import android.widget.Toast
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.services.PlayerNotificationService

class YWebPlayer(private var jsPlayer: WebView, private var queueHandler: QueueHandler,
                 private var mContext: Context, private var playerNotService: PlayerNotificationService)
                    : SeekBar.OnSeekBarChangeListener {

    private var seekBar: SeekBar = (mContext as MainActivity).seekBar
    val client = Client()
    private var pause: Boolean = false

    init {
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
            jsPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            jsPlayer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        jsPlayer.setWebViewClient(client)
        jsPlayer.loadUrl(BASE_URL)
        jsPlayer.setWebChromeClient(WebChromeClient())

        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        queueHandler.initiateQueue(ostList, startIndex, shuffle)
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"" + queueHandler.getCurrVideoId() + "\")")
        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun playerNext() {
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"" + queueHandler.next() + "\")")
        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun playerSeekTo(progress: Int){
        jsPlayer.loadUrl("javascript: player.seekTo($progress, true)")
    }

    fun playerPrevious() {
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"" + queueHandler.previous() + "\")")
        playerNotService.updateNotInfo(queueHandler.currentlyPlaying)
    }

    fun play(){
        jsPlayer.loadUrl("javascript: player.playVideo()")
        pause = false
    }

    fun pause(){
        jsPlayer.loadUrl("javascript: player.pauseVideo()")
        pause = true
    }
    fun playVideo(vid: String) {
        jsPlayer.loadUrl("javascript: player.loadVideoById(\"$vid\")")
    }

    private fun injectJavaScriptFunction(my_web_view: WebView) {
        my_web_view.loadUrl("javascript: " +
                "window.androidObj.textToAndroid = function(message) { " +
                JAVASCRIPT_OBJ + ".textFromWeb(message) }")
        my_web_view.loadUrl( "javascript: " +
                "window.androidObj.notifyNext = function() {" +
                JAVASCRIPT_OBJ + ".playerNext()}")
        my_web_view.loadUrl( "javascript: " +
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
            //Toast.makeText(mContext, "Loaded", Toast.LENGTH_SHORT).show()
            //view.loadData(YPlayerHandlerjs.createHtml(queueHandler.next()), "text/html", "utf-8")
            //view.loadUrl("javascript:player.loadVideoById('SxV4P0z0xyA');")
        }

        /*fun playerNext() {
            myView.loadUrl("javascript:player.loadVideoById('0W8pOI-l4C4')")
        }*/
    }

    companion object {
        val JAVASCRIPT_OBJ = "Android"
        private val BASE_URL = "file:///android_asset/jsplayer.html"
        val handler = Handler()

    }

    private inner class JavaScriptInterface(private var player: YWebPlayer) {

        @JavascriptInterface
        fun textFromWeb(fromWeb: String) {
            Toast.makeText(mContext, fromWeb, Toast.LENGTH_SHORT).show()
            handler.post{player.playerNext()}
        }
        @JavascriptInterface
        fun updateSeekBar(videoLength : Int) {
            Toast.makeText(mContext, videoLength.toString(), Toast.LENGTH_SHORT).show()
            handler.post{seekBar.progress = 0
                            seekBar.max = videoLength}
        }
        @JavascriptInterface
        fun playerNext() {
            Toast.makeText(mContext, "Next", Toast.LENGTH_SHORT).show()
            handler.post{player.playerNext()}
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(progress == seekBar?.max){
            handler.post{playerNext()}
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        //Toast.makeText(mContext, "stopped tracking", Toast.LENGTH_SHORT).show()
        handler.post{playerSeekTo(seekBar!!.progress)}
    }

    fun pausePlay() {
        if(pause){
            handler.post{play()}
        } else{
            handler.post{pause()}
        }
    }

}
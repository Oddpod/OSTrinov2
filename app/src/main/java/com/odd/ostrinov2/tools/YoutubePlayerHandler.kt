package com.odd.ostrinov2.tools

import android.widget.SeekBar
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.services.PlayerNotificationService

class YoutubePlayerHandler(private var playerNotification:
                           PlayerNotificationService, private var queueHandler: QueueHandler,
                           private var mainActivity: MainActivity) :
        YouTubePlayer.PlaybackEventListener,
        YouTubePlayer.PlayerStateChangeListener,
        YouTubePlayer.OnInitializedListener,
        SeekBar.OnSeekBarChangeListener {

    var playing : Boolean
    var repeat: Boolean
    var loadLastSession : Boolean
    private var stoppedTime : Int
    private var userPaused : Boolean
    lateinit var yPlayer: YouTubePlayer
    private var lastSessionTimeStamp: Int
    init{
        playing = false
        repeat = false
        stoppedTime = 0
        userPaused = false
        loadLastSession = false
        lastSessionTimeStamp = 0
    }

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
            if(loadLastSession){
                yPlayer.cueVideo(queueHandler.currentlyPlaying.videoId, lastSessionTimeStamp)
                loadLastSession = false
                userPaused = true
                lastSessionTimeStamp = 0

            } else{
                yPlayer.loadVideo(queueHandler.currentlyPlaying.videoId)
            }

            yPlayer.setPlayerStateChangeListener(this)
            yPlayer.setPlaybackEventListener(this)
            //yPlayer.setOnFullscreenListener(this)

            yPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT)
            mainActivity.seekBar.setOnSeekBarChangeListener(this)

            playerNotification.updateNotInfo(queueHandler.currentlyPlaying)
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        println("oops")
    }

    override fun onAdStarted() {}

    override fun onLoading() {}

    override fun onVideoStarted() {}

    override fun onLoaded(p0: String?) {
        mainActivity.seekBar.max = yPlayer.durationMillis
    }

    override fun onVideoEnded() {
        if (repeat) {
            yPlayer.seekToMillis(0)
        } else {
            playerNext()
        }
        mainActivity.setSeekBarProgress(0)
    }

    override fun onError(p0: YouTubePlayer.ErrorReason?) {
        println(p0.toString())
    }

    override fun onSeekTo(p0: Int) {}

    override fun onBuffering(p0: Boolean) {}

    override fun onPlaying() {
        playing = true
        playerNotification.updateNotButtons(playing)
        mainActivity.pausePlay(playing)
    }

    override fun onStopped() {}

    override fun onPaused() {
        playing = false
        playerNotification.updateNotButtons(playing)
        mainActivity.pausePlay(playing)
        stoppedTime = yPlayer.currentTimeMillis
    }

    fun playerPrevious() {
        yPlayer.loadVideo(queueHandler.previous()!!)
        playerNotification.updateNotInfo(queueHandler.currentlyPlaying)
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
            playerNotification.updateNotInfo(queueHandler.currentlyPlaying)
        }
        else
            yPlayer.pause()
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

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        queueHandler.initiateQueue(ostList, startIndex, shuffle)
        yPlayer.loadVideo(queueHandler.getCurrVideoId())
        playerNotification.updateNotInfo(queueHandler.currentlyPlaying)
        userPaused = false
    }

    fun refresh(){
        if (userPaused) {
            yPlayer.cueVideo(queueHandler.getCurrVideoId(), stoppedTime)
        } else {
            yPlayer.loadVideo(queueHandler.getCurrVideoId(), stoppedTime)
        }
    }

    fun stopPlayer(){
        yPlayer.release()
        playing = false
        mainActivity.youtubePlayerStopped()
        mainActivity.doUnbindService()
        playerNotification.stopNotService()
        mainActivity.pausePlay(playing)
    }

    fun setPlayerStyle(style: YouTubePlayer.PlayerStyle){
        yPlayer.setPlayerStyle(style)
    }

    fun getQueueHandler(): QueueHandler = queueHandler

    fun loadLastSession(load: Boolean, playbackPos: Int, duration: Int){
        loadLastSession = load
        lastSessionTimeStamp = playbackPos
        mainActivity.seekBar.max = duration
        mainActivity.seekBar.progress = playbackPos

    }
}
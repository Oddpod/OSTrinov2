package com.odd.ostrino

import java.util.*
import com.odd.ostrino.Listeners.PlayerListener


data class QueueHandler(var ostList: List<Ost>, var startIndex : Int, var shuffle : Boolean){

    private var playerListeners: Array<PlayerListener>? = null
    private var preQueue: Stack<String>
    private var played: Stack<String>
    lateinit private var queue:Stack<String>
    private val videoIds: List<String> = UtilMeths.getVideoIdList(ostList)
    var currentlyPlaying : String
    private var playbackPosMilliSec: Int = 0
    private var currPlayingIndex:Int = 0

    init{
        played = Stack<String>()
        preQueue = Stack<String>()
        queue = Stack<String>()
        currentlyPlaying = videoIds.get(startIndex)
        for (i in ostList.indices) {
            val videoId = videoIds.get(i)
            if (i < startIndex) {
                played.add(videoId)
            } else if (i > startIndex) {
                preQueue.add(0, videoId)
            }
        }

        if (shuffle) {
            shuffleOn()
        }
    }

    fun shuffleOff() {
        currPlayingIndex = videoIds.indexOf(currentlyPlaying)
        played = Stack<String>()
        preQueue = Stack<String>()
        for (i in videoIds.indices) {
            val videoId = videoIds[i]
            if (i < currPlayingIndex) {
                played.add(videoId)
            } else if (i > currPlayingIndex) {
                preQueue.add(0, videoId)
            }
        }
        shuffle = false
    }

    fun shuffleOn() {
        val seed = System.nanoTime()
        val ostQ = ostList.subList(currPlayingIndex, ostList.size)
        Collections.shuffle(preQueue, Random(seed))
        Collections.shuffle(ostQ, Random(seed))
        //notifyShuffle(ostQ)
        shuffle = true
    }

    fun addToQueue(url: String) {
        //btnNext.setVisibility(View.VISIBLE)
        queue.add(0, UtilMeths.urlToId(url))
    }

    fun removeFromQueue(url: String) {
        val videoId = UtilMeths.urlToId(url)
        if (queue.contains(videoId)) {
            queue.remove(videoId)
        } else {
            preQueue.remove(videoId)
        }
    }

    fun previous() : String?{
        if (!played.isEmpty()) {
            preQueue.push(currentlyPlaying)
            currentlyPlaying = played.pop()
            return currentlyPlaying
        }else {
            /*if (played.isEmpty()) {
            btnPrevious.setVisibility(View.INVISIBLE)
        }*/
            //notifyPlayerListeners(true)
            return null
        }
    }

    fun next() : String{
        if (!queue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = queue.pop()
            return currentlyPlaying
        } else if (!preQueue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = preQueue.pop()
            return currentlyPlaying
        }
        if (preQueue.isEmpty()) {
            //btnNext.setVisibility(View.INVISIBLE)
        }
        //btnPrevious.setVisibility(View.VISIBLE)
        //notifyPlayerListeners(false)
        return ""
    }

    fun setPlayerListeners(playerListeners: Array<PlayerListener>) {
        this.playerListeners = playerListeners
    }

    fun notifyPlayerListeners(previous: Boolean) {
        for (i in playerListeners!!.indices) {
            playerListeners!![i].updateCurrentlyPlaying(videoIds.indexOf(currentlyPlaying))
            if (previous) {
                playerListeners!![i].previous()
            } else {
                playerListeners!![i].next()
            }

        }
    }

    fun notifyShuffle(ostList: List<Ost>) {
        for (i in playerListeners!!.indices) {
            playerListeners!![i].shuffle(ostList)
        }
    }

    fun getCurrPlayingOst() : Ost{
        return ostList.get(currPlayingIndex)
    }
}

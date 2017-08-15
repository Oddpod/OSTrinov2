package com.odd.ostrino

import com.google.android.youtube.player.YouTubePlayer
import java.util.*
import com.odd.ostrino.Listeners.PlayerListener


data class QueueHandler(var ostList: List<Ost>, var startIndex : Int, var shuffle
: Boolean, var playerListeners: Array<PlayerListener> ){

    private var preQueue: Stack<String>
    private var played: Stack<String>
    private var queue:Stack<String>
    private var videoIds: List<String> = UtilMeths.getVideoIdList(ostList)
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

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        videoIds = ArrayList<String>()
        videoIds = UtilMeths.getVideoIdList(ostList)
        played = Stack<String>()
        preQueue = Stack<String>()
        currentlyPlaying = videoIds[startIndex]
        for (i in ostList.indices) {
            val videoId = videoIds[i]
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
        Collections.shuffle(preQueue, Random(seed))
        val shuffledOstList : List<Ost> = ostList.subList(currPlayingIndex, -1)
        Collections.shuffle(shuffledOstList, Random(seed))
        notifyShuffle(shuffledOstList)
        shuffle = true
    }

    fun addToQueue(url: String) {
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

    fun previous(): String?{
        if (!played.isEmpty()) {
            preQueue.push(currentlyPlaying)
            currentlyPlaying = played.pop()
        }else {
            notifyPlayerListeners(true)
        }

        return currentlyPlaying
    }

    fun next() : String?{
        if (!queue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = queue.pop()
        } else if (!preQueue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = preQueue.pop()
        }
        notifyPlayerListeners(false)
        return currentlyPlaying
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

    fun notifyShuffle(shuffledOstList : List<Ost>) {
        for (i in playerListeners!!.indices) {
            playerListeners!![i].shuffle(shuffledOstList)
        }
    }

    fun getCurrPlayingOst() : Ost{
        return ostList.get(videoIds.indexOf(currentlyPlaying))
    }

    fun hasNext(): Boolean{
        return (!queue.isEmpty() || !preQueue.isEmpty())
    }
}

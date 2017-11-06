package com.odd.ostrinov2.tools

import com.odd.ostrinov2.Ost
import java.util.*
import com.odd.ostrinov2.listeners.PlayerListener


data class QueueHandler(var ostList: MutableList<Ost>, var startIndex : Int, var shuffle
: Boolean, var playerListeners: Array<PlayerListener> ){

    private var preQueue: Stack<String>
    private var played: Stack<String>
    private var queue:Stack<String>
    private var videoIds: MutableList<String> = UtilMeths.getVideoIdList(ostList)
    var currentlyPlaying : String
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
        this.ostList = ostList.toMutableList()
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
        notifyUnShuffle()
    }

    fun shuffleOn() {
        val seed = System.nanoTime()
        Collections.shuffle(preQueue, Random(seed))
        notifyShuffle(seed)
        shuffle = true
    }

    fun addToQueue(ost: Ost) {
        val videoId = UtilMeths.urlToId(ost.url)
        queue.add(0, UtilMeths.urlToId(ost.url))
        ostList.add(currPlayingIndex + 1, ost)
        currPlayingIndex = videoIds.indexOf(currentlyPlaying)
        videoIds.add(currPlayingIndex + 1, videoId)
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
        }
        notifyPlayerListeners(true)
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
        currPlayingIndex = videoIds.indexOf(currentlyPlaying)
        for (i in playerListeners.indices) {
            playerListeners[i].updateCurrentlyPlaying(ostList[currPlayingIndex].id)
            if (previous) {
                playerListeners[i].previous()
            } else {
                playerListeners[i].next()
            }

        }
    }

    fun notifyShuffle(seed: Long) {
        for (i in playerListeners.indices) {
            playerListeners[i].shuffle(seed)
        }
    }

    fun notifyUnShuffle(){
        for (i in playerListeners.indices) {
            playerListeners[i].unShuffle(ostList)
        }
    }

    fun getCurrPlayingOst() : Ost {
        return ostList.get(videoIds.indexOf(currentlyPlaying))
    }

    fun hasNext(): Boolean{
        return (!queue.isEmpty() || !preQueue.isEmpty())
    }
}

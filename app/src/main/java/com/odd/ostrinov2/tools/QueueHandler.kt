package com.odd.ostrinov2.tools

import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.QueueAdapter
import java.util.*
import com.odd.ostrinov2.listeners.PlayerListener


data class QueueHandler(var ostList: MutableList<Ost>, var startIndex : Int, var shuffle
: Boolean, var playerListener: PlayerListener, private var queueAdapter: QueueAdapter ){

    var queue: Stack<Ost>
    private var played: Stack<Ost>
    var currentlyPlaying : Ost
    private var currPlayingIndex:Int = 0
    private var queueAddPos: Int = 0

    init{
        played = Stack()
        queue = Stack()
        currentlyPlaying = ostList[startIndex]
        playerListener.updateCurrentlyPlaying(currentlyPlaying.id)
        played.addAll(ostList.subList(0, startIndex))
        queue.addAll(ostList.subList(startIndex + 1, ostList.size))
        queue.reverse()

        if (shuffle) {
            shuffleOn()
        }

        queueAdapter.initiateQueue(this)
    }

    fun initiateQueue(ostList: List<Ost>, startIndex: Int, shuffle: Boolean) {
        this.ostList = ostList.toMutableList()
        played = Stack()
        queue = Stack()
        currentlyPlaying = ostList[startIndex]
        playerListener.updateCurrentlyPlaying(ostList[startIndex].id)
        played.addAll(ostList.subList(0, startIndex))
        queue.addAll(ostList.subList(startIndex + 1, ostList.size))
        queue.reverse()

        if (shuffle) {
            shuffleOn()
        }
        queueAdapter.initiateQueue(this)
    }

    fun shuffleOff() {
        notifyPlayerListeners(false)
        currPlayingIndex = ostList.indexOf(currentlyPlaying)
        played = Stack()
        queue = Stack()
        for (i in ostList.indices) {
            val videoId = ostList[i]
            if (i < currPlayingIndex) {
                played.add(videoId)
            } else if (i > currPlayingIndex) {
                queue.add(0, videoId)
            }
        }
        shuffle = false
        notifyPlayerListeners(false)
    }

    fun shuffleOn() {
        val seed = System.nanoTime()
        queue.subList(queueAddPos, queue.size).shuffle(Random(seed))
        shuffle = true
        notifyPlayerListeners(false)
    }

    fun addToQueue(ost: Ost) {
        queueAddPos++;
        queue.add(queueAddPos, ost)
        ostList.add(currPlayingIndex + 1, ost)
        currPlayingIndex = ostList.indexOf(currentlyPlaying)
        notifyPlayerListeners(false)
    }

    fun removeFromQueue(ost : Ost) {
        if(queue.indexOf(ost) <= queueAddPos && queueAddPos!= 0){
            queueAddPos--
        }
        queue.remove(ost)
    }

    fun previous(): String?{
        if (!played.isEmpty()) {
            queue.push(currentlyPlaying)
            currentlyPlaying = played.pop()
        }
        notifyPlayerListeners(true)
        return currentlyPlaying.videoId
    }

    fun next() : String?{
        if (!queue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = queue.pop()
        }
        notifyPlayerListeners(false)
        return currentlyPlaying.videoId
    }

    fun notifyPlayerListeners(previous: Boolean) {
        currPlayingIndex = ostList.indexOf(currentlyPlaying)
        playerListener.updateCurrentlyPlaying(ostList[currPlayingIndex].id)
        queueAdapter.notifyDataSetChanged()
        }
    fun getCurrVideoId() : String = currentlyPlaying.videoId

    fun getCurrPlayingIndex(): Int = ostList.indexOf(currentlyPlaying)

    fun hasNext(): Boolean = !queue.isEmpty()
}

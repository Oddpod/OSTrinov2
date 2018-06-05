package com.odd.ostrinov2.tools

import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.QueueAdapter
import com.odd.ostrinov2.listeners.PlayerListener
import java.util.*


class QueueHandler(var ostList: MutableList<Ost>, startIndex : Int, var shuffle
: Boolean, var playerListener: PlayerListener, private var queueAdapter: QueueAdapter ){



    private var userQueue = ArrayDeque<Ost>()
    private var queue: Stack<Ost> = Stack()
    private var played: Stack<Ost> = Stack()
    var currentlyPlaying : Ost
    private var currPlayingIndex:Int = 0

    var queueSize = 0
        get() = queue.size + userQueue.size

    fun getQueueItem(pos: Int): Ost{
        return if(pos < userQueue.size)
            userQueue.elementAt(pos)
        else
            queue.elementAt( queue.size - pos - 1)
    }

    init{
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
        notifyPlayerListeners()
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
        notifyPlayerListeners()
    }

    fun shuffleOn() {
        val seed = System.nanoTime()
        queue.addAll(played)
        played.clear()
        queue.shuffle(Random(seed))
        shuffle = true
        notifyPlayerListeners()
    }

    fun addToQueue(ost: Ost) {
        userQueue.add(ost)
        notifyPlayerListeners(QueueUpdateAction.ADD, userQueue.size - 1)
    }

    fun addToQueue(ostList: List<Ost>) {
        ostList.forEach { addToQueue(it) }
    }

    fun removeFromQueue(index: Int) {
        println(index)
        if(index < userQueue.size){
            val ostIterator = userQueue.descendingIterator()
            var i = 0
            for (ost in ostIterator){
                if(i == index){
                    ostIterator.remove()
                    break
                }
                i++
            }
        }
        else
            queue.removeAt(index)
    }

    fun previous(): String?{
        if (!played.isEmpty()) {
            queue.push(currentlyPlaying)
            currentlyPlaying = played.pop()
        }
        notifyPlayerListeners()
        return currentlyPlaying.videoId
    }

    fun next() : String?{
        if(!userQueue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = userQueue.removeFirst()
        } else if (!queue.isEmpty()) {
            played.push(currentlyPlaying)
            currentlyPlaying = queue.pop()
        }
        return currentlyPlaying.videoId
    }

    fun notifyPlayerListeners() {
        currPlayingIndex = ostList.indexOf(currentlyPlaying)
        playerListener.updateCurrentlyPlaying(ostList[currPlayingIndex].id)
        queueAdapter.notifyDataSetChanged()
        }
    fun getCurrVideoId() : String? = currentlyPlaying.videoId

    fun getCurrPlayingIndex(): Int = ostList.indexOf(currentlyPlaying)

    fun hasNext(): Boolean = !queue.isEmpty()
}

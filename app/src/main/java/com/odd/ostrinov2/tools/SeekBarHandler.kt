package com.odd.ostrinov2.tools

import android.os.Handler
import android.widget.SeekBar

class SeekBarHandler(seekBar: SeekBar){

    private var stopped: Boolean = true
    private val seekbarUpdater = object : Runnable {
        override fun run() {
            seekBar.progress = seekBar.progress + 1
            handler.postDelayed(this, 1000)
        }
    }

    fun stopSeekBar() {
        if(!stopped){
            handler.removeCallbacks(seekbarUpdater)
            stopped = true
        }
    }

    fun startSeekbar() {
        if (stopped) {
            handler.postDelayed(seekbarUpdater, 1000)
            stopped = false
        }
    }
    companion object {
        val handler = Handler()
    }
}
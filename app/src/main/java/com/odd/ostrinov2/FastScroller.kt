package com.odd.ostrinov2

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

class FastScroller : LinearLayout {
    private var scrollHandle: View? = null
    private var recyclerView: RecyclerView? = null

    private var fsHeight: Int = 0

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialise(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialise(context)
    }

    private fun initialise(context: Context) {
        orientation = LinearLayout.HORIZONTAL
        clipChildren = false
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.fastscroller, this)
        scrollHandle = findViewById(R.id.fastscroller_bubble)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fsHeight = h
    }

    private fun setPosition(y: Float) {
        val position = y / fsHeight
        val scrollHeight = scrollHandle!!.height
        val scrollPosToSet = ((fsHeight - scrollHeight) * position).toInt()
        scrollHandle!!.y = getValueInRange(0, fsHeight - scrollHeight, scrollPosToSet).toFloat()
    }

    private fun getValueInRange(min: Int, max: Int, value: Int): Int {
        val minimum = Math.max(min, value)
        return Math.min(minimum, max)
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(ScrollListener())
        fsHeight = recyclerView.height
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView?, dx: Int, dy: Int) {
            val firstVisibleView = recyclerView!!.getChildAt(0)
            val firstVisiblePosition = recyclerView!!.getChildAdapterPosition(firstVisibleView)
            val visibleRange = recyclerView!!.childCount
            val lastVisiblePosition = firstVisiblePosition + visibleRange
            val itemCount = recyclerView!!.adapter.itemCount
            val position: Int = when (lastVisiblePosition) {
                itemCount - 1 -> itemCount - 1
                else -> firstVisiblePosition
            }
            val proportion = position.toFloat() / itemCount.toFloat()
            scrollHandle!!.visibility = View.VISIBLE
            setPosition(fsHeight * proportion)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE)
                handler.postDelayed({ scrollHandle!!.visibility = View.GONE },
                        HANDLE_HIDE_DELAY.toLong())
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            setPosition(event.y)
            setRecyclerViewPosition(event.y)
            return true
        } else if (event.action == MotionEvent.ACTION_UP) {
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView != null) {
            val itemCount = recyclerView!!.adapter.itemCount
            val proportion: Float = when {
                scrollHandle!!.y == 0f -> 0f
                scrollHandle!!.y + scrollHandle!!.height >= fsHeight - TRACK_SNAP_RANGE -> 1f
                else -> y / fsHeight.toFloat()
            }
            val targetPos = getValueInRange(0, itemCount - 1, (proportion * itemCount.toFloat()).toInt())
            recyclerView!!.scrollToPosition(targetPos)
        }
    }

    companion object {

        private const val HANDLE_HIDE_DELAY = 1000
        private const val TRACK_SNAP_RANGE = 5
    }
}
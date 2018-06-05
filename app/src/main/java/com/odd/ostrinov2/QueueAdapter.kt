package com.odd.ostrinov2

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.odd.ostrinov2.tools.QueueHandler
import com.odd.ostrinov2.tools.loadThumbnailInto

class QueueAdapter internal constructor(private val mContext: Context) : RecyclerView.Adapter<QueueAdapter.ViewWrapper>() {

    private var onItemClickListener: OnItemClickListener? = null
    private var queueHandler: QueueHandler? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewWrapper {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(viewGroup.context).inflate(R.layout.queue_row, null)
        // create ViewHolder
        return ViewWrapper(itemLayoutView)
    }

    override fun onBindViewHolder(viewWrapper: ViewWrapper, position: Int) {

        val ost = queueHandler!!.queue[itemCount - position - 1]
        viewWrapper.tvTitle.text = ost.title
        viewWrapper.tvShow.text = ost.show
        viewWrapper.tvTags.text = ost.tags
        loadThumbnailInto(viewWrapper.thumbnail,
                ost.videoId, mContext)
        viewWrapper.btnOptions.setOnClickListener {
            queueHandler!!.removeFromQueue(ost)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return if (queueHandler == null) {
            0
        } else {
            queueHandler?.queue!!.size
        }
    }

    inner class ViewWrapper(val base: View) : RecyclerView.ViewHolder(base), View.OnClickListener {
        var tvShow: TextView = base.findViewById(R.id.tvShow)
        var tvTitle: TextView = base.findViewById(R.id.tvTitle)
        var tvTags: TextView = base.findViewById(R.id.tvTags)
        var thumbnail: ImageView = base.findViewById(R.id.ivThumbnail)
        var btnOptions: ImageButton = base.findViewById(R.id.btnOptions)

        init {
            base.setOnClickListener(this)
        }


        override fun onClick(v: View) {
            try {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(v, adapterPosition)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    internal interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)

    }

    fun initiateQueue(queueHandler: QueueHandler) {
        this.queueHandler = queueHandler
        notifyDataSetChanged()
    }

    fun notifyQueueItemRemoved(position: Int){
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    fun notifyQueueItemInserted(position: Int){
        notifyItemInserted(position)
        notifyItemRangeChanged(position, itemCount - position)
    }
}

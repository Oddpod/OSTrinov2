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
import com.squareup.picasso.Picasso

public class SearchAdapter(private var searchResults: MutableList<VideoObject>,
                           val mContext : Context, val mainActivity: MainActivity) :
        RecyclerView.Adapter<SearchAdapter.ObjectViewWrapper>() {


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ObjectViewWrapper {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(parent!!.context).inflate(R.layout.search_row, null)
        // create ViewHolder
        return ObjectViewWrapper(itemLayoutView)
    }

    override fun getItemCount(): Int = searchResults.size

    class VideoObject(val title: String, val uploader: String,
                      val thumbnailUrl: String, val url: String)

    override fun onBindViewHolder(viewWrapper: ObjectViewWrapper, position: Int) {
        val video = searchResults[position]
        viewWrapper.tvVideoTitle?.text = video.title
        viewWrapper.tvViews?.text = video.uploader
        Picasso.with(mContext)
                .load(video.thumbnailUrl)
                .into(viewWrapper.ivThumbnail)
        viewWrapper.btnOptions?.setOnClickListener {
            mainActivity.addToQueue(Ost(video.title, "", "", video.url))
        }
        viewWrapper.baseView.setOnClickListener{
            val ostList = MutableList(1){Ost(video.title, "", "", video.url)}
            mainActivity.initiatePlayer(ostList, 0)
        }
    }

    class ObjectViewWrapper( base: View) : RecyclerView.ViewHolder(base), View.OnClickListener {

        var tvVideoTitle: TextView? = null
        var tvViews: TextView? = null
        var ivThumbnail: ImageView? = null
        var btnOptions: ImageButton? = null
        val baseView: View

        init {
            baseView = base
            tvVideoTitle = base.findViewById(R.id.tvVideoTitle) as TextView
            tvViews = base.findViewById(R.id.tvViews) as TextView
            ivThumbnail = base.findViewById(R.id.ivThumbnail) as ImageView
            btnOptions = base.findViewById(R.id.btnOptions) as ImageButton
        }


        override fun onClick(v: View?) {

        }

    }

}

package com.odd.ostrinov2

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Picasso
import com.odd.ostrinov2.tools.UtilMeths

class SearchAdapter(private val mContext: Context, val mainActivity: MainActivity) :
        RecyclerView.Adapter<SearchAdapter.ObjectViewWrapper>() {

    private var searchResults: MutableList<VideoObject> = ArrayList(20)


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
        viewWrapper.tvVideoTitle.text = video.title
        viewWrapper.tvViews.text = video.uploader
        viewWrapper.btnOptions.setOnClickListener {
            val pum = PopupMenu(mainActivity, viewWrapper.btnOptions)
            pum.inflate(R.menu.btn_chooser_popup)
            pum.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.chooser_addToQueue -> mainActivity.addToQueue(Ost(video.title, "", "",
                                video.url))
                        R.id.chooser_addToLibrary -> UtilMeths.parseAddOst(video.title, mainActivity, video.url)
                    }
                    return true
                }

            })
            pum.show()
        }
        Picasso.with(mContext)
                .load(video.thumbnailUrl)
                .into(viewWrapper.ivThumbnail)
        viewWrapper.baseView.setOnClickListener {
            val ostList = MutableList(1) { Ost(video.title, "", "", video.url) }
            mainActivity.initiatePlayer(ostList, 0)
        }
    }
    class ObjectViewWrapper(base: View) : RecyclerView.ViewHolder(base), View.OnClickListener {

        var tvVideoTitle: TextView
        var tvViews: TextView
        var ivThumbnail: ImageView
        val btnOptions: ImageButton
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

    fun updateVideoObjects(videoObjects: MutableList<SearchAdapter.VideoObject>, extend: Boolean) {
        if(!extend){
            searchResults.clear()
        }
        searchResults.addAll(videoObjects)
        notifyDataSetChanged()
    }

}

package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.asynctasks.DownloadTNImage
import com.odd.ostrinov2.asynctasks.YParsePlaylist
import com.odd.ostrinov2.asynctasks.YPlaylistToOstList
import com.odd.ostrinov2.tools.UtilMeths
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.async

class SearchAdapter(private val mContext: Context?, private val searchFragment: SearchFragment) :
        RecyclerView.Adapter<SearchAdapter.ObjectViewWrapper>() {

    var searchResults: MutableList<SearchObject> = ArrayList(20)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewWrapper {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(
                parent.context).inflate(R.layout.search_row, null)
        // create ViewHolder
        return ObjectViewWrapper(itemLayoutView)
    }

    override fun getItemCount(): Int = searchResults.size

    //The SearchObject from YoutubeSearch, is either a playlist or a video
    class SearchObject(val title: String, val uploader: String,
                       val thumbnailUrl: String, val id: String, val playlist: Boolean,
                       val numVideos: Int = 0)

    override fun onBindViewHolder(viewWrapper: ObjectViewWrapper, position: Int) {
        val video = searchResults[position]
        viewWrapper.tvVideoTitle.text = video.title
        viewWrapper.tvUploader.text = video.uploader
        viewWrapper.btnOptions.setOnClickListener {
            val pum = PopupMenu(mContext, viewWrapper.btnOptions)
            pum.inflate(R.menu.search_chooser_popup)
            pum.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.chooser_addToQueue -> {
                        addToQueue(video, mContext!!)
                    }
                    R.id.chooser_addToLibrary -> {
                        addToLibrary(video, mContext!!)
                    }
                    R.id.chooser_copyLink -> {
                        UtilMeths.copyToClipBoard(mContext!!, video.id)
                    }
                }
                true
            }
            pum.show()
        }
        Picasso.with(mContext)
                .load(video.thumbnailUrl)
                .into(viewWrapper.ivThumbnail)
        if (video.playlist) {
            viewWrapper.clPlaylistOverlay.visibility = View.VISIBLE
            viewWrapper.tvPlaylistOverlay.text = video.numVideos.toString()
        } else {
            viewWrapper.clPlaylistOverlay.visibility = View.GONE
        }
        viewWrapper.baseView.setOnClickListener {
            if (video.playlist)
                searchFragment.getPlaylistItems(video.id)
            else {
                val videoList = searchResults.filter { !it.playlist }
                val startPos = videoList.indexOf(video)
                val ostList = videoList.map { Ost(it.title, "", "", it.id) }
                UtilMeths.initYTPServiceQueue(mContext!!, ostList, startPos)
            }
        }
    }

    class ObjectViewWrapper(base: View) : RecyclerView.ViewHolder(base), View.OnClickListener {

        var tvVideoTitle: TextView = base.findViewById(R.id.tvVideoTitle) as TextView
        var tvUploader: TextView = base.findViewById(R.id.tvUploader) as TextView
        var ivThumbnail: ImageView = base.findViewById(R.id.ivThumbnail) as ImageView
        val btnOptions: ImageButton = base.findViewById(R.id.btnOptions) as ImageButton
        val tvPlaylistOverlay = base.findViewById(R.id.tvPlaylist) as TextView
        val clPlaylistOverlay = base.findViewById(R.id.clPlaylist) as ConstraintLayout
        val baseView: View = base

        override fun onClick(v: View?) {

        }
    }

    fun updateVideoObjects(searchObjects: List<SearchObject>, extend: Boolean) {
        if (!extend) {
            searchResults.clear()
        }
        searchResults.addAll(searchObjects)
        notifyDataSetChanged()
    }

    companion object {
        fun addToQueue(video: SearchAdapter.SearchObject, mContext: Context) {
            if (MainActivity.youtubePlayerLaunched) {
                if (video.playlist) {
                    YPlaylistToOstList(video.id, mContext).execute()
                } else {
                    UtilMeths.addToYTPServiceQueue(mContext,
                            Ost(video.title, "", "", video.id))
                }
            } else {
                Toast.makeText(mContext, "You have nothing playing :C",
                        Toast.LENGTH_SHORT).show()
            }
        }

        fun addToLibrary(video: SearchObject, mContext: Context) {
            if (video.playlist)
                async {
                    YParsePlaylist(video.id, video.title, mContext).execute()
                    LibraryFragment.shouldRefreshList = true
                }
            else {
                async {
                    DownloadTNImage(mContext).execute(video.id)
                    UtilMeths.parseAddOst(video.title, video.id)
                    LibraryFragment.shouldRefreshList = true
                }
            }
        }
    }

}

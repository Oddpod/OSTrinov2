package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.R
import com.odd.ostrinov2.tools.Playlist

class PlaylistAdapter(val mContext: Context?, val playFrag: PlaylistFragment) : RecyclerView.Adapter<PlaylistAdapter.ObjectViewWrapper>() {

    private var playLists: List<Playlist> = MainActivity.getDbHandler().allPlaylists

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PlaylistAdapter.ObjectViewWrapper {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(parent
                .context).inflate(R.layout.search_row, null)
        // create ViewHolder
        return PlaylistAdapter.ObjectViewWrapper(itemLayoutView)
    }

    fun refreshPlaylists() {
        playLists = MainActivity.getDbHandler().allPlaylists
        notifyDataSetChanged()
    }

    override fun getItemCount() = playLists.count()

    override fun onBindViewHolder(holder: ObjectViewWrapper, position: Int) {
        val playlist = playLists[position]
        println(playlist.toString())
        holder.tvVideoTitle.text = playlist.name
        holder.tvViews.text = playlist.numOsts.toString()
        holder.baseView.setOnClickListener {
            val ostList = MainActivity.getDbHandler().getOstsInPlaylist(playlist.id)
            playFrag.changeAdapter(ostList)

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

}
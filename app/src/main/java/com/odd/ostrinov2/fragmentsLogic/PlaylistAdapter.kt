package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.R
import com.odd.ostrinov2.dialogFragments.PlaylistPicker
import com.odd.ostrinov2.tools.Playlist
import com.odd.ostrinov2.tools.UtilMeths

class PlaylistAdapter(val mContext: Context?, val playFrag: PlaylistFragment) : RecyclerView.Adapter<PlaylistAdapter.ObjectViewWrapper>() {

    private var playLists: List<Playlist> = MainActivity.dbHandler.allPlaylists

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PlaylistAdapter.ObjectViewWrapper {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(parent
                .context).inflate(R.layout.playlist_row, null)
        // create ViewHolder
        return PlaylistAdapter.ObjectViewWrapper(itemLayoutView)
    }

    fun refreshPlaylists() {
        playLists = MainActivity.dbHandler.allPlaylists
        notifyDataSetChanged()
    }

    override fun getItemCount() = playLists.count()

    override fun onBindViewHolder(holder: ObjectViewWrapper, position: Int) {
        val playlist = playLists[position]
        println(playlist.toString())
        holder.tvPlaylistName.text = playlist.name
        holder.tvNumSongs.text = playlist.numOsts.toString()
        holder.baseView.setOnClickListener {
            val ostList = MainActivity.dbHandler.getOstsInPlaylist(playlist.id)
            playFrag.changeAdapter(ostList)
        }
        holder.btnOptions.setOnClickListener {
            val pum = PopupMenu(mContext, holder.btnOptions)
            pum.inflate(R.menu.playlist_chooser_popup)
            pum.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.chooser_addToQueue -> {
                        val ostList = MainActivity.dbHandler.getOstsInPlaylist(playlist.id)
                        UtilMeths.addPlaylistToYTPServiceQueue(mContext, ostList)
                    }
                    R.id.chooser_delete_playlist -> {
                        MainActivity.dbHandler.deletePlaylist(playlist.id)
                        refreshPlaylists()
                    }
                    R.id.add_to_playlist -> {
                        val picker = PlaylistPicker()
                        val bundl = Bundle()
                        val ostList = MainActivity.dbHandler.getOstsInPlaylist(playlist.id)
                        bundl.putParcelableArrayList("ostIds", ostList)
                        picker.arguments = bundl
                        picker.show((mContext as MainActivity).supportFragmentManager,
                                "PlaylistPicker")
                    }
                }
                true
            }
            pum.show()
        }
    }

    class ObjectViewWrapper(base: View) : RecyclerView.ViewHolder(base), View.OnClickListener {

        var tvPlaylistName: TextView = base.findViewById(R.id.tvPlaylistName)
        var tvNumSongs: TextView = base.findViewById(R.id.tvNumOsts)
        val btnOptions: ImageButton = base.findViewById(R.id.btnOptions)
        val baseView: View = base

        override fun onClick(v: View?) {

        }

    }

}
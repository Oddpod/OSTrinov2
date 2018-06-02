package com.odd.ostrinov2.fragmentsLogic

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.FastScroller
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_layout.*
import kotlinx.android.synthetic.main.playlist_layout.view.*

class PlaylistFragment: Fragment() {

    private var isFromBackStack: Boolean = false
    var isViewingPlaylist: Boolean = false
    lateinit var applicationContext: Context
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val rootView = inflater.inflate(R.layout.playlist_layout, container, false)


        val fastScroller = rootView.findViewById(R.id.fast_scroller) as FastScroller
        fastScroller.setRecyclerView(rootView.rvPlaylists)

        playlistAdapter = PlaylistAdapter(context, this)
        rootView.rvPlaylists.adapter = playlistAdapter

        if (rootView.rvPlaylists.adapter?.itemCount == 0) {
            rootView.ivArchives.visibility = View.VISIBLE
            Picasso.with(context).load(
                    "http://i0.kym-cdn.com/entries/icons/original/000/023/967/obiwan.jpg")
                    .into(rootView.ivArchives)
        }
        rootView.ivArchives.setOnClickListener { rootView.ivArchives.visibility = View.GONE }

        rootView.btnCreatePlaylist.setOnClickListener {
            val dbHandler = MainActivity.dbHandler
            dbHandler.addNewPlaylist(rootView.etPlaylistName.text.toString())
            playlistAdapter.refreshPlaylists()
        }
        return rootView
    }

    override fun onDestroyView() {
        isFromBackStack = true
        super.onDestroyView()
    }

    fun changeAdapter(osts: List<Ost>) {
        rvPlaylists.adapter = OstsRVAdapter(applicationContext, osts)
        isViewingPlaylist = true
    }

    fun resetAdapter() {
        rvPlaylists.adapter = playlistAdapter
        isViewingPlaylist = false
    }
}
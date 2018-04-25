package com.odd.ostrinov2.fragmentsLogic

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_layout.*
import kotlinx.android.synthetic.main.playlist_layout.view.*

class PlaylistFragment: Fragment() {

    private var isFromBackStack: Boolean = false
    lateinit var applicationContext: Context
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val rootView = inflater.inflate(R.layout.playlist_layout, container, false)

        playlistAdapter = PlaylistAdapter(context, this)
        val mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rootView.rvPlaylists.layoutManager = mLayoutManager
        rootView.rvPlaylists.adapter = playlistAdapter

        if (rootView.rvPlaylists.adapter?.itemCount == 0) {
            println(rootView.ivArchives.width)
            rootView.ivArchives.visibility = View.VISIBLE
            Picasso.with(context).load(
                    "http://i0.kym-cdn.com/entries/icons/original/000/023/967/obiwan.jpg")
                    .into(rootView.ivArchives)
        }
        //rootView.ivArchives.setOnClickListener {  }

        rootView.btnCreatePlaylist.setOnClickListener {
            val dbHandler = MainActivity.getDbHandler()
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
        println(osts)
        rvPlaylists.adapter = PlaylistRVAdapter(applicationContext, osts)
    }
}
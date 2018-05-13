package com.odd.ostrinov2.dialogFragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.R
import com.odd.ostrinov2.tools.Playlist
import kotlinx.coroutines.experimental.async

class PlaylistPicker : DialogFragment() {

    internal lateinit var builder: AlertDialog.Builder
    internal lateinit var inflater: LayoutInflater
    internal lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dbHandler = MainActivity.getDbHandler()
        val playlists = dbHandler.allPlaylists

        builder = AlertDialog.Builder(activity)

        inflater = activity!!.layoutInflater
        dialogView = inflater.inflate(R.layout.playlist_picker, null)


        val rvPlaylists = dialogView.findViewById(R.id.rvPlaylists) as RecyclerView
        val mLayoutManager = LinearLayoutManager(context)
        rvPlaylists.layoutManager = mLayoutManager

        val ostId = arguments?.getInt("ostId")
        val ostIds = arguments?.getIntegerArrayList("ostIds")
        rvPlaylists.adapter = TextAdapter(playlists, this, ostId, ostIds)

        builder.setView(dialogView)
        return builder.create()
    }

    class TextAdapter(private val playlists: List<Playlist>, val dialog: PlaylistPicker,
                      private val ostId: Int?,
                      private val ostIds: List<Int>?) :
            RecyclerView.Adapter<TextAdapter.ViewHolder>() {

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): TextAdapter.ViewHolder {

            val textView = TextView(parent.context)
            textView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            textView.textSize = 18.0F
            textView.setPadding(0, 7, 0, 7)
            return ViewHolder(textView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val playlist = playlists[position]
            holder.textView.text = playlist.name
            holder.textView.setOnClickListener {
                async {
                    if (ostId != null)
                        MainActivity.getDbHandler().addOstToPlaylist(ostId, playlist.id)
                    else
                        MainActivity.getDbHandler().addOstsToPlaylist(ostIds, playlist.id)
                }
                dialog.dismiss()
            }
        }

        override fun getItemCount() = playlists.size
    }
}

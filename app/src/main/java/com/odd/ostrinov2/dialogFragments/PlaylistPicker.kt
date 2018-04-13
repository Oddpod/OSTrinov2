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

        val ostId = arguments?.getInt("ostid")
        rvPlaylists.adapter = TextAdapter(playlists, ostId, this)

        builder.setView(dialogView)
        return builder.create()
    }

    class TextAdapter(private val playlists: List<Playlist>, private val ostId: Int?,
                      val dialog: PlaylistPicker) :
            RecyclerView.Adapter<TextAdapter.ViewHolder>() {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): TextAdapter.ViewHolder {
            // create a new view
            val textView = TextView(parent.context)
            textView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            return ViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            val playlist = playlists[position]
            holder.textView.text = playlist.name
            holder.textView.setOnClickListener {
                async {
                    MainActivity.getDbHandler().addOstToPlaylist(ostId, playlist.id)
                }
                dialog.dismiss()
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = playlists.size
    }
}

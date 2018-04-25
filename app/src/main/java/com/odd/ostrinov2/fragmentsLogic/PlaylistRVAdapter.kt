package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.listeners.PlayerListener
import com.odd.ostrinov2.tools.UtilMeths
import com.squareup.picasso.Picasso
import java.util.*

open class PlaylistRVAdapter(private val mContext: Context, ostListin: List<Ost>) :
        RecyclerView.Adapter<PlaylistRVAdapter.RowViewHolder>(), PlayerListener {

    private val filteredOstList: MutableList<Ost>
    private val ostList: MutableList<Ost>
    private var prevSortedMode = 0
    private val mInflater: LayoutInflater
    var nowPlaying = -1
        private set
    private var lastQuery = ""

    init {
        ostList = ArrayList()
        ostList.addAll(ostListin)
        filteredOstList = ArrayList()
        filteredOstList.addAll(ostListin)
        mInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RowViewHolder {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(parent
                .context).inflate(R.layout.lib_row, null)
        // create ViewHolder
        return RowViewHolder(itemLayoutView)
    }

    override fun getItemCount(): Int = ostList.size

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val ost = getItem(position)
        val tnFile = UtilMeths.getThumbnailLocal(ost.url, mContext)
        holder.tvTitle.text = ost.title
        holder.tvShow.text = ost.show
        holder.tvTags.text = ost.tags
        Picasso.with(mContext)
                .load(tnFile)
                .into(holder.thumbnail)

        if (nowPlaying == ost.id) {
            holder.base.setBackgroundResource(R.drawable.greenrect)
        } else {
            holder.base.setBackgroundResource(R.drawable.white)
        }
        holder.btnOptions.setOnClickListener {
            val pum = PopupMenu(mContext, holder.btnOptions)
            pum.inflate(R.menu.btn_search_chooser_popup)
            pum.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.chooser_addToQueue -> {
                        UtilMeths.addToYTPServiceQueue(mContext, ost)
                    }
                    R.id.chooser_copyLink -> {
                        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE)
                                as ClipboardManager?
                        val clip = ClipData.newPlainText("Ost url", ost.url)
                        clipboard!!.primaryClip = clip
                        Toast.makeText(mContext, "Link Copied to Clipboard", Toast.LENGTH_SHORT)
                                .show()
                    }
                    R.id.add_to_playlist -> {

                        val mIntent = Intent(mContext, MainActivity::class.java)
                        mIntent.putExtra("ostId", ost.id)
                        mIntent.action = Constants.ADD_OST_TO_PLAYLIST
                        mContext.startActivity(mIntent)

                    }
                }
                true
            }
            pum.show()
        }
        holder.base.setOnClickListener {
            UtilMeths.initYTPServiceQueue(mContext, ostList, position)
        }
    }

    private fun getItem(position: Int): Ost = ostList[position]
    override fun getItemId(position: Int): Long = ostList[position].id.toLong()

    class RowViewHolder(val base: View) : RecyclerView.ViewHolder(base) {
        var tvTitle: TextView = base.findViewById(R.id.tvTitle) as TextView
        var tvShow: TextView = base.findViewById(R.id.tvShow) as TextView
        var tvTags: TextView = base.findViewById(R.id.tvTags) as TextView
        var btnOptions: ImageButton = base.findViewById(R.id.btnOptions) as ImageButton
        var thumbnail: ImageView = base.findViewById(R.id.ivThumbnail) as ImageView
    }

    override fun updateCurrentlyPlaying(newId: Int) {
        nowPlaying = newId
        notifyDataSetChanged()
    }

    fun filter(charText: String) {
        lastQuery = charText.toLowerCase(Locale.getDefault())
        ostList.clear()
        if (lastQuery.isEmpty()) {
            ostList.addAll(filteredOstList)
            return
        }
        if (lastQuery.startsWith("tags:")) {
            val query = lastQuery.removeRange(0, 5).trim()
            val tags = query.split(",")
            filteredOstList.forEach {
                var hit = true
                for (tag in tags) {
                    val trimmedTag = tag.trim()
                    if (!it.tags.toLowerCase(Locale.getDefault()).contains(trimmedTag))
                        hit = false
                }
                if (hit) {
                    ostList.add(it)
                }
            }
        } else if (lastQuery.startsWith("show:")) {
            val query = lastQuery.removeRange(0, 5).trim()
            filteredOstList.forEach {
                if (it.show.toLowerCase(Locale.getDefault()).contains(query))
                    ostList.add(it)
            }
        } else {
            if (lastQuery.startsWith("-")) {
                val query = lastQuery.removeRange(0, 1)
                filteredOstList.forEach {
                    if (!it.searchString.toLowerCase(Locale.getDefault()).contains(query))
                        ostList.add(it)
                }
            } else {

                filteredOstList.forEach {
                    if (it.searchString.toLowerCase(Locale.getDefault()).contains(lastQuery))
                        ostList.add(it)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun sort(mode: Int) {
        if (prevSortedMode == mode) {
            unSort()
            prevSortedMode = 0
            return
        }
        prevSortedMode = mode
        sortInternal(mode)
    }

    private fun sortInternal(mode: Int) {
        if (mode == 0) {
            return
        }
        when (mode) {
            1 -> {
                if (ostList.size > 0) {
                    ostList.sortWith(Comparator { ost1, ost2 -> ost1.title.compareTo(ost2.title) })
                }
                notifyDataSetChanged()
            }
            else -> {
            }
        }
    }

    private fun unSort() {
        ostList.clear()
        filter(lastQuery)
    }

    fun updateList(updatedList: List<Ost>) {
        filteredOstList.clear()
        filteredOstList.addAll(updatedList)
        filter(lastQuery)
        sortInternal(prevSortedMode)
    }

    fun getOstList(): List<Ost> = ostList

    fun removeOst(pos: Int) {
        val item = getItem(pos)
        ostList.removeAt(pos)
        filteredOstList.remove(item)
    }
}

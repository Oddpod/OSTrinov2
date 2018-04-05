package com.odd.ostrinov2.fragmentsLogic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.listeners.PlayerListener
import com.odd.ostrinov2.tools.UtilMeths
import com.squareup.picasso.Picasso

import java.util.ArrayList
import java.util.Collections
import java.util.Locale

internal class CustomAdapter(private val mContext: Context, ostListin: List<Ost>) : BaseAdapter(), PlayerListener {

    private val filteredOstList: MutableList<Ost>
    private val ostList: MutableList<Ost>
    private var prevSortedMode = 0
    private val mInflater: LayoutInflater
    var nowPlaying = -1
        private set
    private var lastQuery = ""

    val nowPlayingOst: Ost
        get() = ostList[nowPlaying]

    init {
        ostList = ArrayList()
        ostList.addAll(ostListin)
        filteredOstList = ArrayList()
        filteredOstList.addAll(ostListin)
        mInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int = ostList.size

    override fun getItem(position: Int): Ost = ostList[position]

    override fun getItemId(position: Int): Long = ostList[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var conView = convertView
        val ost = getItem(position)
        val holder: ViewHolder
        if (conView == null) {
            conView = mInflater.inflate(R.layout.lib_row, null)
            holder = ViewHolder(conView)
            conView!!.tag = holder
        } else {
            holder = conView.tag as ViewHolder
        }
        val tnFile = UtilMeths.getThumbnailLocal(ost.url, mContext)
        //System.out.println(tnPath);
        Picasso.with(mContext)
                .load(tnFile)
                .into(holder.thumbnail)

        if (nowPlaying == ost.id) {
            conView.setBackgroundResource(R.drawable.greenrect)
        } else {
            conView.setBackgroundResource(R.drawable.white)
        }
        holder.btnOptions.setOnClickListener { val pum = PopupMenu(mContext, holder.btnOptions)
            pum.inflate(R.menu.btn_search_chooser_popup)
            pum.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.chooser_addToQueue -> { UtilMeths.sendToYTPService(mContext,
                           ost, Constants.ADD_OST_TO_QUEUE)
                    }
                    R.id.chooser_copyLink ->{
                        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE)
                                as ClipboardManager?
                        val clip = ClipData.newPlainText("Ost url", ost.url)
                        clipboard!!.primaryClip = clip
                        Toast.makeText(mContext, "Link Copied to Clipboard", Toast.LENGTH_SHORT)
                                .show()}
                }
                true
            }
            pum.show()
        }

        holder.tvTitle.text = ost.title
        holder.tvShow.text = ost.show
        holder.tvTags.text = ost.tags

        return conView
    }

    private inner class ViewHolder internal constructor(convertView: View) {
        internal var tvTitle: TextView = convertView.findViewById(R.id.tvTitle) as TextView
        internal var tvShow: TextView = convertView.findViewById(R.id.tvShow) as TextView
        internal var tvTags: TextView = convertView.findViewById(R.id.tvTags) as TextView
        internal var btnOptions: ImageButton = convertView.findViewById(R.id.btnOptions) as ImageButton
        internal var thumbnail: ImageView = convertView.findViewById(R.id.ivThumbnail) as ImageView
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
        } else {
            filteredOstList.forEach{
                if(it.searchString.toLowerCase(Locale.getDefault()).contains(lastQuery))
                    ostList.add(it)
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
                    Collections.sort(ostList) { ost1, ost2 -> ost1.title.compareTo(ost2.title) }
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

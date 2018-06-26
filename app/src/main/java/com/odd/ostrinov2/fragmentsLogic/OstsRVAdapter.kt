package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.asynctasks.DownloadTNImage
import com.odd.ostrinov2.dialogFragments.EditOstDialog
import com.odd.ostrinov2.dialogFragments.PlaylistPicker
import com.odd.ostrinov2.listeners.PlayerListener
import com.odd.ostrinov2.tools.FilterHandler
import com.odd.ostrinov2.tools.SortHandler
import com.odd.ostrinov2.tools.UtilMeths
import com.odd.ostrinov2.tools.loadThumbnailInto

class OstsRVAdapter(private val mContext: Context, ostListIn: List<Ost>) :
        RecyclerView.Adapter<OstsRVAdapter.RowViewHolder>(), PlayerListener,
        EditOstDialog.EditOstDialogListener {

    private var ostList: MutableList<Ost> = ArrayList()
    private var unFilteredOstList: MutableList<Ost> = ArrayList()
    private val filterHandler: FilterHandler
    private val sortHandler: SortHandler
    private var editOStDialog: EditOstDialog
    private var nowPlaying = -1
    private var lastQuery = ""

    init {
        ostList.addAll(ostListIn)
        unFilteredOstList.addAll(ostListIn)
        filterHandler = FilterHandler()
        sortHandler = SortHandler(this)
        editOStDialog = EditOstDialog()
        editOStDialog.setEditOstListener(this)
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
        holder.tvTitle.text = ost.title
        holder.tvShow.text = ost.show
        holder.tvTags.text = ost.tags
        loadThumbnailInto(holder.thumbnail, ost.videoId, mContext)

        if (nowPlaying == ost.id) {
            holder.base.setBackgroundResource(R.drawable.greenrect)
        } else {
            holder.base.setBackgroundResource(R.drawable.white)
        }
        holder.btnOptions.setOnClickListener {
            val pum = PopupMenu(mContext, holder.btnOptions)
            pum.inflate(R.menu.lib_chooser_popup)
            pum.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.chooser_addToQueue -> {
                        UtilMeths.addToYTPServiceQueue(mContext, ost)
                    }
                    R.id.chooser_copyLink -> {
                        UtilMeths.copyToClipBoard(mContext, ost.url)
                    }
                    R.id.add_to_playlist -> {
                        val picker = PlaylistPicker()
                        val bundl = Bundle()
                        bundl.putInt("ostId", ost.id)
                        picker.arguments = bundl
                        picker.show((mContext as MainActivity).supportFragmentManager,
                                "PlaylistPicker")
                    }
                }
                true
            }
            pum.show()
        }
        holder.base.setOnClickListener {
            UtilMeths.initYTPServiceQueue(mContext, ostList, position)
        }
        holder.base.setOnLongClickListener {
            editOStDialog.setText(ost)
            val fragMan = (mContext as MainActivity).supportFragmentManager
            editOStDialog.show(fragMan!!, "EditOstDialog")
            true
        }
    }

    fun getOstList() = ostList
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
        filterHandler.filter(charText, unFilteredOstList, ostList)
        notifyDataSetChanged()
    }

    fun sort(mode: SortHandler.SortMode) {
        sortHandler.sort(mode, ostList)
        notifyDataSetChanged()
    }

    fun unSort() {
        ostList.clear()
        filter(lastQuery)
    }

    fun updateList(updatedList: List<Ost>) {
        ostList.clear()
        unFilteredOstList.clear()
        unFilteredOstList.addAll(updatedList)
        sortHandler.sortInternal(ostList)
        filter(lastQuery)
    }

    override fun onSaveButtonClick(editedOst: Ost, dialog: EditOstDialog) {
        val replaceIndex = ostList.indexOf(editedOst)
        val replaceIndex2 = unFilteredOstList.indexOf(editedOst)
        unFilteredOstList[replaceIndex2] = editedOst
        ostList[replaceIndex] = editedOst
        MainActivity.dbHandler.updateOst(editedOst)
        DownloadTNImage(mContext).execute(editedOst.videoId)
        notifyItemChanged(replaceIndex)
    }

    override fun onDeleteButtonClick(deletedOst: Ost, dialog: EditOstDialog) {
        MainActivity.dbHandler.deleteOst(deletedOst.id)
        val removedIndex = ostList.indexOf(deletedOst)
        ostList.remove(deletedOst)
        unFilteredOstList.remove(deletedOst)
        notifyItemRemoved(removedIndex)
        notifyItemRangeChanged(removedIndex, itemCount - removedIndex)
        UtilMeths.deleteThumbnail(deletedOst.videoId, mContext)
        Toast.makeText(mContext, "Deleted " + deletedOst.title, Toast.LENGTH_SHORT).show()
    }

    fun addNewOst(ost: Ost) {
        unFilteredOstList.add(ost)
        filter(lastQuery)
    }
}

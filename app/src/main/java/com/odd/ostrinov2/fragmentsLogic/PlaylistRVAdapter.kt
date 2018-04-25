package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
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
import com.odd.ostrinov2.dialogFragments.EditOstDialog
import com.odd.ostrinov2.dialogFragments.PlaylistPicker
import com.odd.ostrinov2.listeners.PlayerListener
import com.odd.ostrinov2.tools.FilterHandler
import com.odd.ostrinov2.tools.SortHandler
import com.odd.ostrinov2.tools.UtilMeths
import com.odd.ostrinov2.tools.checkPermission
import com.squareup.picasso.Picasso
import java.util.*

class PlaylistRVAdapter(private val mContext: Context, ostListIn: List<Ost>) :
        RecyclerView.Adapter<PlaylistRVAdapter.RowViewHolder>(), PlayerListener,
        EditOstDialog.EditOstDialogListener {

    private val unFilteredOstList: MutableList<Ost>
    private val ostList: MutableList<Ost>
    private val filterHandler: FilterHandler
    private val sortHandler: SortHandler
    private val mInflater: LayoutInflater
    private var editOStDialog: EditOstDialog
    var nowPlaying = -1
        private set
    private var lastQuery = ""

    init {
        ostList = ArrayList()
        ostList.addAll(ostListIn)
        unFilteredOstList = ArrayList()
        unFilteredOstList.addAll(ostListIn)
        filterHandler = FilterHandler(ostList)
        sortHandler = SortHandler(this, ostList)
        editOStDialog = EditOstDialog()
        editOStDialog.setEditOstListener(this)
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
        println("relaunching views")

        val ost = getItem(position)
        println("""SearchString: ${ost.searchString}""")
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
                        val picker = PlaylistPicker()
                        val bundl = Bundle()
                        bundl.putInt("ostId", ost.id)
                        println("ostId: ${ost.id}")
                        picker.arguments = bundl
                        picker.show((mContext as MainActivity).supportFragmentManager,
                                "PlaylistPicker")
                        /*val mIntent = Intent(mContext, MainActivity::class.java)
                        mIntent.putExtra("ostId", ost.id)
                        mIntent.action = Constants.ADD_OST_TO_PLAYLIST
                        mContext.startActivity(mIntent)*/

                    }
                }
                true
            }
            pum.show()
        }
        holder.base.setOnClickListener {
            ostList.forEach { print("$it, ") }
            UtilMeths.initYTPServiceQueue(mContext, ostList, position)
        }
        holder.base.setOnLongClickListener {
            editOStDialog.setText(ost)
            val fragMan = (mContext as MainActivity).supportFragmentManager
            editOStDialog.show(fragMan!!, "EditOstDialog")
            true
        }
    }

    fun getItem(position: Int): Ost = ostList[position]
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
        filterHandler.filter(charText)
        notifyDataSetChanged()
    }

    fun sort(mode: SortHandler.SortMode) {
        sortHandler.sort(mode)
    }

    fun unSort() {
        ostList.clear()
        filter(lastQuery)
    }

    fun updateList(updatedList: List<Ost>) {
        unFilteredOstList.clear()
        unFilteredOstList.addAll(updatedList)
        unFilteredOstList.forEach { print(it) }
        filter(lastQuery)
        sortHandler.sortInternal()
    }

    fun getOstList(): List<Ost> = ostList

    override fun onSaveButtonClick(editedOst: Ost, dialog: EditOstDialog) {
        checkPermission(mContext as MainActivity)
        val replaceIndex = ostList.indexOf(editedOst)
        ostList[replaceIndex] = editedOst
        MainActivity.getDbHandler().updateOst(editedOst)
        UtilMeths.downloadThumbnail(editedOst.url, mContext)
    }

    override fun onDeleteButtonClick(deletedOst: Ost, dialog: EditOstDialog) {
        println("Deleting ost")
        MainActivity.getDbHandler().deleteOst(deletedOst.id)
        filterHandler.unFilteredOstList.remove(deletedOst)
        notifyDataSetChanged()
        filter(lastQuery)
        Toast.makeText(mContext, "Deleted " + deletedOst.title, Toast.LENGTH_SHORT).show()
    }

    fun addNewOst(ost: Ost) {
        ostList.add(ost)
        filterHandler.unFilteredOstList.add(ost)
        sortHandler.ostList.add(ost)
        filter(lastQuery)
    }
}

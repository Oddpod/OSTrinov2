package com.odd.ostrinov2.fragmentsLogic

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.squareup.picasso.Picasso
import com.odd.ostrinov2.tools.UtilMeths

class SearchAdapter(private val mContext: Context, val mainActivity: MainActivity) :
        RecyclerView.Adapter<SearchAdapter.ObjectViewWrapper>() {

    private var searchResults: MutableList<VideoObject> = ArrayList(20)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ObjectViewWrapper {
        // create a new view
        @SuppressLint("InflateParams") val itemLayoutView = LayoutInflater.from(parent!!.context).inflate(R.layout.search_row, null)
        // create ViewHolder
        return ObjectViewWrapper(itemLayoutView)
    }

    override fun getItemCount(): Int = searchResults.size

    class VideoObject(val title: String, val uploader: String,
                      val thumbnailUrl: String, val url: String)

    override fun onBindViewHolder(viewWrapper: ObjectViewWrapper, position: Int) {
        val video = searchResults[position]
        viewWrapper.tvVideoTitle.text = video.title
        viewWrapper.tvViews.text = video.uploader
        viewWrapper.btnOptions.setOnClickListener {
            val pum = PopupMenu(mContext, viewWrapper.btnOptions)
            pum.inflate(R.menu.btn_search_chooser_popup)
            pum.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.chooser_addToQueue -> { UtilMeths.sendToYTPService(mContext,
                            Ost(video.title, "", "", video.url), Constants.ADD_OST_TO_QUEUE)
                    }
                    R.id.chooser_addToLibrary -> {UtilMeths.parseAddOst(video.title, mContext, video.url)
                                                    mainActivity.listFragment.refreshListView()}
                    R.id.chooser_copyLink ->{
                        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE)
                                as ClipboardManager?
                        val clip = ClipData.newPlainText("Ost url", video.url)
                        clipboard!!.primaryClip = clip
                            Toast.makeText(mContext, "Link Copied to Clipboard", Toast.LENGTH_SHORT)
                        .show()}
                }
                true
            }
            pum.show()
        }
        Picasso.with(mContext)
                .load(video.thumbnailUrl)
                .into(viewWrapper.ivThumbnail)
        viewWrapper.baseView.setOnClickListener {
            UtilMeths.sendToYTPService(mContext, Ost(video.title, "", "", video.url),
                    Constants.START_OST)
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

    fun updateVideoObjects(videoObjects: MutableList<VideoObject>, extend: Boolean) {
        if(!extend){
            searchResults.clear()
        }
        searchResults.addAll(videoObjects)
        notifyDataSetChanged()
    }

}

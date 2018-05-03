package com.odd.ostrinov2.fragmentsLogic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.R
import com.odd.ostrinov2.tools.YoutubeSearch
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_layout.view.*

class SearchFragment: Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var youtubeSearch: YoutubeSearch
    private var loading = true
    private val visibleThreshold = 0
    private var previousTotal: Int = 0
    private var visibleItemCount:Int = 0
    private var firstVisibleItem: Int = 0
    private var totalItemCount:Int = 0
    private var isFromBackStack: Boolean = false
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        rootView = inflater.inflate(R.layout.search_layout, container, false)

        rvSearchResults = rootView.findViewById(R.id.rvSearchresults) as RecyclerView
        val mLayoutManager = LinearLayoutManager(mainActivity)
        rvSearchResults.layoutManager = mLayoutManager

        rvSearchResults.addOnScrollListener( object: RecyclerView.OnScrollListener(){

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                visibleItemCount = mLayoutManager.childCount
                totalItemCount = mLayoutManager.itemCount
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false
                        previousTotal = totalItemCount
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
                    // End of ListView has been reached

                    youtubeSearch.getMoreSearchResults()
                    loading = true
                }
            }
        })
        rvSearchResults.adapter = searchAdapter

        return rootView
    }

    override fun onDestroyView() {
        isFromBackStack = true
        super.onDestroyView()
    }

    fun setMainActivity(mainActivity: MainActivity){
        this.mainActivity = mainActivity
        searchAdapter = SearchAdapter(mainActivity.applicationContext, mainActivity)
    }

    fun updateSearchresult(videoObjects: MutableList<SearchAdapter.VideoObject>, extend: Boolean){
        searchAdapter.updateVideoObjects(videoObjects, extend)
        if (searchAdapter.itemCount == 0) {
            rootView.ivArchives.visibility = View.VISIBLE
            Picasso.with(context).load(
                    "http://i0.kym-cdn.com/entries/icons/original/000/023/967/obiwan.jpg")
                    .into(rootView.ivArchives)
        } else if (rootView.ivArchives.visibility == View.VISIBLE)
            rootView.ivArchives.visibility = View.GONE
        rootView.ivArchives.destroyDrawingCache()
    }

    fun performSearch( query: String){
            youtubeSearch = YoutubeSearch(mainActivity, query, this)
    }

    fun isFromBackStack(): Boolean = isFromBackStack
}
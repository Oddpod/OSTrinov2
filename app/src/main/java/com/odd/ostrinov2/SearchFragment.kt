package com.odd.ostrinov2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.tools.YoutubeSearch

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState : Bundle?): View {

        val rootView = inflater!!.inflate(R.layout.search_layout, container, false)

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
                    // End has been reached

                    Log.i("Yaeye!", "end called")

                    youtubeSearch.getMoreSearchResults()

                    loading = true
                }
            }
        })


        return rootView
    }

    fun setMainActivity(mainActivity: MainActivity){
        this.mainActivity = mainActivity
    }

    fun addSearchResults(videoObjects: MutableList<SearchAdapter.VideoObject>){
        searchAdapter.extendVideoObjects(videoObjects)
    }

    fun updateSearchresult(videoObjects: MutableList<SearchAdapter.VideoObject>){
        searchAdapter =  SearchAdapter(videoObjects, mainActivity.applicationContext, mainActivity)
        rvSearchResults.adapter = searchAdapter
    }

    fun performSearch( query: String){
            youtubeSearch = YoutubeSearch(mainActivity, query, this)

    }
}
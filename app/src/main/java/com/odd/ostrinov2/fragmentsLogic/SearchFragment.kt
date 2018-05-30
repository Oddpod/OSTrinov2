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
import com.odd.ostrinov2.asynctasks.YGetPlaylistItems
import com.odd.ostrinov2.asynctasks.YoutubeSearch
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_layout.view.*

class SearchFragment: Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var youtubeSearch: YoutubeSearch
    private lateinit var yPlaylistRetriever: YGetPlaylistItems
    var isInPlaylist: Boolean = false
    private var isFromBackStack: Boolean = false
    private lateinit var rootView: View
    private var lastQuery: String = ""
    private var lastSearchResults: List<SearchAdapter.SearchObject> = ArrayList(20)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        rootView = inflater.inflate(R.layout.search_layout, container, false)

        rvSearchResults = rootView.findViewById(R.id.rvSearchresults) as RecyclerView
        val mLayoutManager = LinearLayoutManager(mainActivity)
        rvSearchResults.layoutManager = mLayoutManager

        rvSearchResults.addOnScrollListener( object: RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {
                    if (isInPlaylist)
                        yPlaylistRetriever.getMoreResults()
                    else
                        youtubeSearch.getMoreSearchResults()
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
        searchAdapter = SearchAdapter(mainActivity.applicationContext, mainActivity, this)
    }

    fun updateSearchResults(searchObjects: List<SearchAdapter.SearchObject>, extend: Boolean) {
        searchAdapter.updateVideoObjects(searchObjects, extend)
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
        lastQuery = query
        youtubeSearch = YoutubeSearch(query, this)
    }

    //Used to get out of playlist and back to search
    fun backPress() {
        isInPlaylist = false
        updateSearchResults(lastSearchResults, false)
    }

    fun getPlaylistItems(pListId: String) {
        lastSearchResults = searchAdapter.searchResults.take(20)
        yPlaylistRetriever = YGetPlaylistItems(pListId, this)
        isInPlaylist = true
    }

    fun isFromBackStack(): Boolean = isFromBackStack
}
package com.odd.ostrinov2.fragmentsLogic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.R
import com.odd.ostrinov2.asynctasks.YGetPlaylistItems
import com.odd.ostrinov2.asynctasks.YoutubeSearch
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.search_layout.view.*

class SearchFragment: Fragment() {

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var youtubeSearch: YoutubeSearch
    private lateinit var yPlaylistRetriever: YGetPlaylistItems
    var isInPlaylist: Boolean = false
    private var isFromBackStack: Boolean = false
    private lateinit var rootView: View
    private var lastQuery: String = ""
    private var lastSearchResults: List<SearchAdapter.SearchObject> = ArrayList(20)

    override fun onCreate(savedInstanceState: Bundle?) {
        searchAdapter = SearchAdapter(context, this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        rootView = inflater.inflate(R.layout.search_layout, container, false)

        val rvSearchResults = rootView.findViewById(R.id.rvSearchResults) as RecyclerView

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

    fun updateSearchResults(searchObjects: List<SearchAdapter.SearchObject>, extend: Boolean) {
        searchAdapter.updateVideoObjects(searchObjects, extend)
        if (searchAdapter.itemCount == 0) {
            rootView.ivArchivesSearch.visibility = View.VISIBLE
            Picasso.with(context).load(
                    "http://i0.kym-cdn.com/entries/icons/original/000/023/967/obiwan.jpg")
                    .into(rootView.ivArchivesSearch)
        } else if (rootView.ivArchivesSearch.visibility == View.VISIBLE)
            rootView.ivArchivesSearch.visibility = View.GONE
        rootView.ivArchivesSearch.destroyDrawingCache()
    }

    fun performSearch( query: String){
        lastQuery = query
        youtubeSearch = YoutubeSearch(query, this)
        isInPlaylist = false
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
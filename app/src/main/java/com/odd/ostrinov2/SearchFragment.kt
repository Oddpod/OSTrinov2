package com.odd.ostrinov2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.tools.YoutubeSearch

class SearchFragment: Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var rvSearchResults: RecyclerView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState : Bundle?): View {

        val rootView = inflater!!.inflate(R.layout.search_layout, container, false)

        rvSearchResults = rootView.findViewById(R.id.rvSearchresults) as RecyclerView
        val mLayoutManager = LinearLayoutManager(mainActivity)
        rvSearchResults.layoutManager = mLayoutManager


        return rootView
    }

    fun setMainActivity(mainActivity: MainActivity){
        this.mainActivity = mainActivity
    }

    fun updateSearchresult(videoObjects: MutableList<SearchAdapter.VideoObject>){
        searchAdapter =  SearchAdapter(videoObjects, mainActivity.applicationContext, mainActivity)
        rvSearchResults.adapter = searchAdapter
    }

    fun performSearch( query: String){
            YoutubeSearch(mainActivity, query, this)

    }
}
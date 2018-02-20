package com.odd.ostrinov2.fragmentsLogic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odd.ostrinov2.R
import kotlinx.android.synthetic.main.search_layout.*

class PlaylistFragment: Fragment() {

    private lateinit var searchAdapter: SearchAdapter
    private var isFromBackStack: Boolean = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState : Bundle?): View {

        val rootView = inflater!!.inflate(R.layout.search_layout, container, false)

        //val adapter: RecyclerView.Adapter<>
        rvSearchresults.adapter = searchAdapter

        return rootView
    }

    override fun onDestroyView() {
        isFromBackStack = true
        super.onDestroyView()
    }
}
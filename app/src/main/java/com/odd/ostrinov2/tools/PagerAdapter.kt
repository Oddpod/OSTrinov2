package com.odd.ostrinov2.tools

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.odd.ostrinov2.fragmentsLogic.SearchFragment

class PagerAdapter(fragMan: FragmentManager, private val frags: List<Fragment>)
            :FragmentStatePagerAdapter(fragMan){

    override fun getItem(position: Int): Fragment {
        val frag =  frags[position]
        if(frag is SearchFragment) {
            if (!frag.isFromBackStack()) {
                frag.performSearch("Pokemon Ost")
            }
        }
        return frag
    }

    override fun getCount(): Int = frags.size

}
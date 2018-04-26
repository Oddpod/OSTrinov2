package com.odd.ostrinov2.tools

import com.odd.ostrinov2.fragmentsLogic.PlaylistRVAdapter
import java.util.*

class FilterHandler {


    private var lastQuery: String = ""

    fun filter(charText: String = lastQuery) {
        val unFilteredOstList = PlaylistRVAdapter.unFilteredOstList
        val ostList = PlaylistRVAdapter.ostList

        lastQuery = charText.toLowerCase(Locale.getDefault())
        PlaylistRVAdapter.ostList.clear()
        if (lastQuery.isEmpty()) {
            PlaylistRVAdapter.ostList.addAll(unFilteredOstList)
            return
        }
        if (lastQuery.startsWith("tags:")) {
            val query = lastQuery.removeRange(0, 5).trim()
            val tags = query.split(",")
            filterTags(tags)
        } else if (lastQuery.startsWith("show:")) {
            val query = lastQuery.removeRange(0, 5).trim()
            unFilteredOstList.forEach {
                if (it.show.toLowerCase(Locale.getDefault()).contains(query))
                    ostList.add(it)
            }
        } else {
            if (lastQuery.startsWith("-")) {
                val query = lastQuery.removeRange(0, 1)
                unFilteredOstList.forEach {
                    if (!it.searchString.toLowerCase(Locale.getDefault()).contains(query))
                        ostList.add(it)
                }
            } else {

                unFilteredOstList.forEach {
                    if (it.searchString.toLowerCase(Locale.getDefault()).contains(lastQuery))
                        ostList.add(it)
                }
            }
        }
    }

    private fun filterTags(tags: List<String>) {
        val unFilteredOstList = PlaylistRVAdapter.unFilteredOstList
        val ostList = PlaylistRVAdapter.ostList
        ostList.clear()
        if (lastQuery.isEmpty()) {
            ostList.addAll(unFilteredOstList)
            return
        }
        unFilteredOstList.forEach {
            var hit = true
            for (tag in tags) {
                val trimmedTag = tag.trim()
                val exclude = trimmedTag.startsWith("-")
                println(trimmedTag.drop(1))
                if (exclude) {
                    val excludeTag = trimmedTag.drop(1)
                    if (it.tags.toLowerCase(Locale.getDefault()).contains(excludeTag)) {
                        hit = false; break
                    }
                } else {
                    if (!it.tags.toLowerCase(Locale.getDefault()).contains(trimmedTag))
                        hit = false; break
                }
            }
            if (hit) {
                ostList.add(it)
            }
        }
        if (lastQuery.isEmpty()) {
            ostList.addAll(unFilteredOstList)
            return
        } else {
            if (lastQuery.startsWith("-")) {
                val query = lastQuery.removeRange(0, 1)
                unFilteredOstList.forEach {
                    if (!it.searchString.toLowerCase(Locale.getDefault()).contains(query))
                        ostList.add(it)
                }
            } else {

                unFilteredOstList.forEach {
                    if (it.searchString.toLowerCase(Locale.getDefault()).contains(lastQuery))
                        ostList.add(it)
                }
            }
        }
    }
}
package com.odd.ostrinov2.tools

import com.odd.ostrinov2.Ost
import java.util.*
import kotlin.collections.ArrayList

class FilterHandler(private var ostList: ArrayList<Ost>) {

    var unFilteredOstList: ArrayList<Ost> = ArrayList(ostList.size)
    private var lastQuery: String = ""

    init {
        unFilteredOstList.addAll(ostList)
    }

    fun filter(charText: String = lastQuery) {
        lastQuery = charText.toLowerCase(Locale.getDefault())
        ostList.clear()
        if (lastQuery.isEmpty()) {
            ostList.addAll(unFilteredOstList)
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
                if (exclude && it.tags.toLowerCase(Locale.getDefault()).contains(trimmedTag.drop(1))) {
                    hit = false
                    break
                }
                if (!it.tags.toLowerCase(Locale.getDefault()).contains(trimmedTag)) {
                    hit = false
                    break
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
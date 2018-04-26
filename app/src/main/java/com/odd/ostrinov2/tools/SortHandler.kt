package com.odd.ostrinov2.tools

import com.odd.ostrinov2.fragmentsLogic.PlaylistRVAdapter

class SortHandler(private val rvAdapter: PlaylistRVAdapter) {

    private var prevSortedMode = SortMode.None

    fun sort(mode: SortMode) {
        if (prevSortedMode == mode) {
            rvAdapter.unSort()
            prevSortedMode = SortMode.None
            return
        }
        prevSortedMode = mode
        sortInternal(mode)
    }

    fun sortInternal(mode: SortMode = prevSortedMode) {
        if (mode == SortMode.None) {
            return
        }
        when (mode) {
            SortMode.Alphabetical -> {
                if (!PlaylistRVAdapter.ostList.isEmpty()) {
                    PlaylistRVAdapter.ostList.sortWith(Comparator { ost1, ost2 -> ost1.title.compareTo(ost2.title) })
                }
            }
            else -> {
            }
        }
    }

    enum class SortMode {
        None,
        Alphabetical

    }
}


package com.odd.ostrinov2.tools

import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.fragmentsLogic.PlaylistRVAdapter

class SortHandler(private val rvAdapter: PlaylistRVAdapter) {

    private var prevSortedMode = SortMode.None

    fun sort(mode: SortMode, ostList: MutableList<Ost>) {
        if (prevSortedMode == mode) {
            rvAdapter.unSort()
            prevSortedMode = SortMode.None
            return
        }
        prevSortedMode = mode
        sortInternal(ostList, mode)
    }

    fun sortInternal(ostList: MutableList<Ost>, mode: SortMode = prevSortedMode) {
        if (mode == SortMode.None) {
            return
        }
        when (mode) {
            SortMode.Alphabetical -> {
                if (!ostList.isEmpty()) {
                    ostList.sortWith(Comparator { ost1, ost2 -> ost1.title.compareTo(ost2.title) })
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


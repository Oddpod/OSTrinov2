package com.odd.ostrinov2.tools

import com.odd.ostrinov2.Constants
import org.json.JSONObject

internal object YDataHandler {
    fun getNumVideosInPlaylist(pListId: String): Int {
        val sh = HttpHandler()
        val jsonUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet" +
                "&playlistId=" + pListId +
                "&key=" + Constants.YDATA_API_TOKEN

        // Making a request to id and getting response
        val jsonStr = sh.makeServiceCall(jsonUrl)
        val pageInfo = JSONObject(jsonStr).getJSONObject("pageInfo")
        return pageInfo.getInt("totalResults")
    }
}
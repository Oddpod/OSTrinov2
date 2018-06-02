package com.odd.ostrinov2.asynctasks

import android.os.AsyncTask
import android.util.Log
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.fragmentsLogic.SearchAdapter
import com.odd.ostrinov2.fragmentsLogic.SearchFragment
import com.odd.ostrinov2.tools.HttpHandler
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class YGetPlaylistItems(private val pListId: String, var searchFragment: SearchFragment) {

    var resultList: MutableList<SearchAdapter.SearchObject> = ArrayList()
    private var nextPagetoken: String
    private val maxResults = 20
    private var totalItems = maxResults
    private var hasNextPage = false
    var loadNextPage: Boolean

    init {
        loadNextPage = false
        nextPagetoken = ""
        GetPlaylistItems().execute()
    }

    fun getMoreResults() {
        loadNextPage = true
        resultList.clear()
        GetPlaylistItems().execute()
    }

    private inner class GetPlaylistItems : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val sh = HttpHandler()
            val jsonUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&" +
                    "maxResults=" + maxResults + "&playlistId=" + pListId +
                    "&key=" + Constants.YDATA_API_TOKEN

            if (!loadNextPage) {
                // Making a request to id and getting response
                val jsonStr = sh.makeServiceCall(jsonUrl)
                val pageInfo = JSONObject(jsonStr).getJSONObject("pageInfo")
                totalItems = pageInfo.getString("totalResults").toInt()

                parseResponseItems(jsonStr)
            } else if (hasNextPage) {
                val jsonUrl2 = "$jsonUrl&pageToken=$nextPagetoken"

                val jsonStr2 = sh.makeServiceCall(jsonUrl2)
                parseResponseItems(jsonStr2)
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            searchFragment.updateSearchResults(resultList, extend = loadNextPage)
        }

        fun parseResponseItems(jsonStr: String?) {
            if (jsonStr != null) {
                try {
                    val jsonObj = JSONObject(jsonStr)
                    if (!jsonObj.has("nextPageToken")) {
                        hasNextPage = false
                    } else {
                        nextPagetoken = jsonObj.getString("nextPageToken")
                        hasNextPage = true
                    }
                    val items: JSONArray = jsonObj.getJSONArray("items")
                    var i = 0
                    while (i < maxResults) {
                        val jsonItemObject = items.getJSONObject(i)

                        val snippet = jsonItemObject.getJSONObject("snippet")
                        val videoId = snippet.getJSONObject("resourceId").getString("videoId")
                        val thumbNailUrl = snippet.getJSONObject("thumbnails")
                                .getJSONObject("medium").getString("url")
                        val videoObject = SearchAdapter.SearchObject(
                                snippet.getString("title"), snippet.getString("channelTitle"),
                                thumbNailUrl, videoId, false)
                        resultList.add(videoObject)
                        i++
                    }

                } catch (e: JSONException) {
                    Log.e("SearchError", "Json parsing error ${e.message}")

                }
            } else {
                Log.e("SearchError", "Couldn't get json from server. " +
                        "JsonString was null")
            }
        }
    }
}
package com.odd.ostrinov2.asynctasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.fragmentsLogic.SearchAdapter
import com.odd.ostrinov2.fragmentsLogic.SearchFragment
import com.odd.ostrinov2.tools.HttpHandler
import com.odd.ostrinov2.tools.YDataHandler
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class YoutubeSearch(private val activity: Activity, private val searchQuery: String, var searchFragment: SearchFragment) {

    var resultList: MutableList<SearchAdapter.SearchObject> = ArrayList()
    var moreResults: MutableList<SearchAdapter.SearchObject> = ArrayList()
    var nextPagetoken: String
    val maxResults: Int = 20
    var loadNextPage: Boolean

    init {
        loadNextPage = false
        nextPagetoken = ""
        val youtubeGetInfo = YoutubeGetInfo()
        youtubeGetInfo.execute()
    }

    fun getMoreSearchResults(){
        loadNextPage = true
        moreResults.clear()
        val youtubeGetInfo = YoutubeGetInfo()
        youtubeGetInfo.execute()
    }

    private inner class YoutubeGetInfo : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg arg0: Void): Void? {
            val sh = HttpHandler()
            if(loadNextPage){
                println("Loading more results")
                val queryString = "https://www.googleapis.com/youtube/v3/search?q=" +
                        "$searchQuery&pageToken=$nextPagetoken&part=" +
                        "snippet&maxResults=$maxResults"
                val jsonUrl = ( queryString + "&key=" + Constants.YDATA_API_TOKEN)
                val jsonStr = sh.makeServiceCall(jsonUrl)
                parseResponseItems(jsonStr)
                return null
            }else{
                println("Searching first time")
                val queryString = "https://www.googleapis.com/youtube/v3/search?q=" +
                        "$searchQuery&part=snippet&maxResults=$maxResults"
                val jsonUrl = ( queryString + "&key=" + Constants.YDATA_API_TOKEN)

                // Making a request to url and getting response
                val jsonStr = sh.makeServiceCall(jsonUrl)

                //Log.e(TAG, "Response from url: $jsonStr");
                parseResponseItems(jsonStr)

                return null
            }

        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if(loadNextPage){
                searchFragment.updateSearchResults(moreResults, extend = true)
                loadNextPage = false
            } else{
                searchFragment.updateSearchResults(resultList, extend = false)
            }
        }

        fun parseResponseItems(jsonStr : String?){
            if (jsonStr != null) {
                try {
                    val jsonObj = JSONObject(jsonStr)
                    nextPagetoken = jsonObj.getString("nextPageToken")
                    val items : JSONArray = jsonObj.getJSONArray("items")
                    var i = 0
                    while (i < maxResults) {
                        val jsonItemObject = items.getJSONObject(i)
                        val id = jsonItemObject.getJSONObject("id")
                        val itemType = id.getString("kind")
                        var playlist = false
                        var numVideosInPlaylist = 0
                        var videoId: String
                        if (itemType == "youtube#playlist") {
                            playlist = true
                            videoId = id.getString("playlistId")
                            numVideosInPlaylist = YDataHandler.getNumVideosInPlaylist(videoId)
                        } else {
                            videoId = id.getString("videoId")
                        }

                        val snippet = jsonItemObject.getJSONObject("snippet")
                        val thumbNailUrl = snippet.getJSONObject("thumbnails")
                                .getJSONObject("medium").getString("url")
                        val videoObject = SearchAdapter.SearchObject(snippet.
                                getString("title"), snippet.getString("channelTitle"),
                                thumbNailUrl, videoId, playlist, numVideosInPlaylist)
                        if(loadNextPage){
                            moreResults.add(videoObject)
                        } else{
                            resultList.add(videoObject)
                        }
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
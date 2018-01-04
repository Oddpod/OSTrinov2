package com.odd.ostrinov2.tools

import android.app.Activity
import android.os.AsyncTask
import android.widget.Toast
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.SearchAdapter
import com.odd.ostrinov2.SearchFragment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class YoutubeSearch(private val activity: Activity, private val searchQuery: String, var searchFragment: SearchFragment) {

    var resultList: MutableList<SearchAdapter.VideoObject>
    var moreResults: MutableList<SearchAdapter.VideoObject>
    var nextPagetoken: String
    val maxResults: Int
    var loadNextPage: Boolean

    init {
        maxResults = 20
        resultList = ArrayList()
        moreResults = ArrayList()
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

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg arg0: Void): Void? {
            val sh = HttpHandler()
            if(loadNextPage){
                println("Loading more results")
                val queryString = "https://www.googleapis.com/youtube/v3/search?q=" +
                        "$searchQuery&type=video&pageToken=$nextPagetoken&part=" +
                        "snippet&maxResults=$maxResults"
                val jsonUrl = ( queryString + "&key=" + Constants.YDATA_API_TOKEN)
                val jsonStr = sh.makeServiceCall(jsonUrl)
                parseResponseItems(jsonStr)
                return null
            }else{
                println("Searching first time")
                val queryString = "https://www.googleapis.com/youtube/v3/search?q=" +
                        "$searchQuery&type=video&part=snippet&maxResults=$maxResults"
                val jsonUrl = ( queryString + "&key=" + Constants.YDATA_API_TOKEN)

                // Making a request to url and getting response
                val jsonStr = sh.makeServiceCall(jsonUrl)

                //  Log.e(TAG, "Response from url: " + jsonStr);
                parseResponseItems(jsonStr)

                return null
            }

        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if(loadNextPage){
                searchFragment.updateSearchresult(moreResults, extend = true)
                loadNextPage = false
            } else{
                searchFragment.updateSearchresult(resultList, extend = false)
            }


        }

        fun parseResponseItems(jsonStr : String?){
            if (jsonStr != null) {
                try {
                    val jsonObj = JSONObject(jsonStr)
                    nextPagetoken = jsonObj.getString("nextPageToken")
                    println(nextPagetoken)
                    val items : JSONArray = jsonObj.getJSONArray("items")
                    var i = 0
                    while (i < maxResults) {
                        val jsonItemObject = items.getJSONObject(i)
                        val videoId = jsonItemObject.getJSONObject("id").
                                getString("videoId")
                        val snippet = jsonItemObject.getJSONObject("snippet")
                        val videoObject = SearchAdapter.VideoObject(snippet.
                                getString("title"), snippet.getString("channelTitle"),
                                UtilMeths.getThumbNailUrl(videoId), UtilMeths.idToUrl(videoId))
                        if(loadNextPage){
                            moreResults.add(videoObject)
                        } else{
                            resultList.add(videoObject)
                        }
                        i++
                    }

                } catch (e: JSONException) {
                    // Log.e(TAG, "Json parsing error: " + e.getMessage());
                    activity.runOnUiThread {
                        Toast.makeText(activity,
                                "Json parsing error: " + e.message,
                                Toast.LENGTH_LONG)
                                .show()
                    }

                }
            } else {
                //Log.e(TAG, "Couldn't get json from server.");
                activity.runOnUiThread {
                    Toast.makeText(activity,
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG)
                            .show()
                }
            }
        }
    }
}
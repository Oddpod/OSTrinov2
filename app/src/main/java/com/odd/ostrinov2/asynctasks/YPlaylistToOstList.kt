package com.odd.ostrinov2.asynctasks

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.tools.HttpHandler
import com.odd.ostrinov2.tools.UtilMeths
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class YPlaylistToOstList(private val pListId: String, context: Context) :
        AsyncTask<Void, Int, Void>() {

    private var wContext: WeakReference<Context> = WeakReference(context)
    private lateinit var nextPagetoken: String
    private var hasNextPage: Boolean = false
    private var ostList: ArrayList<Ost> = ArrayList(20)
    private val maxResults = 20

    override fun doInBackground(vararg params: Void?): Void? {
        val sh = HttpHandler()
        val jsonUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&" +
                "maxResults=" + maxResults + "&playlistId=" + pListId +
                "&key=" + Constants.YDATA_API_TOKEN

        // Making a request to id and getting response
        val jsonStr = sh.makeServiceCall(jsonUrl)

        parseResponseItems(jsonStr)
        while (hasNextPage) {
            val jsonUrl2 = "$jsonUrl&pageToken=$nextPagetoken"

            val jsonStr2 = sh.makeServiceCall(jsonUrl2)
            parseResponseItems(jsonStr2)
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        UtilMeths.addPlaylistToYTPServiceQueue(wContext.get(), ostList)
    }

    fun parseResponseItems(jsonStr: String?) {
        if (jsonStr != null) {
            try {
                val jsonObj = JSONObject(jsonStr)

                val items: JSONArray = jsonObj.getJSONArray("items")

                if (!jsonObj.has("nextPageToken")) {
                    hasNextPage = false
                } else {
                    nextPagetoken = jsonObj.getString("nextPageToken")
                    hasNextPage = true
                }

                var i = 0
                while (i < items.length()) {
                    val jsonItemObject = items.getJSONObject(i)
                    val snippet = jsonItemObject.getJSONObject("snippet")
                    val title = snippet.getString("title")
                    val videoId = snippet.getJSONObject("resourceId").getString("videoId")
                    i++
                    ostList.add(Ost(title, "", "", videoId))
                }

            } catch (e: JSONException) {
                Log.e("JSONEXception", e.message.toString())
            }
        } else {
            Log.e("YParsePlaylist.kt", "Couldn't get json from server.")
        }
    }
}
package com.odd.ostrinov2.asynctasks

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.fragmentsLogic.LibraryFragment
import com.odd.ostrinov2.tools.HttpHandler
import com.odd.ostrinov2.tools.ProgressNotification
import com.odd.ostrinov2.tools.UtilMeths
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class YParsePlaylist(private val pListId: String, private val pListName: String, context: Context) : AsyncTask<Void, Int, Void>() {

    private var parsedTitle: String? = null
    private var wContext: WeakReference<Context> = WeakReference(context)
    private lateinit var nextPageToken: String
    private var hasNextPage: Boolean = false
    private val maxResults = 20
    private var count = 0
    private var totalItems = maxResults
    private var progressNotification: ProgressNotification


    private val mTitle = "Downloading$pListName"

    init {
        progressNotification = ProgressNotification(mTitle, context,
                "Download in progress")
    }

    override fun onPreExecute() {
        super.onPreExecute()
        progressNotification.setStartedNotification()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val sh = HttpHandler()
        val jsonUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&" +
                "maxResults=" + maxResults + "&playlistId=" + pListId +
                "&key=" + Constants.YDATA_API_TOKEN

        // Making a request to id and getting response
        val jsonStr = sh.makeServiceCall(jsonUrl)
        val pageInfo = JSONObject(jsonStr).getJSONObject("pageInfo")
        totalItems = pageInfo.getString("totalResults").toInt()

        parseResponseItems(jsonStr)
        while (hasNextPage) {
            val jsonUrl2 = "$jsonUrl&pageToken=$nextPageToken"

            val jsonStr2 = sh.makeServiceCall(jsonUrl2)
            parseResponseItems(jsonStr2)
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        LibraryFragment.shouldRefreshList = true
        progressNotification.setCompletedNotification()
        Toast.makeText(wContext.get(), "Added $pListName to your OST library", Toast.LENGTH_SHORT).show()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        Log.d("changed progress", "onProgressUpdate with argument = " + values[0])
        super.onProgressUpdate(values.size)
        progressNotification.updateProgress(values[0], totalItems)
    }

    private fun parseResponseItems(jsonStr: String?) {
        if (jsonStr != null) {
            try {
                val jsonObj = JSONObject(jsonStr)

                val items: JSONArray = jsonObj.getJSONArray("items")
                var i = 0

                if (!jsonObj.has("nextPageToken")) {
                    hasNextPage = false
                } else {
                    nextPageToken = jsonObj.getString("nextPageToken")
                    hasNextPage = true
                }

                while (i < items.length()) {
                    val jsonItemObject = items.getJSONObject(i)
                    val snippet = jsonItemObject.getJSONObject("snippet")
                    val title = snippet.getString("title")
                    val videoId = snippet.getJSONObject("resourceId").getString("videoId")
                    i++
                    DownloadTNImage(wContext.get()!!).execute(videoId)
                    parsedTitle = UtilMeths.parseAddOst(title, videoId).title
                    count++
                    publishProgress(count)
                }

            } catch (e: JSONException) {
                Log.e("JSONEXception", e.message.toString())
            }
        } else {
            Log.e("YParsePlaylist.kt", "Couldn't get json from server.")
        }
    }
}
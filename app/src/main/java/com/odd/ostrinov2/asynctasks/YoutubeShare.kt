package com.odd.ostrinov2.tools

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.odd.ostrinov2.Constants
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class YoutubeShare(private val url: String) : AsyncTask<Void, Void, Void>() {

    private var title: String? = null
    private var parsedTitle: String? = null
    private lateinit var wContext: WeakReference<Context>

    fun setContext( context: Context){
        wContext = WeakReference(context)
    }

    override fun doInBackground(vararg params:Void?): Void? {
        val sh = HttpHandler()
        val jsonUrl = ("https://www.youtube.com/oembed?format=json&amp;url=" + url
                + "&key=" + Constants.YDATA_API_TOKEN)

        // Making a request to url and getting response
        val jsonStr = sh.makeServiceCall(jsonUrl)

        //  Log.e(TAG, "Response from url: " + jsonStr);

        if (jsonStr != null) {
            try {
                val jsonObj = JSONObject(jsonStr)
                Log.d("jsonObj", jsonObj.toString())
                title = jsonObj.get("title").toString()
                parsedTitle = UtilMeths.parseAddOst(title!!, url).title

            } catch (e: JSONException) {
                Log.e("JSONException", "Json parsing error: " + e.message)

            }

        } else {
            Log.e("JsonRetrieveError", "Couldn't get json from server.")
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        Toast.makeText(wContext.get(), "Added $parsedTitle to your OST library", Toast.LENGTH_SHORT).show()
    }
}
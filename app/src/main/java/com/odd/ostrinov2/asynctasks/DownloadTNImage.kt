package com.odd.ostrinov2.asynctasks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import com.odd.ostrinov2.tools.UtilMeths
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.net.URL

class DownloadTNImage(context: Context) : AsyncTask<String, Void, Bitmap>() {
    private val wContext = WeakReference<Context>(context)
    private lateinit var videoId: String
    private val TAG = "DownloadTNImage"
    private fun downloadImageBitmap(videoId: String): Bitmap? {
        var bitmap: Bitmap? = null
        val thumbNailUrl = UtilMeths.getThumbnailUrl(videoId)
        try {
            val inputStream = URL(thumbNailUrl).openStream()   // Download Image from URL
            bitmap = BitmapFactory.decodeStream(inputStream)       // Decode Bitmap
            inputStream.close()
        } catch (e: Exception) {
            Log.d(TAG, "Exception 1, Something went wrong!")
            e.printStackTrace()
        }

        return bitmap
    }

    private fun saveImage(context: Context, b: Bitmap?, videoId: String) {
        val foStream: FileOutputStream
        try {
            foStream = context.openFileOutput(videoId, Context.MODE_PRIVATE)
            b?.compress(Bitmap.CompressFormat.JPEG, 100, foStream)
            foStream.close()
        } catch (e: Exception) {
            Log.d("saveImage", e.stackTrace.toString())
            e.printStackTrace()
        }
    }

    override fun doInBackground(vararg params: String): Bitmap?{
        videoId = params[0]
        return downloadImageBitmap(params[0])
    }

    override fun onPostExecute(result: Bitmap?) {
        saveImage(wContext.get()!!, result, videoId)
    }


}


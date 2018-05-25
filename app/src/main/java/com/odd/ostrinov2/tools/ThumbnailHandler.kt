package com.odd.ostrinov2.tools

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.odd.ostrinov2.asynctasks.DownloadTNImage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

fun loadThumbnailInto(iv: ImageView, videoId: String, context: Context) {
    val thumbnailPath = UtilMeths.getThumbnailLocal(videoId, context)
    Picasso.with(context)
            .load(thumbnailPath)
            .into(iv, object : Callback {

                override fun onSuccess() {}

                override fun onError() {
                    //Try again online if cache failed
                    val thumbnailUrl = UtilMeths.getThumbnailUrl(videoId)
                    Picasso.with(context)
                            .load(thumbnailUrl)
                            .into(iv, object : Callback {
                                override fun onSuccess() {
                                    Log.v("Picasso", "Loaded image from web")
                                    DownloadTNImage(context).execute(videoId)
                                }

                                override fun onError() {
                                    Log.v("Picasso", "Could not fetch image")
                                }
                            })
                }
            })
}
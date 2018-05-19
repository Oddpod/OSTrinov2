package com.odd.ostrinov2.tools

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.odd.ostrinov2.R
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

fun loadThumbnailInto(iv: ImageView, videoId: String, context: Context){
    val thumbnailUrl = UtilMeths.getThumbnailUrl(videoId)
    Picasso.with(context)
            .load(thumbnailUrl)
            .networkPolicy(NetworkPolicy.OFFLINE)
            .into(iv, object: Callback {

                override fun onSuccess() {}

                override fun onError() {
                    //Try again online if cache failed
                    Picasso.with(context)
                            .load(thumbnailUrl)
                            .error(R.drawable.close)
                            .into(iv, object: Callback {
                                override fun onSuccess() {
                                    Log.v("Picasso", "Loaded image from web")
                                }

                                override fun onError() {
                                    Log.v("Picasso","Could not fetch image");
                                }
                            })
            }
            })
}
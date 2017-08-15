package com.odd.ostrino

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

import java.io.File
import java.util.ArrayList

internal object UtilMeths {

    fun urlToId(url: String): String {
        var lineArray: Array<String>
        if (url.contains("&")) {
            lineArray = url.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            lineArray = lineArray[0].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else if (url.contains("be/")) {
            lineArray = url.split("be/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else {
            lineArray = url.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        return lineArray[1]
    }

    fun getVideoIdList(ostList: List<Ost>): List<String> {
        val queueList = ArrayList<String>()
        for (ost in ostList) {
            queueList.add(urlToId(ost.url))
        }
        return queueList
    }

    fun doesFileExist(filePath: String): Boolean {
        val folder1 = File(filePath)
        return folder1.exists()

    }

    private fun downloadFile(context: Context, uRl: String, saveName: String) {
        val direct = File(Environment.getExternalStorageDirectory().toString() + "/OSTthumbnails")

        if (!direct.exists()) {
            direct.mkdirs()
        }
        val saveString = direct.absolutePath + "/" + saveName + ".jpg"
        println(saveString)
        if (!UtilMeths.doesFileExist(saveString)) {
            val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val downloadUri = Uri.parse(uRl)
            val request = DownloadManager.Request(
                    downloadUri)

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle(uRl)
                    .setDescription("Downloading thumbnails")
                    .setDestinationInExternalPublicDir("/OSTthumbnails", saveName + ".jpg")

            mgr.enqueue(request)
        }
    }

    fun downloadThumbnail(url: String, context: Context) {
        val saveName = urlToId(url)
        downloadFile(context, "http://img.youtube.com/vi/$saveName/2.jpg", saveName)
    }
}

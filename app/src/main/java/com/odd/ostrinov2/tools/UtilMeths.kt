package com.odd.ostrinov2.tools

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost

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
        } else if(url.contains("=")){
            lineArray = url.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else{
            return url
        }
        return lineArray[1]
    }

    fun idToUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    fun getVideoIdList(ostList: List<Ost>): MutableList<String> {
        val queueList = ArrayList<String>()
        ostList.forEach{queueList.add(urlToId(it.url))}
        return queueList
    }

    private fun doesFileExist(filePath: String): Boolean {
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
        if (!doesFileExist(saveString)) {
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

    fun getThumbNailUrl(videoId: String): String = "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"

    fun getThumbnailLocal(url: String): File = File(Environment.getExternalStorageDirectory().toString()
            + "/OSTthumbnails/" + urlToId(url) + ".jpg");

    fun chooseFileImport(mainActivity: MainActivity) {
        val intent: Intent
        if (Build.VERSION.SDK_INT < 19) {
            intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "text/plain"
            mainActivity.startActivityForResult(intent, 1)

        } else {
            intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/plain"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                mainActivity.startActivityForResult(
                        Intent.createChooser(intent, "Select a File to Upload"),
                        1)
            } catch (ex: android.content.ActivityNotFoundException) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(mainActivity, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun chooseFileExport(mainActivity: MainActivity) {
        val intent: Intent
        if (Build.VERSION.SDK_INT < 19) {
            intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "text/plain"
            mainActivity.startActivityForResult(intent, 2)
        } else {
            intent = Intent()
            intent.action = Intent.ACTION_CREATE_DOCUMENT
            intent.type = "text/plain"
            mainActivity.startActivityForResult(intent, 2)
        }
    }

    fun parseAddOst(title: String, context: Context, url: String) {
        val db = DBHandler(context)
        var titleUC = title
        val shows = db.allShows
        val titleLC = titleUC.toLowerCase()
        var ostShow = ""
        for (show in shows) {
            println("Title: $titleLC show: $show")
            if (show.contains("(")) {
                println("show contains (")
                val lineArray = show.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val showOriginal = lineArray[0]
                val showEnglish = lineArray[1].replace(")", "").trim { it <= ' ' }
                println("show: $showOriginal showEnglish: $showEnglish")
                if (titleLC.contains(showOriginal.toLowerCase()) || titleLC.contains(showEnglish.toLowerCase())) {
                    println("setting to show: " + showOriginal)
                    ostShow = showOriginal
                    if (titleLC.contains(showOriginal.toLowerCase())) {
                        titleUC = titleUC.replace(showOriginal, "").replace("-", "").trim { it <= ' ' }
                    } else {
                        titleUC = titleUC.replace(showEnglish, "").replace("-", "").trim { it <= ' ' }
                    }
                }
            } else if (titleLC.contains(show.toLowerCase()) && show != "") {
                ostShow = show
                titleUC = titleUC.replace(show, "").replace("-", "").trim { it <= ' ' }
            }
        }
        downloadThumbnail(url, context)
        db.addNewOst(Ost(titleUC, ostShow, "", url))
    }

    fun buildOstListFromQueue(idString: String, dbHandler: DBHandler): MutableList<Ost>{
        val idsArray = idString.trim(',').split(',')
        val ostList: MutableList<Ost> = ArrayList(idsArray.size)
        idsArray.forEach{
            if(!it.matches(Regex("[0-9]+"))) {
                ostList.add(Ost(null, null, null, it))
            }else{
                ostList.add(dbHandler.getOst(it.toInt()))
            }
        }
        return ostList
    }
}

package com.odd.ostrinov2.tools

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.services.YTplayerService

internal object UtilMeths {

    fun urlToId(url: String): String {
        var lineArray: Array<String>
        when {
            url.contains("&") -> {
                lineArray = url.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                lineArray = lineArray[0].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            url.contains("be/") -> lineArray = url.split("be/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            url.contains("=") -> lineArray = url.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            else -> return url
        }
        if (lineArray.size == 1)
            return ""
        return lineArray[1]
    }

    fun idToUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    fun idToUrlMobile(videoId: String): String = "https://youtu.be/$videoId"

    fun initYTPServiceQueue(context: Context, ostList: List<Ost>, startPos: Int) {
        val intent = Intent(context, YTplayerService::class.java)
        intent.putExtra("osts_extra", ostList as ArrayList)
        intent.putExtra("startIndex", startPos)
        intent.action = Constants.START_OST
        context.startService(intent)
    }

    fun addToYTPServiceQueue(context: Context, ost: Ost) {
        val intent = Intent(context, YTplayerService::class.java)
        intent.putExtra("ost_extra", ost)
        intent.action = Constants.ADD_OST_TO_QUEUE
        context.startService(intent)
    }

    fun addPlaylistToYTPServiceQueue(context: Context?, ostList: ArrayList<Ost>) {
        val intent = Intent(context, YTplayerService::class.java)
        intent.putParcelableArrayListExtra("ost_extra", ostList)
        intent.action = Constants.ADD_OSTS_TO_QUEUE
        context?.startService(intent)
    }

    fun getThumbnailUrl(videoId: String): String = "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"

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

    fun parseAddOst(title: String, videoId: String): Ost {
        val db = MainActivity.getDbHandler()
        var titleUC = title
        val shows = db.allShows
        val titleLC = titleUC.toLowerCase()
        var ostShow = ""
        for (show in shows) {
            if (show.contains("(")) {
                val lineArray = show.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val showOriginal = lineArray[0]
                val showEnglish = lineArray[1].replace(")", "").trim { it <= ' ' }
                if (titleLC.contains(showOriginal.toLowerCase()) || titleLC.contains(showEnglish.toLowerCase())) {
                    ostShow = showOriginal
                    titleUC = if (titleLC.contains(showOriginal.toLowerCase())) {
                        titleUC.replace(showOriginal, "").replace("-", "").trim { it <= ' ' }
                    } else {
                        titleUC.replace(showEnglish, "").replace("-", "").trim { it <= ' ' }
                    }
                }
            } else if (titleLC.contains(show.toLowerCase()) && show != "") {
                ostShow = show
                titleUC = titleUC.replace(show, "").replace("-", "").trim { it <= ' ' }
            }
        }
        val parsedOst = Ost(titleUC, ostShow, "", videoId)
        db.addNewOst(parsedOst)
        return parsedOst
    }

    fun buildOstListFromQueue(idString: String, dbHandler: DBHandler): MutableList<Ost> {
        val idsArray = idString.trim(',').split(',')
        val ostList: MutableList<Ost> = ArrayList(idsArray.size)
        idsArray.forEach {
            if (!it.matches(Regex("[0-9]+"))) {
                ostList.add(Ost("", "", "", it))
            } else if (dbHandler.getOst(it.toInt()) != null) { //Ost might have gotten deleted
                ostList.add(dbHandler.getOst(it.toInt()) as Ost)
            }
        }
        return ostList
    }
}

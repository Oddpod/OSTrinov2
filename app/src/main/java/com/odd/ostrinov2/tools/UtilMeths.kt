package com.odd.ostrinov2.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.services.YTplayerService
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

internal object UtilMeths {

    fun urlToId(url: String): String {
        val pattern = Pattern.compile("(?<=/|=|^)([_|\\-0-9a-zA-Z]{11})\\s*|&")
        val match = pattern.matcher(url)
        match.find()
        return match.group()
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

    fun getThumbnailLocal(url: String, context: Context): File {
        println(url)
        val fileName = urlToId(url)
        //val preferences = mContext.getSharedPreferences(Constants.TB_STORAGE_LOCATION, 0)
        return File(context.filesDir, fileName)
    }


    fun deleteThumbnail(url: String, context: Context) {
        val tnFile = getThumbnailLocal(url, context)
        try {
            tnFile.delete()
        } catch (ex: NoSuchFileException) {
            System.err.format("%s: no such" + " file or directory%n", tnFile.absolutePath)
        } catch (exc: IOException) {
            // File permission problems are caught here.
            System.err.println(exc)
        }
    }

    fun deleteAllThumbnails(context: Context) {
        deleteRecursive(context.filesDir)
    }

    private fun deleteRecursive(fileOrDirectory: File) {

        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles())
                deleteRecursive(child)

        fileOrDirectory.delete()

    }

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
        val db = MainActivity.dbHandler
        var titleUC = title
        val shows = db.allShows
        val titleLC = titleUC.toLowerCase()
        var ostShow = ""
        var isShowInDb = false
        for (show in shows) {
            if (show.contains("(")) {
                val lineArray = show.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val showOriginal = lineArray[0]
                val showEnglish = lineArray[1].replace(")", "").trim { it <= ' ' }
                if (titleLC.contains(showOriginal.toLowerCase())) {
                    ostShow = show
                    titleUC = titleUC.replace(showOriginal, "").replace("-", "").trim { it <= ' ' }
                }
                if (titleLC.contains(showEnglish.toLowerCase())) {
                    ostShow = show
                    titleUC.replace(showEnglish, "").replace("-", "").trim { it <= ' ' }
                }
                isShowInDb = true
            } else if (titleLC.contains(show.toLowerCase()) && show != "") {
                ostShow = show
                titleUC = titleUC.replace(show, "").replace("-", "").trim { it <= ' ' }
                isShowInDb = true
            }
            titleUC = titleUC.replace(" OST ", "")
            if (isShowInDb)
                break
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

    fun copyToClipBoard(mContext: Context, data: String) {
        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE)
                as ClipboardManager?
        val clip = ClipData.newPlainText("video url", data)
        clipboard!!.primaryClip = clip
        Toast.makeText(mContext, "Link Copied to Clipboard", Toast.LENGTH_SHORT)
                .show()
    }
}

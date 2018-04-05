package com.odd.ostrinov2.tools

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.odd.ostrinov2.Constants
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.services.YTplayerService

import java.io.File
import java.io.IOException
import java.util.ArrayList

internal object UtilMeths {

    fun urlToId(url: String): String {
        var lineArray: Array<String>
        if (url.contains("&")) {
            lineArray = url.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            lineArray = lineArray[0].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else if (url.contains("be/")) {
            lineArray = url.split("be/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else if (url.contains("=")) {
            lineArray = url.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else {
            return url
        }
        return lineArray[1]
    }

    fun idToUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    fun getVideoIdList(ostList: List<Ost>): MutableList<String> {
        val queueList = ArrayList<String>()
        ostList.forEach { queueList.add(urlToId(it.url)) }
        return queueList
    }

    private fun doesFileExist(filePath: String): Boolean {
        val folder1 = File(filePath)
        return folder1.exists()
    }

    private fun downloadFile(context: Context, uRl: String, saveName: String) {

        val isSDPresent = android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
        val isSDSupportedDevice = Environment.isExternalStorageRemovable()
        val storageString: String

        if(isSDSupportedDevice && isSDPresent) {
            storageString = Environment.getExternalStorageDirectory().absolutePath

        } else {
            storageString = context.filesDir.absolutePath
        }
        val direct = File( "/OSTthumbnails")
        createDir(direct, context)
        downloadAndSave(uRl, direct.absolutePath, saveName, context)
    }

    private fun createDir(storageDir: File, context: Context){
        if (!storageDir.exists()) {
            storageDir.mkdirs()
            val settings = context.getSharedPreferences(Constants.TB_STORAGE_LOCATION, 0)
            val editor = settings.edit()
            editor.putString(Constants.TB_STORAGE_LOCATION, storageDir.absolutePath)

            // Commit the edits!
            val success = editor.commit()
            val successString = success.toString()
            Log.i("Wrote Storage loc", successString)
        }
    }

    fun sendToYTPService(context: Context, ost: Ost, action: String){
        val intent = Intent(context, YTplayerService::class.java)
        intent.putExtra("ost_extra", ost)
        intent.action = action
        context.startService(intent)
    }

    private fun downloadAndSave(url: String, dir: String, saveName: String, context: Context) {
        val saveString = "/$saveName.jpg"
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(
                downloadUri)
        if (!UtilMeths.doesFileExist(saveString)) {
            val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Downloading thumbnails")
                    .setDescription(url)
                    .setDestinationInExternalPublicDir(dir, saveString)
            mgr.enqueue(request)
        }
        val settings = context.getSharedPreferences(Constants.TB_STORAGE_LOCATION, 0)
        val editor = settings.edit()
        //println(" dir: " + dir)
        editor.putString(Constants.TB_STORAGE_LOCATION, dir)

        // Commit the edits!
        val success = editor.commit()
        val successString = success.toString()
        Log.i("Wrote Storage loc", successString)
    }

    fun downloadThumbnail(url: String, context: Context) {
        val saveName = urlToId(url)
        downloadFile(context, "http://img.youtube.com/vi/$saveName/2.jpg", saveName)
    }

    fun getThumbNailUrl(videoId: String): String = "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"

    fun getThumbnailLocal(url: String, context: Context): File {
        val fileName = urlToId(url) + ".jpg"
        //val preferences = context.getSharedPreferences(Constants.TB_STORAGE_LOCATION, 0)
        return File(Environment.getExternalStorageDirectory().absolutePath + "/OSTthumbnails" + "/$fileName")
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

    fun parseAddOst(title: String, context: Context, url: String): Ost {
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
        val parsedOst = Ost(titleUC, ostShow, "", url)
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

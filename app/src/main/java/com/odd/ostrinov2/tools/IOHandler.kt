package com.odd.ostrinov2.tools

import android.net.Uri
import android.os.AsyncTask
import android.widget.Toast
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.ref.WeakReference

internal object IOHandler {

    fun writeToFile(uri: Uri, ostList: List<Ost>, mainActivity: MainActivity){
        WriteToFileAsync(uri, ostList, mainActivity).execute()
    }

    class WriteToFileAsync(val uri: Uri, val ostList: List<Ost>, mainActivity: MainActivity) : AsyncTask<Void, Void, Void>() {
        private var wContext: WeakReference<MainActivity> = WeakReference(mainActivity)
        private var progressNotification: ProgressNotification =
                ProgressNotification("Exporting OSTs", mainActivity,
                        "Export in progress")

        override fun onPreExecute() {
            super.onPreExecute()
            progressNotification.setStartedNotification()
        }
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val os = wContext.get()!!.contentResolver.openOutputStream(uri)
                val osw = OutputStreamWriter(os!!)
                var line: String
                var counter = 0
                val numItems = ostList.count()
                for (ost in ostList) {
                    val title = ost.title
                    val show = ost.show
                    val tags = ost.tags
                    val url = ost.url
                    line = "$title; $show; $tags; $url"
                    osw.write(line + "\n")
                    progressNotification.updateProgress(++counter, numItems)
                }
                osw.close()
            } catch (e: IOException) {
                throw IOException("File not found")
            }
            return null
        }
        override fun onPostExecute(result: Void?) {
            wContext.get()!!.libraryFragment.refreshListView()
            Toast.makeText(wContext.get()!!, "Finished Exporting OSts", Toast.LENGTH_SHORT).show()
            progressNotification.setCompletedNotification()
            super.onPostExecute(result)
        }
    }

    fun readFromFile(uri: Uri, mainActivity: MainActivity){
        ReadFromFileAsync(uri, mainActivity).execute()
    }

    class ReadFromFileAsync(val uri: Uri, mainActivity: MainActivity) : AsyncTask<Void, Void, Void>() {
        private var wContext: WeakReference<MainActivity> = WeakReference(mainActivity)
        private val progressNotification: ProgressNotification =
                ProgressNotification("Importing OSTs", mainActivity,
                        "Import in progress")

        override fun onPreExecute() {
            super.onPreExecute()
            progressNotification.setStartedNotification()
        }
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val db = DBHandler(wContext.get()!!)
                val inStream = wContext.get()!!.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inStream!!))
                val lines: List<String> = reader.readLines()
                val numLines = lines.count()
                var counter = 0
                for (line in lines) {
                    val lineArray = line.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (lineArray.size < 4) {
                        continue
                    }
                    val title = lineArray[0]
                    val show = lineArray[1]
                    val tags = lineArray[2]
                    val url = lineArray[3]
                    val ost = Ost(title, show, tags, url)
                    val alreadyInDB = db.checkiIfOstInDB(ost)
                    if (!alreadyInDB) {
                        db.addNewOst(ost)
                        UtilMeths.downloadThumbnail(lineArray[3], wContext.get()!!)
                    }
                    progressNotification.updateProgress(++counter, numLines)
                }
            } catch (e: IOException) {
                println("File not found")
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            wContext.get()!!.libraryFragment.refreshListView()
            Toast.makeText(wContext.get()!!, "Finished Importing OSts", Toast.LENGTH_SHORT).show()
            progressNotification.setCompletedNotification()
            super.onPostExecute(result)
        }
    }
}
package com.odd.ostrinov2.tools

import android.content.Context
import android.net.Uri
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

internal object IOHandler {

    @Throws(IOException::class)
    fun writeToFile(uri: Uri, ostList: List<Ost>, context: Context) {
        try {
            val os = context.contentResolver.openOutputStream(uri)
            val osw = OutputStreamWriter(os!!)
            var line: String
            for (ost in ostList) {
                val title = ost.title
                val show = ost.show
                val tags = ost.tags
                val url = ost.url
                line = "$title; $show; $tags; $url"
                osw.write(line + "\n")
            }
            osw.close()
        } catch (e: IOException) {
            throw IOException("File not found")
        }
    }

    fun readFromFile(uri: Uri, context: Context) {
        try {
            val db = MainActivity.getDbHandler()
            val `is` = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(`is`!!))
            val lines: List<String> = reader.readLines()
            for (line in lines){
                val ost = Ost()
                val lineArray = line.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (lineArray.size < 4) {
                    return
                }
                ost.title = lineArray[0]
                ost.show = lineArray[1]
                ost.tags = lineArray[2]
                ost.url = lineArray[3]
                val alreadyInDB = db.checkiIfOstInDB(ost)
                if (!alreadyInDB) {
                    db.addNewOst(ost)
                    UtilMeths.downloadThumbnail(lineArray[3], context)
                }
            }
        } catch (e: IOException) {
            println("File not found")
            e.printStackTrace()
        }
    }
}
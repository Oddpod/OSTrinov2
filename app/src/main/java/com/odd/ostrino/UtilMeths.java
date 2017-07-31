package com.odd.ostrino;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class UtilMeths {

    static String urlToId(String url){
        String [] lineArray;
        if(url.contains("&")){
            lineArray = url.split("&");
            lineArray = lineArray[0].split("=");
        } else if(url.contains("be/")){
            lineArray = url.split("be/");
        }else{
            lineArray = url.split("=");
        }
        return lineArray[1];
    }

    static List<String> getVideoIdList(List<Ost> ostList){
        List<String> queueList = new ArrayList<>();
        for (Ost ost : ostList){
            queueList.add(urlToId(ost.getUrl()));
        }
        return queueList;
    }

    static boolean doesFileExist(String filePath){
        File folder1 = new File(filePath);
        return folder1.exists();

    }

    static void downloadFile(Context context, String uRl, String saveName) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/OSTthumbnails");

        if (!direct.exists()) {
            direct.mkdirs();
        }
        String saveString = direct.getAbsolutePath() + "/" + saveName + ".jpg";
        System.out.println(saveString);
        if(!UtilMeths.doesFileExist(saveString)) {
            DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri = Uri.parse(uRl);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle(uRl)
                    .setDescription("Downloading thumbnails")
                    .setDestinationInExternalPublicDir("/OSTthumbnails", saveName + ".jpg");

            mgr.enqueue(request);
        }
    }

    static void downloadThumbnail(String url, Context context){
        String saveName = urlToId(url);
        downloadFile(context, "http://img.youtube.com/vi/" + saveName + "/2.jpg", saveName);
    }
}

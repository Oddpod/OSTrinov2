package com.odd.ostrino;

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

    static List<String> extractUrls(List<Ost> ostList){
        List<String> queueList = new ArrayList<>();
        for (Ost ost : ostList){
            queueList.add(ost.getUrl());
        }
        return queueList;
    }

    static boolean doesFileExist(String filePath){
        File folder1 = new File(filePath);
        return folder1.exists();

    }
}

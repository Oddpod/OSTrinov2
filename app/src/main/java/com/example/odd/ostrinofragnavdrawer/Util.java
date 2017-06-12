package com.example.odd.ostrinofragnavdrawer;

import java.util.ArrayList;
import java.util.List;

class Util {

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
}

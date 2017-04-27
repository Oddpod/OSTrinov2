package com.example.odd.ostrinofragnavdrawer;

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
}

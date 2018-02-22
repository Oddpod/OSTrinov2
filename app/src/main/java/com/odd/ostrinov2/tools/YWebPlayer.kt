package com.odd.ostrinov2.tools

import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient


class YWebPlayer(var jsPlayer: WebView, var queueHandler: QueueHandler){

    init{
        jsPlayer.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            jsPlayer.settings.mediaPlaybackRequiresUserGesture = false
        }
        jsPlayer.settings.mediaPlaybackRequiresUserGesture = false
        jsPlayer.settings.userAgentString = "Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0"
        jsPlayer.setWebChromeClient(WebChromeClient())
        jsPlayer.setWebViewClient(WebViewClient())
        jsPlayer.loadData(YPlayerHandlerjs.createHtml(queueHandler.getCurrVideoId()), "text/html", "utf-8")
    }

    fun playerNext(){
        jsPlayer.loadData(YPlayerHandlerjs.createHtml(queueHandler.next()), "text/html", "utf-8")
    }

    fun playVideo( vid : String){
        jsPlayer.loadData(YPlayerHandlerjs.createHtml(queueHandler.next()), "text/html", "utf-8")
    }
}
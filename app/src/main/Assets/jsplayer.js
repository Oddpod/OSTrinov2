 var tag = document.createElement('script');

 tag.src = "https://www.youtube.com/iframe_api";
 var firstScriptTag = document.getElementsByTagName('script')[0];
 firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
 var player;
 function onYouTubeIframeAPIReady() {
  player = new YT.Player('playerFrame', {
    events: {
      'onReady': onPlayerReady,
      'onStateChange': onPlayerStateChange
        }
    });
}

function onPlayerReady(event) {
  player.setPlaybackQuality("auto");
  //player.playVideo()
}

window.androidObj = function AndroidClass(){};
var initial = true;

function updateFromAndroid(message){
    nativeText.nodeValue = message;
}

function onPlayerStateChange(event){
   if(event.data == -1){
      initial = true;
   }
   if (event.data == YT.PlayerState.ENDED){
      window.androidObj.notifyNext();
   }
   if(event.data == YT.PlayerState.PAUSED || event.data == YT.PlayerState.PLAYING){
        window.androidObj.notifyPausePlay(event.data)
   }

   /*if(event.data == YT.PlayerState.CUED){
      window.androidObj.notifySeekBar(player.getDuration());
   }*/
   if(event.data == YT.PlayerState.PLAYING){
        if(initial){
           window.androidObj.notifySeekBar(player.getDuration());
           initial = false;
        }
   }
}
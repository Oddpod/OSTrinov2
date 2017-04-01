package com.example.odd.ostrinofragnavdrawer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.List;


public class YoutubeFragment extends Fragment implements OnInitializedListener{
    // API キー
    public static final String API_KEY = "AIzaSyDSMKvbGUJxKhPz5t4PMFEByD5qFy1sjEA";

    // YouTubeのビデオID
    private String videoId;
    private List<String> videoIds;
    private boolean playQueue = false;
    public YouTubePlayer mPlayer = null;

    public YoutubeFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.youtube_api, container, false);

        // YouTubeフラグメントインスタンスを取得
        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        // レイアウトにYouTubeフラグメントを追加
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_layout, youTubePlayerFragment).commit();

        // YouTubeフラグメントのプレーヤーを初期化する
        youTubePlayerFragment.initialize(API_KEY, this);

        return rootView;
    }

    // YouTubeプレーヤーの初期化成功
    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.mPlayer = player;
        initPlayer();
        //player.setOnFullscreenListener(fullScreenListener);
    }

    // YouTubeプレーヤーの初期化失敗
    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult error) {
        // YouTube error
        String errorMessage = error.toString();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        Log.d("errorMessage:", errorMessage);
    }

    public void initPlayer(){
        if(playQueue){
            mPlayer.loadVideos(videoIds);
            playQueue = false;

        }else{
            mPlayer.loadVideo(videoId);
        }
        mPlayer.play();
        mPlayer.setShowFullscreenButton(false);
    }

    public void setVideoId(String url){
        videoId = urlToId(url);
        System.out.println(videoId);
    }

    public void setVideoIds(List<String> urls){
        videoIds = new ArrayList<>();
        for(String url : urls){
            String id = urlToId(url);
            videoIds.add(id);
        }
        System.out.println("videoIDs: " + videoIds.toString());
    }

    public void playAll(boolean playlist){
        this.playQueue = playlist;
    }

    public String urlToId(String url){
        String [] lineArray;
        if(url.contains("&")){
            lineArray = url.split("&");
            lineArray = lineArray[0].split("=");
        } else if(url.contains("be/")){
            lineArray = url.split("be/");
        }else{
            lineArray = url.split("=");
        }
        return videoId = lineArray[1];
    }

}
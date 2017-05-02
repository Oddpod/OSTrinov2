package com.example.odd.ostrinofragnavdrawer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static android.R.drawable.arrow_down_float;
import static android.R.drawable.arrow_up_float;
import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;
import static android.R.drawable.ic_partial_secure;


public class YoutubeFragment extends Fragment implements View.OnClickListener,
        OnInitializedListener,
        YouTubePlayer.PlayerStateChangeListener,
        YouTubePlayer.PlaybackEventListener{

    // API キー
    public static final String API_KEY = "AIzaSyDSMKvbGUJxKhPz5t4PMFEByD5qFy1sjEA";

    // YouTubeのビデオID
    private String currentlyPlaying;
    private List<String> videoIds;
    private boolean playQueue = false, minimized = false, youtubeFragLaunched, playing, playerStopped;
    public YouTubePlayer mPlayer = null;
    private Stack<String> queue, played;
    private FragmentTransaction transaction;
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private RelativeLayout layoutView;
    private FrameLayout playerLayout;
    public Button btnPrevious, btnPause, btnNext, btnMinimize;
    private View rootView;
    private int playbackPosMilliSec;

    public YoutubeFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.youtube_api, container, false);
        layoutView = (RelativeLayout) rootView;
        playerLayout = (FrameLayout) rootView.findViewById(R.id.youtube_layout);
        // YouTubeフラグメントインスタンスを取得

        btnPrevious = (Button) rootView.findViewById(R.id.btnPrevious);
        btnPause = (Button) rootView.findViewById(R.id.btnPause);
        btnNext = (Button) rootView.findViewById(R.id.btnNext);
        btnMinimize = (Button) rootView.findViewById(R.id.btnMinimize);

        btnPrevious.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);

        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        // レイアウトにYouTubeフラグメントを追加
        transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_layout, youTubePlayerFragment).commit();

        youtubeFragLaunched = true;

        // YouTubeフラグメントのプレーヤーを初期化する
        youTubePlayerFragment.initialize(API_KEY, this);

        return rootView;
    }

    // YouTubeプレーヤーの初期化成功
    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.mPlayer = player;
        queue = new Stack<>();
        played = new Stack<>();
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
            mPlayer.loadVideo(currentlyPlaying);
        }
        mPlayer.play();
        playing = true;
        playerStopped = false;
        mPlayer.setShowFullscreenButton(false);
        mPlayer.setPlayerStateChangeListener(this);
        mPlayer.setPlaybackEventListener(this);
    }

    public void addToQueue(List<String> urls){
        btnNext.setVisibility(View.VISIBLE);
        for (String url: urls){
            queue.add(Util.urlToId(url));
        }
    }

    public void previous() {
        if (!played.isEmpty()) {
            queue.push(currentlyPlaying);
            currentlyPlaying = played.pop();
            mPlayer.loadVideo(currentlyPlaying);
        }
        System.out.println("queue: " + queue.toString());
        System.out.println("played: " + played.toString());
    }

    public void pausePlayer(){
        mPlayer.pause();
    }

    public void unPausePlayer(){
        mPlayer.play();
    }


    public void next(){
        if(!queue.isEmpty()){
            played.push(currentlyPlaying);
            currentlyPlaying = queue.pop();
            mPlayer.loadVideo(currentlyPlaying);
        }
        System.out.println("queue: " + queue.toString());
        System.out.println("played: " + played.toString());
    }

    public void setVideoId(String url){
        currentlyPlaying = Util.urlToId(url);
        System.out.println(currentlyPlaying);
    }

    public void setVideoIds(List<String> urls){
        videoIds = new ArrayList<>();
        for(String url : urls){
            String id = Util.urlToId(url);
            videoIds.add(id);
        }
        System.out.println("videoIDs: " + videoIds.toString());
    }

    public void playAll(boolean playlist){
        this.playQueue = playlist;
    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoaded(String s) {

    }

    @Override
    public void onAdStarted() {

    }

    @Override
    public void onVideoStarted() {
        if(played.isEmpty()){
            btnPrevious.setVisibility(View.INVISIBLE);
        }
        if(queue.isEmpty()){
            btnNext.setVisibility(View.INVISIBLE);
        }
        else if(!played.isEmpty()){
            btnPrevious.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVideoEnded() {
        //Toast.makeText(getActivity(), "Video ended", Toast.LENGTH_SHORT).show();
        if(!queue.isEmpty()){
            String currentVidId = queue.pop();
            played.push(currentVidId);
            mPlayer.loadVideo(currentVidId);
            mPlayer.play();
        }
        btnPrevious.setVisibility(View.VISIBLE);
        if(queue.isEmpty()){
            btnNext.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {

    }

    @Override
    public void onPlaying() {
        playing = true;
        btnPause.setBackgroundResource(ic_media_pause);
        playerStopped = false;
    }

    @Override
    public void onPaused() {
        playing = false;
        btnPause.setBackgroundResource(ic_media_play);

    }

    @Override
    public void onStopped() {
        playing = false;
        playerStopped = true;
        playbackPosMilliSec = mPlayer.getCurrentTimeMillis();
        btnPause.setBackgroundResource(ic_media_play);
    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnPrevious:{
                previous();
                break;
            }

            case R.id.btnPause:{
                System.out.println(playing);
                if(playerStopped){
                    System.out.println("You are here");
                    mPlayer.loadVideo(currentlyPlaying, playbackPosMilliSec);
                }
                else if(playing){
                    mPlayer.pause();
                    btnPause.setBackgroundResource(ic_media_play);
                    playing = false;
                }else{
                    btnPause.setBackgroundResource(ic_media_pause);
                    mPlayer.play();
                    playing = true;
                }
                System.out.println("btnpause pressed");
                break;
            }

            case R.id.btnNext:{
                next();
                break;
            }

            //Not allowed according to API license I think:C
            case R.id.btnMinimize:{
                if(minimized){
                    layoutView.addView(playerLayout);
                    btnMinimize.setBackgroundResource(arrow_up_float);
                    minimized = false;

                }else{
                    btnMinimize.setBackgroundResource(arrow_down_float);
                    layoutView.removeView(playerLayout);
                    minimized = true;
                }

                break;
            }
        }
    }

    public boolean getYoutubeFragLaunched(){
        return youtubeFragLaunched;
    }
}
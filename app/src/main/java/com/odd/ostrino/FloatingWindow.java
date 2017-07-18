package com.odd.ostrino;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.List;

public class FloatingWindow extends Service implements View.OnClickListener, YouTubePlayer.OnInitializedListener{

    private WindowManager wm;
    public RelativeLayout rl;
    private WindowManager.LayoutParams params;
    LayoutInflater inflater;
    String videoID;
    YouTubePlayerSupportFragment youtubeFragment;
    Button btnStop, btnPrevious, btnPause, btnNext, btnMinimize;
    List<String> videoIDs;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        //rl = (RelativeLayout) inflater.inflate(R.layout.activity_youtube, null);
        //YouTubePlayerView youTubePlayerView = (YouTubePlayerView) rl.findViewById(R.id.youtubeView);
        //youTubePlayerView.initialize(Constants.API_TOKEN, this);

        btnStop = (Button) rl.findViewById(R.id.btnClosePlayer);
        /*btnPrevious = (Button) rl.findViewById(R.id.btnFloatPrevious);
        btnPause = (Button) rl.findViewById(R.id.btnFloatPause);
        btnNext = (Button) rl.findViewById(R.id.btnFloatNext);
        btnMinimize = (Button) rl.findViewById(R.id.btnMinimize);

        btnPrevious.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);*/

        rl.setBackgroundColor(Color.argb(66, 255, 0, 0));

        params = new WindowManager.LayoutParams(420, WindowManager.LayoutParams.WRAP_CONTENT
                , WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.x = 0;
        params.y = 0;
        params.gravity = Gravity.CENTER;

        wm.addView(rl, params);


        rl.setOnTouchListener(new View.OnTouchListener() {

            private WindowManager.LayoutParams updateParams = params;
            int X, Y;
            float touchedX, touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        X = updateParams.x;
                        Y = updateParams.y;

                        touchedX = event.getRawX();
                        touchedY = event.getRawY();

                        System.out.println(X + ", " + Y);

                        break;
                    case MotionEvent.ACTION_MOVE:

                        updateParams.x = (int) (X + (event.getRawX() - touchedX));
                        updateParams.y = (int) (Y + (event.getRawY() - touchedY));

                        wm.updateViewLayout(rl, updateParams);

                        break;

                    case MotionEvent.ACTION_MASK:{

                    }

                    default:
                        break;
                }

                return false;
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(rl);
                stopSelf();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            /*case R.id.btnFloatPrevious:{
                break;
            }
            case R.id.btnFloatPause:{
                break;
            }
            case R.id.btnFloatNext:{
                break;
            }*/
            case R.id.btnMinimize:{
                break;
            }
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.loadVideo("pnB_beLVOh8");
        youTubePlayer.play();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}

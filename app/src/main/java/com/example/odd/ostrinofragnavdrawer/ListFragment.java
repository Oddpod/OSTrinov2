package com.example.odd.ostrinofragnavdrawer;

import android.graphics.PixelFormat;
import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class ListFragment extends Fragment implements FunnyJunk.YareYareListener, View.OnClickListener{

    private boolean editedOst;
    private PopupWindow popupWindow;
    private int ostReplaceId, orientation;
    private List<Ost> allOsts;
    private List<CheckBox> checkBoxes;
    private TableLayout tableLayout;
    private float rowTextSize = 11;
    private DBHandler dbHandler;
    private EditText filter;
    private String TAG = "OstInfo";
    private String TAG2 = "Jojo";
    private String filterText;
    private TextWatcher textWatcher;
    private TableRow tR;
    public FrameLayout flOnTop;
    public YoutubeFragment youtubeFragment = null;
    private Button btnDelHeader, btnPlayAll, btnplaySelected, btnStopPlayer, btnShuffle, btnQueue, btnMovePlayer;
    boolean youtubeFragLaunched;
    private View rootView;
    private LayoutInflater inflater;
    private float flPosX, flPosY;
    ViewGroup container;
    boolean floaterLaunched, shuffleActivated, playerDocked;
    AddScreen dialog;
    RelativeLayout parentLayout;
    WindowManager wm;
    RelativeLayout.LayoutParams landParams, portParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        shuffleActivated = false;
        playerDocked = true;
        rootView = inflater.inflate(R.layout.activity_listscreen, container, false);
        parentLayout = (RelativeLayout) rootView;
        dbHandler = new DBHandler(getActivity());

        //Landscape and Portrait parameters for the view containing the YoutubeFragment
        landParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        landParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        landParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        portParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        portParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        portParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        filter = (EditText) rootView.findViewById(R.id.edtFilter);
        filterText = "";
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterText = filter.getText().toString();
                filterText = filterText.toLowerCase();
                if (filterText.equals("muda muda muda")) {
                    FunnyJunk dialog = new FunnyJunk();
                    dialog.show(getActivity().getFragmentManager(), TAG2);
                }
                cleanTable(tableLayout);
                for (Ost ost : allOsts) {
                    addRow(ost);
                }

            }
        };
        filter.addTextChangedListener(textWatcher);
        btnPlayAll = (Button) rootView.findViewById(R.id.btnPlayAll);
        btnplaySelected = (Button) rootView.findViewById(R.id.btnPlaySelected);
        btnStopPlayer = (Button) rootView.findViewById(R.id.btnStopPlayer);
        btnShuffle = (Button) rootView.findViewById(R.id.btnShuffle);
        btnQueue = (Button) rootView.findViewById(R.id.btnQueue);
        btnMovePlayer = (Button) rootView.findViewById(R.id.btnMovePlayer);
        tableLayout = (TableLayout) rootView.findViewById(R.id.tlOstTable);
        flOnTop = (FrameLayout) rootView.findViewById(R.id.flOntop);

        btnPlayAll.setOnClickListener(this);
        btnPlayAll.setOnClickListener(this);
        btnplaySelected.setOnClickListener(this);
        btnStopPlayer.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);
        btnQueue.setOnClickListener(this);

        btnMovePlayer.setOnTouchListener(new View.OnTouchListener() {
                float dx, dy;
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) rootView.getLayoutParams();

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playerDocked = false;
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            dx = event.getRawX() - flOnTop.getX();
                            dy = event.getRawY() - flOnTop.getY();
                        }
                        break;
                        case MotionEvent.ACTION_MOVE: {
                            float setPosX = event.getRawX() - dx;
                            float setPosY = event.getRawY() - dy;
                            boolean xOutsideScreen = setPosX < 0 || setPosX > width - flOnTop.getWidth();
                            boolean yOutsideScreen = setPosY < lParams.topMargin || setPosY > height - flOnTop.getHeight()*1.5 - lParams.topMargin;
                            if( xOutsideScreen && yOutsideScreen){
                                return false;
                            }
                            if (xOutsideScreen) {
                                flOnTop.setY(setPosY);
                            }
                            else if (yOutsideScreen) {
                                flOnTop.setX(setPosX);
                            } else {
                                flOnTop.setX(setPosX);
                                flOnTop.setY(setPosY);
                            }
                            System.out.println("X: " + flOnTop.getX() + ", Y: " + flOnTop.getY());
                            flPosX = flOnTop.getX();
                            flPosY = flOnTop.getY();
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            //your stuff
                        }
                        return true;
                    }
                    return false;
                }
        });

        createList();

        return rootView;
    }

    public void createList() {
        checkBoxes = new ArrayList<>();

        btnDelHeader = (Button) rootView.findViewById(R.id.btnDelHeader);

        btnDelHeader.setOnClickListener(this);
        btnDelHeader.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                ViewGroup ctainer = (ViewGroup) inflater.inflate(R.layout.delete_dialog, null);
                popupWindow = new PopupWindow(ctainer, 600, 400, true);
                popupWindow.showAtLocation(rootView, 1, 0, 0);
                Button btnNo = (Button) ctainer.findViewById(R.id.btnNo);
                Button btnYes = (Button) ctainer.findViewById(R.id.btnYes);

                btnNo.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "I think you mean no :)", Toast.LENGTH_LONG).show();
                        popupWindow.dismiss();
                    }
                });
                return false;
            }
        });

        allOsts = dbHandler.getAllOsts();

        for (Ost ost : allOsts) {
            addRow(ost);
        }
    }

    public void addRow(final Ost ost) {
        //final int id = ost.getId();
        String ostInfoString = ost.getTitle() + " " + ost.getShow() + " " + ost.getTags();
        ostInfoString = ostInfoString.toLowerCase();
        final String title = ost.getTitle();
        String show = ost.getShow();
        String tags = ost.getTags();
        tR = new TableRow(getActivity());
        tR.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        final String url = ost.getUrl();

        TextView label_title = new TextView(getActivity());
        label_title.setText(title);
        label_title.setTextColor(Color.BLACK);
        label_title.setTextSize(rowTextSize);
        label_title.setPadding(5, 5, 5, 5);
        tR.addView(label_title);

        TextView label_show = new TextView(getActivity());
        label_show.setText(show);
        label_show.setTextColor(Color.BLACK);
        label_show.setTextSize(rowTextSize);
        label_show.setPadding(5, 5, 5, 5);
        tR.addView(label_show);

        TextView label_tags = new TextView(getActivity());
        label_tags.setText(tags);
        label_tags.setTextColor(Color.BLACK);
        label_tags.setTextSize(rowTextSize);
        label_tags.setPadding(5, 5, 5, 5);
        tR.addView(label_tags);

        //Launches url when you click the title
        label_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(url);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                initYoutubeFrag();
                youtubeFragment.setVideoId(url);
                updateYoutubeFrag();
            }

        });

        CheckBox checkBox = new CheckBox(getActivity());
        checkBox.setPadding(5, 5, 0, 5);
        checkBox.setTextSize(rowTextSize);
        checkBox.setChecked(false);
        checkBoxes.add(checkBox);

        tR.addView(checkBox);
        //Long press to edit Ost
        tR.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                System.out.println(ost.toString());
                dialog = new AddScreen();
                dialog.show(getFragmentManager(), TAG);
                ostReplaceId = ost.getId();
                dialog.setText(ost);
                dialog.setButtonText("Save");
                editedOst = true;

                //Toast.makeText(getApplicationContext(), " Editing Ost ", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        if (!filterText.equals("") && !ostInfoString.contains(filterText)) {
            //System.out.println(filterText + ostInfoString);
            tR.removeAllViews();
        }
        tableLayout.addView(tR, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
    }

    private void cleanTable(TableLayout table) {
        checkBoxes.clear();

        int childCount = table.getChildCount();

        // Remove all rows except the first one
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnShuffle:{
                if(shuffleActivated){
                    shuffleActivated = false;
                    btnShuffle.setBackgroundResource(R.drawable.shuffle);
                }
                else{
                    shuffleActivated = true;
                    youtubeFragment.shuffle();
                    btnShuffle.setBackgroundResource(R.drawable.shuffle_activated);
                }
                break;

            }
            case R.id.btnPlayAll: {
                List<String> urlList = new ArrayList<>();
                List<Ost> currDispOstList = getCurrDispOstList();
                if(currDispOstList.size() > 0){
                    for (Ost ost : currDispOstList) {
                        urlList.add(ost.getUrl());
                    }

                    if(shuffleActivated){
                        Collections.shuffle(urlList);
                    }
                    initYoutubeFrag();
                    youtubeFragment.setVideoIds(urlList);
                    youtubeFragment.playAll(true);
                    updateYoutubeFrag();
                }
                else{
                    Toast.makeText(getActivity(), "No Osts for this filter", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.btnPlaySelected:{
                int i = 0;
                List<String> playList = new ArrayList<>();
                for (CheckBox box : checkBoxes){
                    //System.out.println(box.isChecked());
                    if(box.isChecked()){
                        //System.out.println("i: " + i);
                        String url = allOsts.get(i).getUrl();
                        playList.add(url);
                        box.setChecked(false);
                    }
                    i++;
                }
                if(playList.size()> 0){
                    if(shuffleActivated){
                        Collections.shuffle(playList);
                    }
                    initYoutubeFrag();
                    youtubeFragment.setVideoIds(playList);
                    youtubeFragment.playAll(true);
                    updateYoutubeFrag();
                }
                break;

            }
            case R.id.btnQueue:{
                if(youtubeFragment != null) {
                int i = 0;
                List<String> playList = new ArrayList<>();
                for (CheckBox box : checkBoxes){
                    if(box.isChecked()){
                        String url = allOsts.get(i).getUrl();
                        playList.add(url);
                        box.setChecked(false);
                    }
                    i++;
                }
                    //youtubeFragment.queueVideos(playList);
                    youtubeFragment.addToQueue(playList);
                    Toast.makeText(getActivity(), "Songs queued", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btnDelHeader: {
                int i = 0;
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        int id = allOsts.get(i).getId();
                        dbHandler.deleteOst(id);
                    }

                    i++;
                }
                refreshList();
                break;
            }
            case R.id.btnStopPlayer: {
                if(!floaterLaunched) {
                    youtubeFragment.pausePlayer();
                    flOnTop.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    public void initYoutubeFrag(){
        if(youtubeFragment == null){
            youtubeFragment = new YoutubeFragment();
        }
    }

    public void launchYoutubeFrag(){
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .add(R.id.flOntop, youtubeFragment)
                .commit();
        flOnTop.setVisibility(View.VISIBLE);
    }

    public void updateYoutubeFrag(){
        if(youtubeFragLaunched){
            flOnTop.setVisibility(View.VISIBLE);
            youtubeFragment.initPlayer();
        }
        else{
            launchYoutubeFrag();
            youtubeFragLaunched = true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        orientation = newConfig.orientation;
        System.out.println(playerDocked);
        if(playerDocked) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                flOnTop.setLayoutParams(landParams);
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                flOnTop.setLayoutParams(portParams);
            }
        }
    }

    public void startOst(String url){
        initYoutubeFrag();
        youtubeFragment.setVideoId(url);
        updateYoutubeFrag();
    }

    public void refreshList(){
        tR.removeAllViews();
        cleanTable(tableLayout);
        allOsts = dbHandler.getAllOsts();
        checkBoxes.clear(); //clear list of Checkboxes
        for (Ost ost : allOsts) {
            addRow(ost);
        }
    }

    public void launchFloater() {
        wm = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        final RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.floater_layout_nobuttons, container, false);
        final Button btnCloseFloater = (Button) rl.findViewById(R.id.btnCloseFloater);
        final FrameLayout flYoutubePlayer = (FrameLayout) rl.findViewById(R.id.flYoutubePlayer);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(600, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.x = 0;
        params.y = 0;
        params.gravity = Gravity.CENTER;

        rl.setBackgroundColor(Color.argb(66, 255, 0, 0));

        parentLayout.removeView(flOnTop);
        flYoutubePlayer.addView(flOnTop);

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

                    default:
                        break;
                }

                return false;
            }
        });

        floaterLaunched = true;
        btnCloseFloater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floaterLaunched = false;
                orientation = getActivity().getResources().getConfiguration().orientation;
                flYoutubePlayer.removeView(flOnTop);
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    flOnTop.setLayoutParams(landParams);
                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    flOnTop.setLayoutParams(portParams);
                }
                wm.removeView(rl);
                parentLayout.addView(flOnTop);
            }
        });
    }

    public int getOstReplaceId(){
        return ostReplaceId;
    }

    public boolean isEditedOst(){
        return editedOst;
    }

    public void isNotEdited(){
        editedOst = false;
    }

    public List<Ost> getCurrDispOstList(){
        if(filterText.equals("")){
            return allOsts;
        }
        List<Ost> currOsts = new ArrayList<>();
        for( Ost ost : allOsts){
            String ostString = ost.getTitle() + ", " + ost.getShow() + ", " + ost.getTags();
            ostString = ostString.toLowerCase();
            if(ostString.contains(filterText)){
                //System.out.println("added");
                currOsts.add(ost);
            }
        }
        return currOsts;
    }
}

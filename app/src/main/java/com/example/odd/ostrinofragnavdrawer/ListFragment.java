package com.example.odd.ostrinofragnavdrawer;

import android.graphics.PixelFormat;
import android.support.v4.app.DialogFragment;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.WINDOW_SERVICE;

public class ListFragment extends Fragment implements FunnyJunk.YareYareListener,
        View.OnClickListener, QueueListener, PlayerListener {

    private boolean editedOst;
    private PopupWindow popupWindow;
    private int ostReplaceId, orientation, previouslyPlayed;
    private List<Ost> allOsts, currOstList;
    private List<CheckBox> checkBoxes;
    private DBHandler dbHandler;
    private EditText filter;
    private String TAG = "OstInfo";
    private String TAG2 = "Jojo";
    private String filterText;
    private TextWatcher textWatcher;
    private TableRow tR;
    public FrameLayout flOnTop;
    public YoutubeFragment youtubeFragment = null;
    private Button btnDelHeader, btnplaySelected, btnStopPlayer, btnShuffle, btnMovePlayer, btnOptions;
    private ImageButton btnShufflePlay, btnAdd;
    boolean youtubeFragLaunched;
    private LayoutInflater inflater;
    private CustomAdapter customAdapter;
    private float flPosX, flPosY;
    private ListView lvOst;
    private ViewGroup container;
    boolean floaterLaunched, shuffleActivated, playerDocked;
    private AddScreen dialog;
    private RelativeLayout parentLayout;
    private WindowManager wm;
    private RelativeLayout.LayoutParams landParams, portParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        shuffleActivated = false;
        playerDocked = true;
        final View rootView = inflater.inflate(R.layout.activity_listscreen, container, false);
        parentLayout = (RelativeLayout) rootView;
        dbHandler = new DBHandler(getActivity());

        lvOst = (ListView) rootView.findViewById(R.id.lvOstList);
        lvOst.findViewById(R.id.btnOptions);
        lvOst.setDivider(null);

        allOsts = dbHandler.getAllOsts();

        customAdapter = new CustomAdapter(getContext(), allOsts, this);
        lvOst.setAdapter(customAdapter);

        lvOst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getActivity(), "Clicked on item " + position, Toast.LENGTH_SHORT).show();
                currOstList = getCurrDispOstList();
                initYoutubeFrag();
                List<String> queueList = new ArrayList<>();
                for (Ost ost : currOstList){
                    queueList.add(ost.getUrl());
                }
                youtubeFragment.initiateQueue(queueList, position);
                updateYoutubeFrag();
                if(currOstList.contains(allOsts.get(previouslyPlayed))) {
                    getViewByPosition(previouslyPlayed, lvOst).setBackgroundResource(R.drawable.white);
                }
                view.setBackgroundResource(R.drawable.greenrect);
                previouslyPlayed = position;
            }
        });

        lvOst.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currOstList = getCurrDispOstList();
                Ost ost = getCurrDispOstList().get(position);
                dialog = new AddScreen();
                dialog.show(getFragmentManager(), TAG);
                ostReplaceId = ost.getId();
                dialog.setText(ost);
                dialog.setButtonText("Save");
                dialog.showDeleteButton();
                editedOst = true;
                return true;
            }
        });

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
                String filterString = s.toString().toLowerCase();
                customAdapter.filter(filterString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterText = filter.getText().toString();
                filterText = filterText.toLowerCase();
                if (filterText.equals("muda muda muda")) {
                    FunnyJunk dialog = new FunnyJunk();
                    dialog.show(getActivity().getFragmentManager(), TAG2);
                }
                customAdapter.filter(s.toString());
                /*ListFragment.this.customAdapter.getFilter().filter(s);
                customAdapter = new CustomAdapter(getContext(), getCurrDispOstList(), ListFragment.this);
                lvOst.setAdapter(customAdapter);*/
                /*cleanTable(tableLayout);
                for (Ost ost : allOsts) {
                    addRow(ost);
                }*/
            }
        };
        filter.addTextChangedListener(textWatcher);
        btnShuffle = (Button)  rootView.findViewById(R.id.btnShuffle);
        btnShufflePlay = (ImageButton) rootView.findViewById(R.id.btnShufflePlay);
        btnAdd = (ImageButton)  rootView.findViewById(R.id.btnAdd);
        btnStopPlayer = (Button) rootView.findViewById(R.id.btnStopPlayer);
        btnMovePlayer = (Button) rootView.findViewById(R.id.btnMovePlayer);
        flOnTop = (FrameLayout) rootView.findViewById(R.id.flOntop);

        btnShuffle.setOnClickListener(this);
        btnShufflePlay.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnStopPlayer.setOnClickListener(this);
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

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnShuffle:{
                if(youtubeFragment == null){
                    Toast.makeText(getActivity(), "Player is not running", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(shuffleActivated){
                    shuffleActivated = false;
                    youtubeFragment.shuffleOff();
                    btnShuffle.setBackgroundResource(R.drawable.shuffle);
                }
                else{
                    shuffleActivated = true;
                    youtubeFragment.shuffleOn();
                    btnShuffle.setBackgroundResource(R.drawable.shuffle_activated);
                }
                break;

            }
            case R.id.btnShufflePlay:{
                Random rnd = new Random();
                currOstList = getCurrDispOstList();
                int rndPos = rnd.nextInt(currOstList.size());
                initYoutubeFrag();
                List<String> queueList = new ArrayList<>();
                for (Ost ost : currOstList){
                    queueList.add(ost.getUrl());
                }
                youtubeFragment.initiateQueue(queueList, rndPos);
                updateYoutubeFrag();
                customAdapter.updateCurrentlyPlaying(rndPos);
                refreshListView();
                youtubeFragment.shuffleOn();
                previouslyPlayed = rndPos;
                break;
            }

            case R.id.btnAdd:{
                AddScreen dialog = new AddScreen();
                dialog.show(getFragmentManager(), TAG);
                dialog.setButtonText("Add");
                editedOst = false;
                break;
            }
            /*
            case R.id.btnPlayAll: {
                List<String> urlList = new ArrayList<>();
                List<Ost> currDispOstList = getCurrDispOstList();
                if(currDispOstList.size() > 0){
                    for (Ost ost : currDispOstList) {
                        urlList.add(ost.getUrl());
                    }

                    if(shuffleActivated){
                        Collections.shuffleOn(urlList);
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
                        Collections.shuffleOn(playList);
                    }
                    initYoutubeFrag();
                    youtubeFragment.setVideoIds(playList);
                    youtubeFragment.playAll(true);
                    updateYoutubeFrag();
                }
                break;

            }
            case R.id.btnQueue:{
                List<String> queueList = new ArrayList<>();
                int i = 0;
                for (Ost ost : currOstList){
                    if(i > clickedPos){
                        queueList.add(ost.getUrl());
                    }
                    i++;
                }
                System.out.println(queueList);
                /*if(youtubeFragment != null) {
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
            */
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
            PlayerListener[] playerListeners = new PlayerListener[2];
            playerListeners[0] = customAdapter;
            playerListeners[1] = this;
            youtubeFragment.setPlayerListener(playerListeners);
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
                flOnTop.invalidate();
                flOnTop.setLayoutParams(portParams);
            }
        }
    }

    public void startOst(String url){
        initYoutubeFrag();
        youtubeFragment.setVideoId(url);
        updateYoutubeFrag();
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
                currOsts.add(ost);
            }
        }
        return currOsts;
    }

    @Override
    public void addToQueue(int addId) {
        Ost ost = getCurrDispOstList().get(addId);
        youtubeFragment.addToQueue(ost.getUrl());
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void updateCurrentlyPlaying(int newId){
        getViewByPosition(newId, lvOst).setBackgroundResource(R.drawable.greenrect);
        getViewByPosition(previouslyPlayed, lvOst).setBackgroundResource(R.drawable.white);
        previouslyPlayed = newId;
    }

    public void refreshListView(){
        editedOst = false;
        allOsts = dbHandler.getAllOsts();
        currOstList = getCurrDispOstList();
        customAdapter.updateList(currOstList);
        System.out.println("heyooooooooo");
    }

    public AddScreen getDialog(){
        return dialog;
    }
}

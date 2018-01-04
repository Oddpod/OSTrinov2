package com.odd.ostrinov2;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.odd.ostrinov2.dialogFragments.AddScreen;
import com.odd.ostrinov2.listeners.PlayerListener;
import com.odd.ostrinov2.listeners.QueueListener;
import com.odd.ostrinov2.tools.DBHandler;
import com.odd.ostrinov2.tools.IOHandler;
import com.odd.ostrinov2.tools.PermissionHandlerKt;
import com.odd.ostrinov2.tools.UtilMeths;
import com.odd.ostrinov2.tools.YoutubeShare;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AddScreen.AddScreenListener,
        DialogInterface.OnDismissListener, QueueListener,
        View.OnClickListener {

    private final static String PREFS_NAME= "Saved queue";
    private DBHandler db;
    private Ost unAddedOst;
    private int backPress;
    private ListFragment listFragment;
    private SearchFragment searchFragment;
    private FrameLayout floatingPlayer;
    private RelativeLayout rlContent;
    private boolean youtubePlayerLaunched = false, about = false, addCanceled = true, ostFromWidget = false;
    private QueueAdapter queueAdapter;
    private FragmentManager manager;
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private Boolean mIsBound = false, shuffleActivated = false, repeat = false, playing = false,
                lastSessionLoaded = false;
    private YTplayerService yTplayerService;
    private ImageButton btnRepeat, btnPlayPause, btnShuffle;
    private SeekBar seekBar;
    private Runnable seekbarUpdater;
    private Handler handler = new Handler();
    private SearchView searchView = null;
    private String lastQuery = "Pokemon Ost";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DBHandler(this);
        unAddedOst = null;
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        floatingPlayer = (FrameLayout) findViewById(R.id.floatingPlayer);

        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnPlayPause = (ImageButton) findViewById(R.id.btnPause);
        final ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
        ImageButton btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);

        btnRepeat.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        //Make sure you update Seekbar on UI thread

        RecyclerView rvQueue = (RecyclerView) findViewById(R.id.rvQueue);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvQueue.setLayoutManager(mLayoutManager);
        queueAdapter = new QueueAdapter(this, this);
        rvQueue.setAdapter(queueAdapter);
        rvQueue.setItemAnimator(new DefaultItemAnimator());

        listFragment = new ListFragment();
        listFragment.setMainAcitivity(this);
        listFragment.setRetainInstance(true);
        manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.rlListContainer, listFragment)
                .addToBackStack("list")
                .commit();

        searchFragment = new SearchFragment();
        searchFragment.setMainActivity(this);
        searchFragment.setRetainInstance(true);
        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.floatingPlayer, youTubePlayerFragment).commit();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bnvFrag);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                switch (item.getItemId()){
                    case R.id.nav_barLibrary:{
                        manager.beginTransaction()
                                .replace(R.id.rlListContainer, listFragment)
                                .addToBackStack("list")
                                .commit();
                        break;
                    }
                    case R.id.nav_barSearch:{
                        manager.beginTransaction()
                                .replace(R.id.rlListContainer, searchFragment)
                                .addToBackStack("search")
                                .commit();
                        if(!searchFragment.isFromBackStack()){
                            searchFragment.performSearch(lastQuery);
                        }
                        break;
                    }
                    case R.id.nav_barPlaylist:{
                        Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                return false;
            }
        });

        Intent intent = getIntent();
        int ostId = intent.getIntExtra(getString(R.string.label_ost_of_the_day), -1);
        if (ostId != -1) {
            ostFromWidget = true;
        } else {
            addOstLink(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (about) {
            super.onBackPressed();
            about = false;
        } else {
            backPress += 1;
            Toast.makeText(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();

            if (backPress > 1) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setQueryHint("Filter");

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("onQueryTextChange", newText);
                    /*listFragment.getCustomAdapter().filter(newText);
                    MemesKt.launchMeme(newText, MainActivity.this);*/

                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);
                    manager.beginTransaction()
                            .replace(R.id.rlListContainer, searchFragment)
                            .addToBackStack("search")
                            .commit();
                    searchFragment.performSearch(query);
                    about = true;
                    lastQuery = query;

                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings: {
                Toast.makeText(this, "Nothing here yet", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.action_about: {
                AboutFragment aboutFragment = new AboutFragment();
                manager.beginTransaction()
                        .replace(R.id.rlListContainer, aboutFragment)
                        .addToBackStack("about")
                        .commit();
                about = true;
                break;
            }

            case R.id.share: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (!youtubePlayerLaunched) {
                    Toast.makeText(this, "Nothing is playing", Toast.LENGTH_SHORT).show();
                } else {
                    Ost ost = yTplayerService.getQueueHandler().getCurrPlayingOst();
                    ClipData clip = ClipData.newPlainText("Ost url", ost.getUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Link Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.hide_SearchBar: {
                if (listFragment.tlTop.getVisibility() == View.GONE) {
                    listFragment.tlTop.setVisibility(View.VISIBLE);
                } else {
                    listFragment.tlTop.setVisibility(View.GONE);
                }
                break;
            }

            case R.id.import_osts: {
                UtilMeths.INSTANCE.chooseFileImport(this);
                break;
            }

            case R.id.export_osts: {
                UtilMeths.INSTANCE.chooseFileExport(this);
                break;
            }

            case R.id.delete_allOsts: {
                db.emptyTable();
                listFragment.refreshListView();
                break;
            }

            case R.id.refresh_tagsTable: {
                /*SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                String CREATE_TAGS_TABLE = "CREATE TABLE " + "tagsTable" + "("
                        + "tagid" + " INTEGER PRIMARY KEY,"
                        + "tag" + " TEXT " + ")";
                sqLiteDatabase.execSQL(CREATE_TAGS_TABLE);

                String CREATE_SHOW_TABLE = "CREATE TABLE " + "showTable" + "("
                        + "showid" + " INTEGER PRIMARY KEY,"
                        + "show" + " TEXT " + ")";
                sqLiteDatabase.execSQL(CREATE_SHOW_TABLE);*/
                db.reCreateTagsAndShowTables();
                break;
            }
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveButtonClick(DialogFragment dialog) {
        MultiAutoCompleteTextView entTags = (MultiAutoCompleteTextView) dialog.getDialog().findViewById(R.id.mactvTags);
        AutoCompleteTextView entShow = (AutoCompleteTextView) dialog.getDialog().findViewById(R.id.actvShow);
        EditText entTitle = (EditText) dialog.getDialog().findViewById(R.id.edtTitle);
        EditText entUrl = (EditText) dialog.getDialog().findViewById(R.id.edtUrl);

        String title = entTitle.getText().toString();
        String show = entShow.getText().toString();
        String tags = entTags.getText().toString();
        String url = entUrl.getText().toString();

        PermissionHandlerKt.checkPermission(this);
        Ost lastAddedOst = new Ost(title, show, tags, url);
        lastAddedOst.setId(listFragment.getOstReplaceId());
        boolean alreadyAdded = db.checkiIfOstInDB(lastAddedOst);
        if (listFragment.isEditedOst()) {
            db.updateOst(lastAddedOst);
            listFragment.refreshListView();
            UtilMeths.INSTANCE.downloadThumbnail(url, this);
            addCanceled = false;
        } else if (!alreadyAdded) {
            if (!url.contains("https://")) {
                Toast.makeText(this, "You have to put in a valid youtube link", Toast.LENGTH_SHORT).show();
            } else {
                db.addNewOst(lastAddedOst);
                Toast.makeText(getApplicationContext(), lastAddedOst.getTitle() + " added", Toast.LENGTH_SHORT).show();
                listFragment.refreshListView();
                UtilMeths.INSTANCE.downloadThumbnail(url, this);
                addCanceled = false;
            }
        } else {
            Toast.makeText(this, lastAddedOst.getTitle() + " From " + lastAddedOst.getShow() + " has already been added", Toast.LENGTH_SHORT).show();
            //lastAddedOst = null;
        }
    }

    @Override
    public void onDeleteButtonClick(DialogFragment dialog) {
        db.deleteOst(listFragment.getOstReplaceId());
        listFragment.getCustomAdapter().removeOst(listFragment.getOstReplacePos());
        listFragment.refreshListView();
        Toast.makeText(this, "Deleted " + listFragment.getDialog().getFieldData()[0], Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        String[] fieldData = getDialog().getFieldData();

        String title = fieldData[0];
        String show = fieldData[1];
        String tags = fieldData[2];
        String url = fieldData[3];

        if(addCanceled){
            unAddedOst = new Ost(title, show, tags, url);
            listFragment.setUnAddedOst(unAddedOst);
        }
        else{
            addCanceled = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionHandlerKt.checkPermission(this);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            IOHandler.INSTANCE.readFromFile(currFileURI, this);
            listFragment.refreshListView();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            try {
                IOHandler.INSTANCE.writeToFile(currFileURI, db.getAllOsts(), this);
                listFragment.refreshListView();

            } catch (IOException e) {
                Log.i("OnActivityResult", " caught IOexception");
            }
        }
        if (requestCode == 3) {
            yTplayerService.launchFloater(floatingPlayer, this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public AddScreen getDialog() {
        return listFragment.getDialog();
    }

    public void addToQueue(Ost ost) {
        if (!youtubePlayerLaunched) {
            Toast.makeText(this, "You have to play something first", Toast.LENGTH_SHORT).show();
        } else {
            yTplayerService.getQueueHandler().addToQueue(ost);
            queueAdapter.addToQueue(ost);
        }

    }

    public void initPlayerService() {
        if (yTplayerService == null) {
            startService();
            doBindService();
        }
    }

    public void initiatePlayer(List<Ost> ostList, int startId) {
        queueAdapter.initiateQueue(ostList, startId);

        if (!youtubePlayerLaunched) {
            initiateSeekbarTimer();
            rlContent.removeView(floatingPlayer);
            youtubePlayerLaunched = true;
            PlayerListener[] playerListeners = new PlayerListener[2];
            playerListeners[0] = queueAdapter;
            playerListeners[1] = listFragment.getCustomAdapter();
            yTplayerService.launchFloater(floatingPlayer, this);
            yTplayerService.startQueue(ostList, startId, shuffleActivated,
                    playerListeners, youTubePlayerFragment);
        } else {
            yTplayerService.initiateQueue(ostList, startId, shuffleActivated);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (!youtubePlayerLaunched) {
            Toast.makeText(this, "Play something first bruh :)", Toast.LENGTH_SHORT).show();
        } else {
            switch (id) {
                case R.id.btnRepeat:{
                    if(!repeat){
                        yTplayerService.setRepeating(true);
                        btnRepeat.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                        repeat = true;
                    }else{
                        yTplayerService.setRepeating(false);
                        btnRepeat.clearColorFilter();
                        repeat = false;
                    }
                    break;
                }

                case R.id.btnPause: {
                    yTplayerService.pausePlay();
                    break;
                }

                case R.id.btnNext: {
                    yTplayerService.playerNext();
                    break;
                }

                case R.id.btnPrevious: {
                    yTplayerService.playerPrevious();
                    break;
                }

                case R.id.btnShuffle:{
                    if(shuffleActivated){
                        shuffleOff();
                        shuffleActivated = false;
                    }
                    else{
                        shuffleOn();
                        shuffleActivated = true;
                    }
                    break;
                }
            }
        }
    }

    public void pausePlay(Boolean playing) {
        this.playing = playing;
        if (playing) {
            btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }

    }

    public void shuffleOn() {
        yTplayerService.getQueueHandler().shuffleOn();
        shuffleActivated = true;
        btnShuffle.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
    }

    public void shuffleOff() {
        shuffleActivated = false;
        yTplayerService.getQueueHandler().shuffleOff();
        btnShuffle.clearColorFilter();
    }

    @Override
    public void addToQueue(int addId) {

    }

    @Override
    public void removeFromQueue(String url) {
        yTplayerService.getQueueHandler().removeFromQueue(url);
    }

    public void youtubePlayerStopped() {
        youtubePlayerLaunched = false;
        handler.removeCallbacks(seekbarUpdater);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                }
                return;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        Boolean autoRotate = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
        if (autoRotate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(MainActivity.this, YTplayerService.class);
        serviceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            yTplayerService = ((YTplayerService.LocalBinder) service).getService();
            yTplayerService.registerBroadcastReceiver();
            if(ostFromWidget){
                startWidgetOst();
            } else if(!lastSessionLoaded) {
                loadLastSession();
            }
            // Tell the user about this for our demo.
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mIsBound = false;
        }
    };

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this,
                YTplayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        //mCallbackText.setText("Binding.");
    }

    public void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            /*if (yTplayerService != null) {
                try {
                    Message msg = Message.obtain(null,
                            MessengerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }*/

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (youtubePlayerLaunched) {
            yTplayerService.refresh();
            saveSession();
        }
        handler.removeCallbacks(seekbarUpdater);
        doUnbindService();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        doBindService();
        if (!youtubePlayerLaunched) {
            initPlayerService();
        } else{
            handler.postDelayed(seekbarUpdater, 1000);
        }
    }

    private void addOstLink(Intent intent){
        if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getType().equals("text/plain")){
            Bundle extras = intent.getExtras();
            String link = extras.getString(Intent.EXTRA_TEXT);
            new YoutubeShare(this, link);
            Toast.makeText(this, "Added " + link + "to your OST library", Toast.LENGTH_SHORT).show();
            Intent result = new Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"));
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        int ostId = intent.getIntExtra(getString(R.string.label_ost_of_the_day), -1);

        if (ostId != -1) {
            initiatePlayer(db.getAllOsts(), ostId);
        }else{
            addOstLink(intent);
        }
        super.onNewIntent(intent);
    }

    void startWidgetOst(){
        Intent widgetIntent = getIntent();
        int startId = widgetIntent.getIntExtra(getString(R.string.label_ost_of_the_day), -1);
        if(startId != -1){
            initiatePlayer(db.getAllOsts(), startId);
        }
    }

    private void initiateSeekbarTimer(){
        final int interval = 1000; // 1 Second
        seekbarUpdater = new Runnable() {
            public void run() {
                if (youtubePlayerLaunched && playing) {
                    seekBar.setProgress(yTplayerService.getPlayer().getCurrentTimeMillis());
                }
                handler.postDelayed(seekbarUpdater, interval);
            }
        };

        handler.postAtTime(seekbarUpdater, System.currentTimeMillis() + interval);
        handler.postDelayed(seekbarUpdater, interval);
    }

    public void setSeekBarProgress(int progress){
        seekBar.setProgress(progress);
    }

    public SeekBar getSeekBar(){
        return seekBar;
    }

    private void loadLastSession(){
        SharedPreferences lastSessionPrefs = getSharedPreferences(PREFS_NAME, 0);
        String queueString = lastSessionPrefs.getString("lastSession", "");
        int timestamp = lastSessionPrefs.getInt("timeStamp", 0);
        int lastCurr = lastSessionPrefs.getInt("lastCurrPlaying", 0);
        int videoDuration = lastSessionPrefs.getInt("videoDuration", 0);
        if(!queueString.equals("")){
            Log.d("lastQueue", queueString);
            List<Ost> lastQueueList = UtilMeths.INSTANCE.buildOstListFromQueue(queueString, db);
            initiatePlayer(lastQueueList, lastCurr);
            yTplayerService.getPlayerHandler().loadLastSession(true, timestamp, videoDuration);
        }
        lastSessionLoaded = true;
    }

    public void saveSession(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        StringBuilder stringBuilder = new StringBuilder();
        for (Ost ost: yTplayerService.getQueueHandler().getOstList()
                ) {
            if(ost.getId() == 0){ //appends the url of the ost since it has been added from search
                stringBuilder.append(ost.getUrl()).append(",");
            }
            else{
                stringBuilder.append(ost.getId()).append(",");
            }
        }
        String idString = stringBuilder.toString();
        editor.putString("lastSession", idString);
        YouTubePlayer youTubePlayer = yTplayerService.getPlayer();
        int lastCurrentlyPlaying = yTplayerService.getQueueHandler().getCurrPlayingIndex();
        editor.putInt("lastCurrPlaying", lastCurrentlyPlaying);
        editor.putInt("timeStamp", youTubePlayer.getCurrentTimeMillis());
        editor.putInt("videoDuration", youTubePlayer.getDurationMillis());

        // Commit the edits!
        Boolean success = editor.commit();
        String successString = success.toString();
        Log.i("Wrote session", successString);
    }
}

package com.odd.ostrinov2;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.odd.ostrinov2.asynctasks.YParsePlaylist;
import com.odd.ostrinov2.dialogFragments.AddOstDialog;
import com.odd.ostrinov2.fragmentsLogic.AboutFragment;
import com.odd.ostrinov2.fragmentsLogic.LibraryFragment;
import com.odd.ostrinov2.fragmentsLogic.PlaylistFragment;
import com.odd.ostrinov2.fragmentsLogic.SearchFragment;
import com.odd.ostrinov2.services.YTplayerService;
import com.odd.ostrinov2.tools.DBHandler;
import com.odd.ostrinov2.tools.IOHandler;
import com.odd.ostrinov2.tools.PagerAdapter;
import com.odd.ostrinov2.tools.PermissionHandlerKt;
import com.odd.ostrinov2.tools.UtilMeths;
import com.odd.ostrinov2.tools.YoutubeShare;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddOstDialog.AddDialogListener, View.OnClickListener {

    private final static String PREFS_NAME = "Saved queue";
    private static DBHandler dbHandler;
    private static boolean shoudlRefreshList = false;
    private int backPress;
    private LibraryFragment libraryFragment;
    private SearchFragment searchFragment;
    private PlaylistFragment playlistFragment;
    private FrameLayout floatingPlayer;
    private RelativeLayout rlContent;
    private boolean youtubePlayerLaunched = false, about = false, ostFromWidget = false;
    private int ostFromWidgetId;
    private QueueAdapter queueAdapter;
    private FragmentManager manager;
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private Boolean mIsBound = false;
    private Boolean shuffleActivated = false;
    private Boolean repeat = false;
    private Boolean playing = false;
    private Boolean lastSessionLoaded = false;
    private YTplayerService yTplayerService;
    private ImageButton btnRepeat, btnPlayPause, btnShuffle;
    private SeekBar seekBar;
    private Runnable seekbarUpdater;
    private Handler handler = new Handler();
    private SearchView searchView = null;
    private ViewPager fragPager;

    private static Runnable permissionCallback;

    public static void setPermissionCallback(Runnable permissionCallback) {
        MainActivity.permissionCallback = permissionCallback;
    }

    public static void setShoudlRefreshList(boolean shoudlRefreshList) {
        MainActivity.shoudlRefreshList = shoudlRefreshList;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            yTplayerService = ((YTplayerService.LocalBinder) service).getService();
            if (ostFromWidget) {
                startWidgetOst(ostFromWidgetId);
                ostFromWidget = false;
                shuffleOn();
                System.out.println("ost from widget");
            } else if (!lastSessionLoaded) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHandler = new DBHandler(this);
        //dbHandler.recreateDatabase();
        rlContent = findViewById(R.id.rlContent);
        fragPager = findViewById(R.id.frag_pager);

        checkAutorotate();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        floatingPlayer = findViewById(R.id.floatingPlayer);

        btnRepeat = findViewById(R.id.btnRepeat);
        btnPlayPause = findViewById(R.id.btnPause);
        final ImageButton btnNext = findViewById(R.id.btnNext);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);

        btnRepeat.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);

        seekBar = findViewById(R.id.seekBar);
        //Make sure you update Seekbar on UI thread

        RecyclerView rvQueue = findViewById(R.id.rvQueue);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvQueue.setLayoutManager(mLayoutManager);
        queueAdapter = new QueueAdapter(this);
        rvQueue.setAdapter(queueAdapter);
        rvQueue.setItemAnimator(new DefaultItemAnimator());

        libraryFragment = new LibraryFragment();
        libraryFragment.setMainAcitivity(this);
        libraryFragment.setRetainInstance(true);
        manager = getSupportFragmentManager();

        searchFragment = new SearchFragment();
        searchFragment.setMainActivity(this);
        searchFragment.setRetainInstance(true);
        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.floatingPlayer, youTubePlayerFragment).commit();

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bnvFrag);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                switch (item.getItemId()) {
                    case R.id.nav_barLibrary: {
                        fragPager.setCurrentItem(0);
                        break;
                    }
                    case R.id.nav_barSearch: {
                        fragPager.setCurrentItem(1);
                        break;
                    }
                    case R.id.nav_barPlaylist: {
                        fragPager.setCurrentItem(2);
                        break;
                    }
                }
                return false;
            }
        });

        playlistFragment = new PlaylistFragment();
        playlistFragment.applicationContext = getApplicationContext();
        List<Fragment> frags = new ArrayList<Fragment>() {{
            add(libraryFragment);
            add(searchFragment);
            add(playlistFragment);
        }};
        PagerAdapter adapter = new PagerAdapter(manager, frags);

        fragPager.setAdapter(adapter);
        fragPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            MenuItem prevMenuItem;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
                prevMenuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //
            }
        });

        Intent intent = getIntent();
        handleIntent(intent);
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
            searchView.setQueryHint("Search");

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);
                    fragPager.setCurrentItem(1);
                    searchFragment.performSearch(query);
                    about = true;
                    //lastQuery = query;

                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (about) {
            super.onBackPressed();
            about = false;
        } else if (playlistFragment.isViewingPlaylist() && fragPager.getCurrentItem() == 2) {
            playlistFragment.resetAdapter();
        } else if (searchFragment.isInPlaylist() && fragPager.getCurrentItem() == 1) {
            searchFragment.backPress();
        } else {
            backPress += 1;
            Toast.makeText(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();

            if (backPress > 1) {
                super.onBackPressed();
            }
        }
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
                        .replace(R.id.frag_pager, aboutFragment)
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
                    Ost ost = yTplayerService.getQueueHandler().getCurrentlyPlaying();
                    ClipData clip = ClipData.newPlainText("Ost id", ost.getUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Link Copied to Clipboard", Toast.LENGTH_SHORT).show();
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
                dbHandler.recreateDatabase(dbHandler.getWritableDatabase());
                libraryFragment.refreshListView();
                break;
            }

            case R.id.refresh_tagsTable: {
                /*SQLiteDatabase sqLiteDatabase = dbHandler.getWritableDatabase();
                String CREATE_TAGS_TABLE = "CREATE TABLE " + "tagsTable" + "("
                        + "tagid" + " INTEGER PRIMARY KEY,"
                        + "tag" + " TEXT " + ")";
                sqLiteDatabase.execSQL(CREATE_TAGS_TABLE);

                String CREATE_SHOW_TABLE = "CREATE TABLE " + "showTable" + "("
                        + "showid" + " INTEGER PRIMARY KEY,"
                        + "show" + " TEXT " + ")";
                sqLiteDatabase.execSQL(CREATE_SHOW_TABLE);*/
                dbHandler.reCreateTagsAndShowTables();
                break;
            }
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Runnable callback = new Runnable() {
            @Override
            public void run() {
                onRequestCode(requestCode, resultCode, data);
            }
        };
        PermissionHandlerKt.checkPermission(this, callback);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onRequestCode(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            IOHandler.INSTANCE.readFromFile(currFileURI, this);
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            IOHandler.INSTANCE.writeToFile(currFileURI, dbHandler.getAllOsts(), this);
        }

        if (requestCode == 3) {
            yTplayerService.launchFloater(floatingPlayer, this);
        }
    }

    public void initPlayerService() {
        if (yTplayerService == null) {
            startService();
            doBindService();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (!youtubePlayerLaunched) {
            Toast.makeText(this, "Play something first bruh :)", Toast.LENGTH_SHORT).show();
        } else {
            switch (id) {
                case R.id.btnRepeat: {
                    if (!repeat) {
                        yTplayerService.setRepeating(true);
                        btnRepeat.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                        repeat = true;
                    } else {
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

                case R.id.btnShuffle: {
                    if (shuffleActivated) {
                        shuffleOff();
                    } else {
                        shuffleOn();
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

    public void youtubePlayerStopped() {
        youtubePlayerLaunched = false;
        handler.removeCallbacks(seekbarUpdater);
    }

    public void youtubePlayerLaunched() {
        youtubePlayerLaunched = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_READWRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissionCallback != null) {
                        permissionCallback.run();
                    }
                } else {
                    PermissionHandlerKt.launchReadWriteExternalNotGrantedDialog(this);
                }
                break;
            }
            case Constants.REQUEST_SYSTEM_OVERLAY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissionCallback != null) {
                        permissionCallback.run();
                    }
                } else {
                    Toast.makeText(this, "I'm sorry, can't play anything without this request :C",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkAutorotate();
    }

    private void checkAutorotate() {
        Boolean autoRotate = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
        if (autoRotate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(MainActivity.this, YTplayerService.class);
        serviceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    public void initiatePlayer(List<Ost> ostList, int startId) {
        if (!youtubePlayerLaunched) {
            initiateSeekbarTimer();
            rlContent.removeView(floatingPlayer);
            yTplayerService.launchFloater(floatingPlayer, this);
            yTplayerService.startQueue(ostList, startId, shuffleActivated,
                    libraryFragment.getLibListAdapter(), queueAdapter, youTubePlayerFragment);
        } else {
            yTplayerService.initiateQueue(ostList, startId, shuffleActivated);
        }
    }

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
    public void onStart() {
        if (shoudlRefreshList) {
            libraryFragment.refreshListView();
            shoudlRefreshList = false;
        }
        super.onStart();
        checkAutorotate();
        doBindService();
        if (!youtubePlayerLaunched) {
            initPlayerService();
        } else {
            handler.postDelayed(seekbarUpdater, 1000);
        }
    }

    private void addOstLink(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getType().equals("text/plain")) {
            Bundle extras = intent.getExtras();
            String link = extras.getString(Intent.EXTRA_TEXT);
            if (link.contains("playlist")) {

                String pListName = link.split(":")[0];
                String pListId = link.split("list=")[1];
                YParsePlaylist yPP = new YParsePlaylist(pListId, pListName, this);
                yPP.execute();
            } else {
                YoutubeShare yShare = new YoutubeShare(link);
                yShare.setContext(this);
                yShare.execute();
            }
            Intent result = new Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"));
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        if (ostFromWidget) {
            startWidgetOst(ostFromWidgetId);
            ostFromWidget = false;
            shuffleOn();
        }
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String intAction = intent.getAction();
        if (intAction == null) {
            return;
        }
        switch (intAction) {
            case Constants.NOT_OPEN_ACTIVITY_ACTION: {
                // Does nothing so far
                break;
            }
            case Constants.START_OST: {
                ostFromWidget = true;
                ostFromWidgetId = intent.getIntExtra(getString(R.string.label_ost_of_the_day),
                        -1);
                break;
            }
            case Intent.ACTION_SEND: {
                addOstLink(intent);
                break;
            }
            case Constants.INITPLAYER: {
                final List<Ost> osts = intent.getParcelableArrayListExtra("osts_extra");
                int startPos = intent.getIntExtra("startIndex", 0);
                initiatePlayer(osts, startPos);
                break;
            }
            default:
                break;
        }
    }

    void startWidgetOst(int startId) {
        if (startId != -1) {
            if (dbHandler.getAllOsts().isEmpty()) {
                Toast.makeText(this, "Uh oh, It seems your library is empty :C",
                        Toast.LENGTH_SHORT).show();
            } else {
                initiatePlayer(dbHandler.getAllOsts(), startId);
            }
        }
    }

    private void initiateSeekbarTimer() {
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

    public void setSeekBarProgress(int progress) {
        seekBar.setProgress(progress);
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    private void loadLastSession() {
        SharedPreferences lastSessionPrefs = getSharedPreferences(PREFS_NAME, 0);
        String queueString = lastSessionPrefs.getString("lastSession", "");
        int timestamp = lastSessionPrefs.getInt("timeStamp", 0);
        int lastCurr = lastSessionPrefs.getInt("lastCurrPlaying", 0);
        int videoDuration = lastSessionPrefs.getInt("videoDuration", 0);
        if (!queueString.equals("")) {
            Log.d("lastQueue", queueString);
            List<Ost> lastQueueList = UtilMeths.INSTANCE.buildOstListFromQueue(queueString, dbHandler);
            if (!lastQueueList.isEmpty() && lastCurr < lastQueueList.size()) {
                initiatePlayer(lastQueueList, lastCurr);
                yTplayerService.getPlayerHandler().loadLastSession(true, timestamp, videoDuration);
            }
        }
        lastSessionLoaded = true;
    }

    public void saveSession() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        StringBuilder stringBuilder = new StringBuilder();
        for (Ost ost : yTplayerService.getQueueHandler().getOstList()
                ) {
            if (ost.getId() == 0) { //appends the id of the ost since it has been added from search
                stringBuilder.append(ost.getVideoId()).append(",");
            } else {
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

    public static DBHandler getDbHandler() {
        return dbHandler;
    }

    public LibraryFragment getLibraryFragment() {
        return libraryFragment;
    }

    @Override
    public void onAddButtonClick(@NotNull final Ost ostToAdd, @NotNull DialogFragment dialog) {
        addnewOst(ostToAdd);
    }

    private void addnewOst(Ost ostToAdd) {
        boolean alreadyAdded = dbHandler.checkiIfOstInDB(ostToAdd);
        if (!alreadyAdded) {
            dbHandler.addNewOst(ostToAdd);
            Toast.makeText(getApplicationContext(), ostToAdd.getTitle() + " added",
                    Toast.LENGTH_SHORT).show();
            libraryFragment.addOst(ostToAdd);
        } else {
            Toast.makeText(this, ostToAdd.getTitle() + " From " + ostToAdd.getShow()
                    + " has already been added", Toast.LENGTH_SHORT).show();
            //lastAddedOst = null;
        }
    }
}

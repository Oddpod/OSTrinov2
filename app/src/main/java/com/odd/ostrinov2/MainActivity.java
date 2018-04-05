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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.odd.ostrinov2.dialogFragments.AddScreen;
import com.odd.ostrinov2.fragmentsLogic.AboutFragment;
import com.odd.ostrinov2.fragmentsLogic.ListFragment;
import com.odd.ostrinov2.fragmentsLogic.SearchFragment;
import com.odd.ostrinov2.services.YTplayerService;
import com.odd.ostrinov2.tools.DBHandler;
import com.odd.ostrinov2.tools.IOHandler;
import com.odd.ostrinov2.tools.PagerAdapter;
import com.odd.ostrinov2.tools.PermissionHandlerKt;
import com.odd.ostrinov2.tools.QueueHandler;
import com.odd.ostrinov2.tools.SeekBarHandler;
import com.odd.ostrinov2.tools.UtilMeths;
import com.odd.ostrinov2.tools.YoutubeShare;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddScreen.AddScreenListener, DialogInterface.OnDismissListener, View.OnClickListener {

    private static DBHandler dbHandler;
    private Ost unAddedOst;
    private int backPress;
    private QueueHandler queueHandler;
    private ListFragment listFragment;
    private SearchFragment searchFragment;
    private boolean youtubePlayerLaunched = false, about = false, addCanceled = true, ostFromWidget = false;
    private int ostFromWidgetId;
    private QueueAdapter queueAdapter;
    private FragmentManager manager;
    private Boolean mIsBound = false, shuffleActivated = false, repeat = false, isPlaying = false,
            lastSessionLoaded = false;
    private YTplayerService yTplayerService;
    private ImageButton btnRepeat, btnPlayPause, btnShuffle;
    public SeekBar seekBar;
    private static Runnable seekbarUpdater;
    public static Handler handler = new Handler();
    private SearchView searchView = null;
    private ViewPager fragPager;
    private SeekBarHandler seekBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHandler = new DBHandler(this);
        //dbHandler.emptyTable();
        unAddedOst = null;
        fragPager = (ViewPager) findViewById(R.id.frag_pager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

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
        queueAdapter = new QueueAdapter(this);
        rvQueue.setAdapter(queueAdapter);
        rvQueue.setItemAnimator(new DefaultItemAnimator());

        listFragment = new ListFragment();
        listFragment.setMainAcitivity(this);
        listFragment.setRetainInstance(true);
        manager = getSupportFragmentManager();

        searchFragment = new SearchFragment();
        searchFragment.setMainActivity(this);
        searchFragment.setRetainInstance(true);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bnvFrag);
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
                        Toast.makeText(getApplicationContext(), "Not implemented", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                return false;
            }
        });

        List<Fragment> frags = new ArrayList<Fragment>() {{
            add(listFragment);
            add(searchFragment);
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
                dbHandler.emptyTable();
                listFragment.refreshListView();
                break;
            }

            case R.id.refresh_tagsTable: {
                dbHandler.reCreateTagsAndShowTables();
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
        boolean alreadyAdded = dbHandler.checkiIfOstInDB(lastAddedOst);
        if (listFragment.isEditedOst()) {
            dbHandler.updateOst(lastAddedOst);
            listFragment.refreshListView();
            UtilMeths.INSTANCE.downloadThumbnail(url, this);
            addCanceled = false;
        } else if (!alreadyAdded) {
            if (!url.contains("https://")) {
                Toast.makeText(this, "You have to put in a valid youtube link", Toast.LENGTH_SHORT).show();
            } else {
                dbHandler.addNewOst(lastAddedOst);
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
        dbHandler.deleteOst(listFragment.getOstReplaceId());
        listFragment.removeOst(listFragment.getOstReplacePos());
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

        if (addCanceled) {
            unAddedOst = new Ost(title, show, tags, url);
            listFragment.setUnAddedOst(unAddedOst);
        } else {
            addCanceled = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionHandlerKt.checkPermission(this);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            IOHandler.INSTANCE.readFromFile(currFileURI, this);
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            IOHandler.INSTANCE.writeToFile(currFileURI, dbHandler.getAllOsts(), this);
        }

        if (requestCode == 3) {
            yTplayerService.launchFloater(this, queueHandler);
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
            System.out.println("Adding to queue");
            yTplayerService.getQueueHandler().addToQueue(ost);
        }

    }

    public void initiatePlayer(List<Ost> ostList, int startId) {
        if (!youtubePlayerLaunched) {
            seekBarHandler = new SeekBarHandler(seekBar);
            youtubePlayerLaunched = true;
            System.out.println("Initiating player");
            queueHandler = new QueueHandler(ostList, startId,
                    shuffleActivated, listFragment.getCustomAdapter(), queueAdapter);
            yTplayerService.launchFloater(this, queueHandler);
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
                    UtilMeths.INSTANCE.sendYTPServiceIntent(this, null,
                            Constants.PLAY_ACTION);
                    break;
                }

                case R.id.btnNext: {
                    UtilMeths.INSTANCE.sendYTPServiceIntent(this, null,
                            Constants.NEXT_ACTION);
                    break;
                }

                case R.id.btnPrevious: {
                    UtilMeths.INSTANCE.sendYTPServiceIntent(this, null,
                            Constants.PREV_ACTION);
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

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        if(isPlaying){
            seekBarHandler.startSeekbar();
        } else{
            seekBarHandler.stopSeekBar();
        }
        pausePlay();
    }

    public void pausePlay() {
        if (isPlaying) {
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
            if (ostFromWidget) {
                startWidgetOst(ostFromWidgetId);
                ostFromWidget = false;
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
            //yTplayerService.refresh();
            saveSession();
            yTplayerService.getYWebPlayer().setOutsideActivity(true);
            //handler.removeCallbacks(seekbarUpdater);
        }
        doUnbindService();
    }

    @Override
    public void onStart() {
        super.onStart();
        doBindService();
        if (youtubePlayerLaunched) {
            yTplayerService.getYWebPlayer().setOutsideActivity(false);
            //handler.postDelayed(seekbarUpdater, 1000);
        }
    }

    private void addOstLink(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getType().equals("text/plain")) {
            Bundle extras = intent.getExtras();
            String link = extras.getString(Intent.EXTRA_TEXT);
            YoutubeShare yShare = new YoutubeShare(link);
            yShare.setContext(this);
            yShare.execute();
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
        }
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String intAction = intent.getAction();
        if (intAction == null) {
            return;
        }
        switch (intAction) {
            case Constants.PLAY_ACTION:{
                setPlaying(true);
                break;
            }
            case Constants.PAUSE_ACTION:{
                setPlaying(false);
                break;
            }
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
        }
    }

    void startWidgetOst(int startId) {
        if (startId != -1) {
            initiatePlayer(dbHandler.getAllOsts(), startId);
        }
    }

    public void setSeekBarProgress(int progress) {
        seekBar.setProgress(progress);
    }

    public void saveSession() {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        StringBuilder stringBuilder = new StringBuilder();
        for (Ost ost : yTplayerService.getQueueHandler().getOstList()
                ) {
            if (ost.getId() == 0) { //appends the url of the ost since it has been added from search
                stringBuilder.append(ost.getUrl()).append(",");
            } else {
                stringBuilder.append(ost.getId()).append(",");
            }
        }
        String idString = stringBuilder.toString();
        editor.putString("lastSession", idString);
        int lastCurrentlyPlaying = yTplayerService.getQueueHandler().getCurrPlayingIndex();
        editor.putInt("lastCurrPlaying", lastCurrentlyPlaying);
        editor.putInt("timeStamp", seekBar.getProgress());
        editor.putInt("videoDuration", seekBar.getMax());

        // Commit the edits!
        Boolean success = editor.commit();
        String successString = success.toString();
        Log.i("Wrote session", successString);
    }
    private void loadLastSession() {
        SharedPreferences lastSessionPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        String queueString = lastSessionPrefs.getString("lastSession", "");
        int timestamp = lastSessionPrefs.getInt("timeStamp", 0);
        int lastCurr = lastSessionPrefs.getInt("lastCurrPlaying", 0);
        int videoDuration = lastSessionPrefs.getInt("videoDuration", 0);
        if (!queueString.equals("")) {
            Log.d("lastQueue", queueString);
            List<Ost> lastQueueList = UtilMeths.INSTANCE.buildOstListFromQueue(queueString,
                    dbHandler);
            if(!lastQueueList.isEmpty() && lastCurr < lastQueueList.size()) {
                initiatePlayer(lastQueueList, lastCurr);
                yTplayerService.loadLastSession(timestamp, videoDuration);
            }
        }
        lastSessionLoaded = true;
    }

    public static DBHandler getDbHandler() {
        return dbHandler;
    }

    public ListFragment getListFragment() {
        return listFragment;
    }
}

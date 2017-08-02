package com.odd.ostrino;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.odd.ostrino.Listeners.PlayerListener;
import com.odd.ostrino.Listeners.QueueListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements AddScreen.AddScreenListener, FunnyJunk.YareYareListener,
        DialogInterface.OnDismissListener, QueueListener,
        View.OnClickListener{

    private DBHandler db;
    private Ost unAddedOst;
    private List<Ost> ostList;
    private int backPress;
    private ListFragment listFragment;
    private FrameLayout floatingPlayer;
    private RelativeLayout rlContent;
    private boolean youtubeFragLaunched = false, about = false;
    private final static int MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE = 0;
    private QueueAdapter queueAdapter;
    private FragmentManager manager;
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private Boolean mIsBound = false, playing = false;
    private YTplayerService yTplayerService;
    private ImageButton btnPlayPause;
    SeekBar seekBar;
    private int currTime = 0;
    private Runnable runnable;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DBHandler(this);
        unAddedOst = null;
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);

        final int interval = 1000; // 1 Second
        runnable = new Runnable(){
            public void run() {
                if(youtubeFragLaunched && yTplayerService.getPlaying()){
                    System.out.println(yTplayerService.yPlayer.getCurrentTimeMillis());
                    seekBar.setProgress(yTplayerService.yPlayer.getCurrentTimeMillis());
                }
                handler.postDelayed(runnable, interval);
            }
        };

        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        floatingPlayer = (FrameLayout) findViewById(R.id.floatingPlayer);

        btnPlayPause = (ImageButton) findViewById(R.id.btnPause);
        ImageButton btnNext = (ImageButton) findViewById(R.id.btnNext);
        ImageButton btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);

        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);

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
        manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.rlListContainer, listFragment)
                .addToBackStack("list")
                .commit();

        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.floatingPlayer, youTubePlayerFragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(about){
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

            case R.id.action_about:{
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
                if(listFragment.getCustomAdapter().getNowPlaying() == -1){
                    Toast.makeText(this, "Nothing is playing", Toast.LENGTH_SHORT).show();
                } else{
                    Ost ost = listFragment.getCustomAdapter().getNowPlayingOst();
                    ClipData clip = ClipData.newPlainText("Ost url", ost.getUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Link Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.hide_SearchBar:{
                if(listFragment.tlTop.getVisibility() == View.GONE){
                    listFragment.tlTop.setVisibility(View.VISIBLE);
                } else{
                    listFragment.tlTop.setVisibility(View.GONE);
                }
                break;
            }

            case R.id.import_osts: {
                chooseFileImport();
                break;
            }

            case R.id.export_osts: {
                chooseFileExport();
                break;
            }

            case R.id.delete_allOsts:{
                db.emptyTable();
                listFragment.refreshListView();
                break;
            }

            case  R.id.refresh_tagsTable:{
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

        checkPermission();
        Ost lastAddedOst = new Ost(title, show, tags, url);
        lastAddedOst.setId(listFragment.getOstReplaceId());
        boolean alreadyAdded = db.checkiIfOstInDB(lastAddedOst);

        if (listFragment.isEditedOst()) {
            db.updateOst(lastAddedOst);
            listFragment.refreshListView();
            downloadThumbnail(url);
        } else if (!alreadyAdded) {
            if (!url.contains("https://")) {
                Toast.makeText(this, "You have to put in a valid youtube link", Toast.LENGTH_SHORT).show();
            } else {
                db.addNewOst(lastAddedOst);
                Toast.makeText(getApplicationContext(), lastAddedOst.getTitle() + " added", Toast.LENGTH_SHORT).show();
                listFragment.refreshListView();
                downloadThumbnail(url);
            }
        } else {
            Toast.makeText(this, lastAddedOst.getTitle() + " From " + lastAddedOst.getShow() + " has already been added", Toast.LENGTH_SHORT).show();
            //lastAddedOst = null;
        }
    }

    @Override
    public void onDeleteButtonClick(DialogFragment dialog) {
        db.deleteOst(listFragment.getOstReplaceId());
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

        unAddedOst = new Ost(title, show, tags, url);
        listFragment.setUnAddedOst(unAddedOst);
    }

    private void chooseFileImport() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            startActivityForResult(intent, 1);

        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Select a File to Upload"),
                        1);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(getApplicationContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseFileExport() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            startActivityForResult(intent, 2);
        } else {
            intent = new Intent();
            intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("text/plain");
            startActivityForResult(intent, 2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkPermission();
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            readFromFile(currFileURI);
            listFragment.refreshListView();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            try {
                writeToFile(currFileURI);
                listFragment.refreshListView();

            } catch (IOException e) {
                System.out.println(" caught IOexception");
            }
        }
        if(requestCode == 3){
            //launchFloater();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void readFromFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                Ost ost = new Ost();
                String[] lineArray = line.split("; ");
                if (lineArray.length < 4) {
                    return;
                }
                ost.setTitle(lineArray[0]);
                ost.setShow(lineArray[1]);
                ost.setTags(lineArray[2]);
                ost.setUrl(lineArray[3]);
                boolean alreadyInDB = db.checkiIfOstInDB(ost);
                if (!alreadyInDB) {
                    db.addNewOst(ost);
                    downloadThumbnail(lineArray[3]);
                }
            }
        } catch (IOException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public void writeToFile(Uri uri) throws IOException {
        ostList = db.getAllOsts();
        try {
            OutputStream os = getContentResolver().openOutputStream(uri);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            String line;
            for (Ost ost : ostList) {

                String title = ost.getTitle();
                String show = ost.getShow();
                String tags = ost.getTags();
                String url = ost.getUrl();
                line = title + "; " + show + "; " + tags + "; " + url + "; ";
                osw.write(line + "\n");
            }
            osw.close();
        } catch (IOException e) {
            throw new IOException("File not found");
        }
    }

    public AddScreen getDialog() {
        return listFragment.getDialog();
    }

    public void addToQueue(Ost ost) {
        if(!youtubeFragLaunched){
            Toast.makeText(this, "You have to play something first", Toast.LENGTH_SHORT).show();
        }else{
            yTplayerService.queueHandler.addToQueue(ost.getUrl());
            queueAdapter.addToQueue(ost);
        }

    }

    public void initPlayerService(){
        if(yTplayerService == null){
            startService();
            doBindService();
        }
    }

    public void initiatePlayer(List<Ost> ostList, int startid) {
        queueAdapter.initiateQueue(ostList, startid);

        if(!mIsBound){
            initPlayerService();
        }
        if (!youtubeFragLaunched) {
            //floatingPlayer.setVisibility(View.VISIBLE);
            rlContent.removeView(floatingPlayer);
            youtubeFragLaunched = true;
            PlayerListener[] playerListeners = new PlayerListener[3];
            playerListeners[0] = queueAdapter;
            playerListeners[1] = listFragment;
            playerListeners[2] = listFragment.getCustomAdapter();
            yTplayerService.showNotification();
            yTplayerService.startQueue(ostList, startid, listFragment.shuffleActivated,
                    playerListeners, youTubePlayerFragment);
            yTplayerService.launchFloater(floatingPlayer, this);
            //yTPlayerFrag.initialize(Constants.API_TOKEN, yTplayerService);
        }else {
            yTplayerService.initiateQueue(ostList, startid, listFragment.shuffleActivated);
        }
        playing = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnPause:{
                if(!youtubeFragLaunched){
                    Toast.makeText(this, "Play something first bruh :)", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(playing){
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    playing = false;
                } else{
                    btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                    playing = true;
                }
                yTplayerService.pausePlay();
                break;
            }
            case R.id.btnNext:{
                yTplayerService.playerNext();
                btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                break;
            }
            case R.id.btnPrevious:{
                yTplayerService.playerPrevious();
                btnPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                break;
            }
        }
    }

    public void shuffleOn() {
        yTplayerService.queueHandler.shuffleOn();
    }

    public void shuffleOff() {
        yTplayerService.queueHandler.shuffleOff();
    }

    @Override
    public void addToQueue(int addId) {

    }

    @Override
    public void removeFromQueue(String url) {
        yTplayerService.queueHandler.removeFromQueue(url);
    }

    public boolean youtubeFragNotLaunched() {
        return youtubeFragLaunched = false;
    }

    void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void downloadFile(String uRl, String saveName) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/OSTthumbnails");

        if (!direct.exists()) {
            direct.mkdirs();
        }
        String saveString = direct.getAbsolutePath() + "/" + saveName + ".jpg";
        System.out.println(saveString);
        if(!UtilMeths.doesFileExist(saveString)) {
            DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri = Uri.parse(uRl);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle(uRl)
                    .setDescription("Downloading thumbnails")
                    .setDestinationInExternalPublicDir("/OSTthumbnails", saveName + ".jpg");

            mgr.enqueue(request);
        }
    }

    public void downloadThumbnail(String url){
        downloadFile("http://img.youtube.com/vi/" + UtilMeths.urlToId(url) + "/2.jpg", UtilMeths.urlToId(url));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        Boolean autoRotate = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
        if (autoRotate){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;*/
    }

    void startService() {
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
            yTplayerService = ((YTplayerService.LocalBinder)service).getService();
            // Tell the user about this for our demo.
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mIsBound = false;
            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this,
                YTplayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        //mCallbackText.setText("Binding.");
    }

    void doUnbindService() {
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
        if(youtubeFragLaunched){
            yTplayerService.refresh();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if(!youtubeFragLaunched) {
            initPlayerService();
        }
    }

    public void updateSeekbar(){
        if(youtubeFragLaunched){
            if(yTplayerService.getPlaying()){
                seekBar.setProgress(currTime++);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int ostId = intent.getIntExtra(getString(R.string.label_ost_of_the_day), -1);

        System.out.println(ostId);
        if(ostId != -1){
            initiatePlayer(db.getAllOsts(), ostId);
        }

        super.onNewIntent(intent);
    }
}

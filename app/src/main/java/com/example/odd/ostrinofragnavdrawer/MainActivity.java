package com.example.odd.ostrinofragnavdrawer;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.odd.ostrinofragnavdrawer.Listeners.PlayerListener;
import com.example.odd.ostrinofragnavdrawer.Listeners.QueueListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity
        implements AddScreen.AddScreenListener, FunnyJunk.YareYareListener,
        DialogInterface.OnDismissListener, QueueListener, View.OnClickListener {

    private DBHandler db;
    private Ost unAddedOst;
    private List<Ost> ostList;
    private Random rnd;
    private int backPress;
    private ListFragment listFragment;
    private YoutubeFragment youtubeFragment = null;
    private FrameLayout flPlayer;
    private RelativeLayout rlContainer;
    private OstAdapter ostAdapter;
    private boolean youtubeFragLaunched = false;
    private RecyclerView rvQueue;
    private final static int MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DBHandler(this);
        unAddedOst = null;

        //For reseting database
        //SQLiteDatabase dtb = db.getWritableDatabase();
        rnd = new Random();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        flPlayer = (FrameLayout) findViewById(R.id.flPlayer);
        rlContainer = (RelativeLayout) findViewById(R.id.rlContainer);
        Button btnStopPlayer = (Button) flPlayer.findViewById(R.id.btnStopPlayer);
        Button btnMovePlayer = (Button) flPlayer.findViewById(R.id.btnMovePlayer);

        btnStopPlayer.setOnClickListener(this);
        btnMovePlayer.setOnClickListener(this);
        btnMovePlayer.setOnTouchListener(new View.OnTouchListener() {
            float dx, dy;
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) flPlayer.getLayoutParams();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        dx = event.getRawX() - flPlayer.getX();
                        dy = event.getRawY() - flPlayer.getY();
                    }
                    break;
                    case MotionEvent.ACTION_MOVE: {
                        float setPosX = event.getRawX() - dx;
                        float setPosY = event.getRawY() - dy;
                        boolean xOutsideScreen = setPosX < 0 || setPosX > width - flPlayer.getWidth();
                        boolean yOutsideScreen = setPosY < toolbar.getHeight() || setPosY > rlContainer.getHeight() - flPlayer.getHeight() - lParams.topMargin;
                        if (xOutsideScreen && yOutsideScreen) {
                            return false;
                        }
                        if (xOutsideScreen) {
                            flPlayer.setY(setPosY);
                        } else if (yOutsideScreen) {
                            flPlayer.setX(setPosX);
                        } else {
                            flPlayer.setX(setPosX);
                            flPlayer.setY(setPosY);
                        }
                        //System.out.println("X: " + flPlayer.getX() + ", Y: " + flPlayer.getY());
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

        rvQueue = (RecyclerView) findViewById(R.id.rvQueue);

        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvQueue.setLayoutManager(mLayoutManager);
        rvQueue.setItemAnimator(new DefaultItemAnimator());
        rvQueue.setAdapter(new OstAdapter());

        listFragment = new ListFragment();
        listFragment.setMainAcitivity(this);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.rlContent, listFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
                return true;
            }

            case R.id.share: {
                Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.import_osts: {
                chooseFileImport();
                break;
            }

            case R.id.export_osts: {
                chooseFileExport();
            }

            case R.id.delete_allOsts:{
                db.emptyTable();
                listFragment.refreshListView();
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
            lastAddedOst = null;
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
        System.out.println(unAddedOst);
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
        if(youtubeFragment == null){
            Toast.makeText(this, "You have to play something first", Toast.LENGTH_SHORT).show();
        }else{
            youtubeFragment.addToQueue(ost.getUrl());
            ostAdapter.addToQueue(ost);
        }

    }

    public void initYoutubeFrag() {
        if (youtubeFragment == null) {
            youtubeFragment = new YoutubeFragment();
            PlayerListener[] playerListeners = new PlayerListener[2];
            playerListeners[0] = ostAdapter;
            playerListeners[1] = listFragment;
            youtubeFragment.setPlayerListeners(playerListeners);
        }
    }

    public void launchYoutubeFrag() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.flPlayer, youtubeFragment)
                .commit();
        flPlayer.setVisibility(View.VISIBLE);
    }

    public void initiatePlayer(List<Ost> ostList, int startid) {
        ostAdapter = new OstAdapter(this, ostList, startid, this);
        rvQueue.setAdapter(ostAdapter);
        initYoutubeFrag();
        youtubeFragment.initiateQueue(ostList, startid);
        updateYoutubeFrag();
    }

    public void updateYoutubeFrag() {
        if (youtubeFragLaunched) {
            flPlayer.setVisibility(View.VISIBLE);
            youtubeFragment.initPlayer();
        } else {
            launchYoutubeFrag();
            youtubeFragLaunched = true;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnStopPlayer: {
                youtubeFragment.pausePlayer();
                flPlayer.setVisibility(View.GONE);
                break;
            }
            case R.id.btnMovePlayer: {
                Toast.makeText(this, "Touch and drag to move the player", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public void shuffleOn() {
        youtubeFragment.shuffleOn();
    }

    public void shuffleOff() {
        youtubeFragment.shuffleOff();
    }

    @Override
    public void addToQueue(int addId) {

    }

    @Override
    public void removeFromQueue(String url) {
        youtubeFragment.removeFromQueue(url);
    }

    public boolean youtubeFragNotLaunched() {
        return youtubeFragLaunched;
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
        if(!Util.doesFileExist(saveString)) {
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
        downloadFile("http://img.youtube.com/vi/" + Util.urlToId(url) + "/2.jpg", Util.urlToId(url));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
    }

}

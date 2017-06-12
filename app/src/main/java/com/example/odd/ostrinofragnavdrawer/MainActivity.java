package com.example.odd.ostrinofragnavdrawer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.odd.ostrinofragnavdrawer.R.id.btnMovePlayer;
import static com.example.odd.ostrinofragnavdrawer.R.id.flPlayer;
import static com.example.odd.ostrinofragnavdrawer.R.id.lvQueue;


public class MainActivity extends AppCompatActivity
        implements AddScreen.AddScreenListener, FunnyJunk.YareYareListener,
        DialogInterface.OnDismissListener, PlayerListener, QueueListener, View.OnClickListener{

    private DBHandler db;
    private Ost unAddedOst;
    private List<Ost> ostList;
    private Random rnd;
    int backPress;
    private ListFragment listFragment;
    private CustomAdapter customAdapter;
    private YoutubeFragment youtubeFragment = null;
    private FrameLayout flPlayer;
    private RelativeLayout rlContainer;
    ListView lvQueue;
    private boolean playerDocked = true, youtubeFragLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DBHandler(this);
        unAddedOst = null;
        playerDocked = true;

        //For reseting database
        /*SQLiteDatabase dtb = db.getWritableDatabase();
        db.emptyTable();
        db.onCreate(dtb);*/
        rnd = new Random();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        flPlayer = (FrameLayout) findViewById(R.id.flPlayer);
        rlContainer =(RelativeLayout) findViewById(R.id.rlContainer);
        Button btnStopPlayer = (Button) flPlayer.findViewById(R.id.btnStopPlayer);
        Button btnMovePlayer = (Button) flPlayer.findViewById(R.id.btnMovePlayer);

        btnStopPlayer.setOnClickListener(this);
        btnMovePlayer.setOnClickListener(this);
        btnMovePlayer.setOnTouchListener(new View.OnTouchListener() {
            float dx, dy;
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) flPlayer.getLayoutParams();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                playerDocked = false;
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
                        if( xOutsideScreen && yOutsideScreen){
                            return false;
                        }
                        if (xOutsideScreen) {
                            flPlayer.setY(setPosY);
                        }
                        else if (yOutsideScreen) {
                            flPlayer.setX(setPosX);
                        } else {
                            flPlayer.setX(setPosX);
                            flPlayer.setY(setPosY);
                        }
                        System.out.println("X: " + flPlayer.getX() + ", Y: " + flPlayer.getY());
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

            case R.id.add_ost: {
                Toast.makeText(this, "Will be removed", Toast.LENGTH_SHORT).show();
                break;
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

        Ost lastAddedOst = new Ost(title, show, tags, url);
        lastAddedOst.setId(listFragment.getOstReplaceId());
        boolean alreadyAdded = db.checkiIfOstInDB(lastAddedOst);

        if (listFragment.isEditedOst()) {
            db.updateOst(lastAddedOst);
            listFragment.refreshListView();
        } else if (!alreadyAdded) {
            if(!url.contains("https://")){
                Toast.makeText(this, "You have to put in a valid youtube link", Toast.LENGTH_SHORT).show();
            }
            else{
                db.addNewOst(lastAddedOst);
                Toast.makeText(getApplicationContext(), lastAddedOst.getTitle() + " added", Toast.LENGTH_SHORT).show();
                listFragment.refreshListView();
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
            System.out.println("File Chooser launched");

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
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            readFromFile(currFileURI);
            //listFragment.refreshList();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            try {
                writeToFile(currFileURI);

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
                System.out.println(line);
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
                System.out.println(line);
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
        youtubeFragment.addToQueue(ost.getUrl());

    }

    public void initYoutubeFrag(){
        if(youtubeFragment == null){
            youtubeFragment = new YoutubeFragment();
            PlayerListener[] playerListeners = new PlayerListener[2];
            playerListeners[0] = customAdapter;
            playerListeners[1] = listFragment;
            youtubeFragment.setPlayerListener(playerListeners);
        }
    }

    public void launchYoutubeFrag(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.flPlayer, youtubeFragment)
                .commit();
        flPlayer.setVisibility(View.VISIBLE);
    }

    public void initiatePlayer(List<Ost> ostList, int startid){

        lvQueue = (ListView) findViewById(R.id.lvQueue);
        lvQueue.findViewById(R.id.btnOptions);

        customAdapter = new CustomAdapter(getBaseContext(), ostList.subList(startid, ostList.size()), this, true);
        lvQueue.setAdapter(customAdapter);
        lvQueue.setDivider(null);
        initYoutubeFrag();
        youtubeFragment.initiateQueue(Util.extractUrls(ostList), startid);
        updateYoutubeFrag();
    }

    public void updateYoutubeFrag(){
        if(youtubeFragLaunched){
            flPlayer.setVisibility(View.VISIBLE);
            youtubeFragment.initPlayer();
        }
        else{
            launchYoutubeFrag();
            youtubeFragLaunched = true;
        }
    }
    @Override
    public void updateCurrentlyPlaying(int newId) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.btnStopPlayer:{
                youtubeFragment.pausePlayer();
                flPlayer.setVisibility(View.GONE);
                break;
            }
            case R.id.btnMovePlayer:{
                Toast.makeText(this, "Touch and drag to move the player", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public void shuffleOn(){
        youtubeFragment.shuffleOn();
    }

    public void shuffleOff(){
        youtubeFragment.shuffleOff();
    }

    @Override
    public void addToQueue(int addId) {

    }

    public boolean youtubeFragNotLaunched(){
        return youtubeFragment == null;
    }
}

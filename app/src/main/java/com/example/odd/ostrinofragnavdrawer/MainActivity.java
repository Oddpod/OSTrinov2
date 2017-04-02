package com.example.odd.ostrinofragnavdrawer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AddScreen.AddScreenListener, FunnyJunk.YareYareListener{

    private String TAG = "OstInfo";
    private DBHandler db;
    private Ost lastAddedOst;
    private List<Ost> ostList;
    private Random rnd;
    int backPress;
    private ListFragment listFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DBHandler(this);
        rnd = new Random();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listFragment = new ListFragment();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.nav_addOst:{
                AddScreen dialog = new AddScreen();
                dialog.show(getSupportFragmentManager(), TAG);
                dialog.setButtonText("Add");
                listFragment.isNotEdited();
                break;
            }

            case R.id.nav_importOst:{
                chooseFileImport();
                break;
            }

            case R.id.nav_exportOsts:{
                chooseFileExport();
                break;
            }

            case R.id.nav_randomOst:{
                ostList = db.getAllOsts();
                if(ostList.size()> 0){
                    int rndId = rnd.nextInt(ostList.size());
                    Ost ost = db.getOst(rndId);
                    String url = ost.getUrl();
                    listFragment.startOst(url);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Ost list is empty", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.nav_testConnection:{
                Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_testFloater:{
                if(!listFragment.youtubeFragLaunched){
                    Toast.makeText(this, "You must play something first bruh! :)", Toast.LENGTH_SHORT).show();
                }else {
                    listFragment.launchFloater();
                /*if(Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1234);
                    }
                    else{
                        Intent intent = new Intent(this, FloatingWindow.class);
                        startService(intent);

                    }
                }
                else
                {
                    Intent intent = new Intent(this, FloatingWindow.class);
                    FloatingWindow fw = new FloatingWindow();
                    listFragment.flNether.removeView(listFragment.flOnTop);
                    fw.addView(listFragment.flOnTop);
                    startService(intent);

                }*/
                }
                break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveButtonClick(DialogFragment dialog) {
        EditText entTitle = (EditText) dialog.getDialog().findViewById(R.id.edtTitle);
        String title = entTitle.getText().toString();
        EditText entShow = (EditText) dialog.getDialog().findViewById(R.id.edtShow);
        String show = entShow.getText().toString();
        EditText entTags = (EditText) dialog.getDialog().findViewById(R.id.edtTags);
        String tags = entTags.getText().toString();
        EditText entUrl = (EditText) dialog.getDialog().findViewById(R.id.edtUrl);
        String url = entUrl.getText().toString();
        lastAddedOst = new Ost(title, show, tags, url);
        lastAddedOst.setId(listFragment.getOstReplaceId());
        boolean alreadyAdded = db.checkiIfInDB(lastAddedOst);

        if(listFragment.isEditedOst()){
            db.updateOst(lastAddedOst);
        }

        else if(!alreadyAdded){
            db.addNewOst(lastAddedOst);
            Toast.makeText(getApplicationContext(), lastAddedOst.getTitle() + " added", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, lastAddedOst.getTitle() + " From " + lastAddedOst.getShow() + " has already been added", Toast.LENGTH_SHORT).show();
            lastAddedOst = null;
        }
        listFragment.refreshList();
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

    private void chooseFileExport(){
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
            listFragment.refreshList();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            try{
                writeToFile(currFileURI);

            }catch(IOException e){
                System.out.println(" caught IOexception");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void readFromFile(Uri uri){
        try{
            InputStream is = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                Ost ost = new Ost();
                System.out.println(line);
                String[] lineArray = line.split("; ");
                if(lineArray.length < 4){
                    return;
                }
                ost.setTitle(lineArray[0]);
                ost.setShow(lineArray[1]);
                ost.setTags(lineArray[2]);
                ost.setUrl(lineArray[3]);
                boolean alreadyInDB = db.checkiIfInDB(ost);
                if (!alreadyInDB) {
                    db.addNewOst(ost);
                    }
                }
            }catch(IOException e){
                System.out.println("File not found");
                e.printStackTrace();
            }
        }

    public void writeToFile(Uri uri) throws IOException{
        ostList= db.getAllOsts();
        try {
            OutputStream os = getContentResolver().openOutputStream(uri);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            String line;
            for( Ost ost : ostList){

                String title = ost.getTitle();
                String show = ost.getShow();
                String tags = ost.getTags();
                String url = ost.getUrl();
                line = title + "; " + show + "; " + tags + "; " + url + "; ";
                System.out.println(line);
                osw.write(line + "\n");
            }
            osw.close();
        }catch (IOException e){
            throw new IOException("File not found");
        }
    }
}

package com.example.odd.ostrinofragnavdrawer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ostdb", OST_TABLE = "ostTable", SHOW_TABLE = "showTable", TAGS_TABLE = "tagsTable";

    private static final String KEY_ID = "ostid", KEY_TITLE = "title", KEY_SHOW = "show", KEY_TAGS = "tags", KEY_URL = "url", KEY_TAG = "tag";

    DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ostList = new ArrayList<>();
        showList = new ArrayList<>();
        tagsList = new ArrayList<>();
    }

    private List<Ost> ostList;
    private List<String> showList, tagsList;

    //creating Tables
    @Override
    public void onCreate(SQLiteDatabase db){

        String CREATE_OST_TABLE = "CREATE TABLE " + OST_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_SHOW + " TEXT,"
                + KEY_TAGS + " TEXT, "
                + KEY_URL + " Text " + ")";
        db.execSQL(CREATE_OST_TABLE);

        String CREATE_SHOW_TABLE = "CREATE TABLE " + SHOW_TABLE + "("
               + "showid" + " INTEGER PRIMARY KEY,"
               + KEY_SHOW + " TEXT " + ")";
        db.execSQL(CREATE_SHOW_TABLE);

        String CREATE_TAGS_TABLE = "CREATE TABLE " + TAGS_TABLE + "("
                + "tagid" + " INTEGER PRIMARY KEY,"
                + KEY_TAG + " TEXT " + ")";
        db.execSQL(CREATE_TAGS_TABLE);
        System.out.println("Created tables");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS " + OST_TABLE);

        onCreate(db);
    }

    void addNewOst(Ost newOst){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        String show = newOst.getShow();

        values.put(KEY_TITLE, newOst.getTitle());
        values.put(KEY_SHOW, show);
        values.put(KEY_TAGS, newOst.getTags());
        values.put(KEY_URL, newOst.getUrl());

        if(!checkIfShowInDB(show)){
            addNewShow(show);
        }

        String [] tags = newOst.getTags().split(", ");
        for(String tag : tags) {
            if (!checkIfTagInDB(tag)) {
                addNewTag(tag);
            }
        }

        //inserting Row
        db.insert(OST_TABLE, null, values);
        db.close();
        System.out.println("row inserted");

    }

    public void addNewShow(String newShow){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_SHOW, newShow);
        db.insert(SHOW_TABLE, null, values);
        System.out.println("added new show");

    }

    public void addNewTag(String newTag){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TAG, newTag);
        db.insert(TAGS_TABLE, null, values);
        System.out.println("added new tag");
    }

    public void emptyTable(){
        //Truncate does not work in sqllite
        SQLiteDatabase db = this.getWritableDatabase();
        String TRUNCATE_TABLE = "DROP TABLE " + OST_TABLE + "";
        db.execSQL(TRUNCATE_TABLE);
    }

    boolean deleteOst(int delID){

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(OST_TABLE, KEY_ID + "=" + delID, null) > 0;
    }

    List<Ost> getAllOsts(){

        List<Ost> ostList = new ArrayList<>();

        String selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + "," + KEY_SHOW + "," + KEY_TAGS + "," + KEY_URL + " FROM " + OST_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{

                Ost ost = new Ost();
                ost.setId(cursor.getInt(0));
                ost.setTitle(cursor.getString(1));
                ost.setShow(cursor.getString(2));
                ost.setTags(cursor.getString(3));
                ost.setUrl(cursor.getString(4));

                ostList.add(ost);

            }while(cursor.moveToNext());
        }
        cursor.close();
        return ostList;
    }

    Ost getOst(int id){

        Ost ost = new Ost();
        String selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + "," + KEY_SHOW + "," + KEY_TAGS + "," + KEY_URL
                + " FROM " + OST_TABLE
                + " WHERE " + KEY_ID + "=" + Integer.toString(id);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            do {
                ost.setId(cursor.getInt(0));
                ost.setTitle(cursor.getString(1));
                ost.setShow(cursor.getString(2));
                ost.setTags(cursor.getString(3));
                ost.setUrl(cursor.getString(4));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return ost;
    }

    void updateOst(Ost ost){

        ContentValues values = new ContentValues();

        values.put(KEY_ID, ost.getId());
        values.put(KEY_TITLE, ost.getTitle());
        values.put(KEY_SHOW, ost.getShow());
        values.put(KEY_TAGS, ost.getTags());
        values.put(KEY_URL, ost.getUrl());

        //replacing row
        SQLiteDatabase db = this.getWritableDatabase();
        db.replace(OST_TABLE, null, values);
    }

    boolean checkiIfOstInDB(Ost ost) {
        ostList = getAllOsts();
        String ostString = ost.toString().toLowerCase();
        for (Ost ostFromDB : ostList){
            if(ostFromDB.toString().toLowerCase().equals(ostString)){
                return true;
            }
        }
        return false;
    }


    public boolean checkIfShowInDB(String show){
        showList = getAllShows();
        String showString = show.toLowerCase();
        for (String showFromDB : showList){
            if(showFromDB.toLowerCase().equals(showString)){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfTagInDB(String tag){
        tagsList = getAllTags();
        String tagString = tag.toLowerCase();
        for (String tagFromDB : tagsList){
            if(tagFromDB.toLowerCase().equals(tagString)){
                return true;
            }
        }
        return false;
    }


    public List<String> getAllShows(){

        List<String> showList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + SHOW_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{

                showList.add(cursor.getString(1));

            }while(cursor.moveToNext());
        }
        cursor.close();
        return showList;
    }

    public List<String> getAllTags(){
        List<String> tagsTable = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TAGS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{

                tagsTable.add(cursor.getString(1));

            }while(cursor.moveToNext());
        }
        cursor.close();
        return tagsTable;
    }
}

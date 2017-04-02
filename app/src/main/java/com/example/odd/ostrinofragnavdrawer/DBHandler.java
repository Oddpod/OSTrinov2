package com.example.odd.ostrinofragnavdrawer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ostdb", OST_TABLE = "ostTable";

    private static final String KEY_ID = "ostid", KEY_TITLE = "title", KEY_SHOW = "show", KEY_TAGS = "tags", KEY_URL = "url";

    DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ostList = new ArrayList<>();
    }

    private List<Ost> ostList;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS " + OST_TABLE);

        onCreate(db);
    }

    void addNewOst(Ost newOst){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, newOst.getTitle());
        values.put(KEY_SHOW, newOst.getShow());
        values.put(KEY_TAGS, newOst.getTags());
        values.put(KEY_URL, newOst.getUrl());

        //inserting Row
        db.insert(OST_TABLE, null, values);
        db.close();
        System.out.println("row inserted");

    }

    public void dropOstTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String DROP_TABLE = "DROP TABLE " + OST_TABLE + "";
        db.execSQL(DROP_TABLE);
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

    boolean checkiIfInDB(Ost ost) {
        ostList = getAllOsts();
        String ostString = ost.toString().toLowerCase();
        for (Ost ostFromDB : ostList){
            if(ostFromDB.toString().toLowerCase().equals(ostString)){
                return true;
            }
        }
        return false;
    }
}

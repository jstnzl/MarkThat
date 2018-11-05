package com.example.justi.markthat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class MyDB extends SQLiteOpenHelper {

    Context ctx;
    SQLiteDatabase db;
    private static String DB_NAME = "recording-test2";
    private  static int VERSION = 1;

    public MyDB(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, null, VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table recordTable(fileName String primary key, title String, description String, foreign key(fileName) references markTable(file));");
        db.execSQL("create table markTable(file String primary key, title String, description String, duration String);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists recordTable");
        db.execSQL("drop table if exists markTable");
        VERSION++;
        onCreate(db);
    }

    public void insert(String fileName, String title, String description){
        if(title.matches(""))
            title = "Recording-"+fileName;
        if(description.matches(""))
            description = "No description yet";
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("fileName", fileName);
        cv.put("title", title);
        cv.put("description", description);
        db.insert("recordTable", null, cv);
        ContentValues vc = new ContentValues();
        vc.put("file", fileName);
        vc.put("title", "Mark");
        vc.put("description", "Description Goes Here");
        db.insert("markTable", null, vc);
    }

    public ArrayList<String> getAllRecords(){
        ArrayList<String> res = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cr = db.rawQuery("select * from recordTable;", null );
        while(cr.moveToNext()){
            // file, title, desc
            res.add(cr.getString(0) + "," + cr.getString(1)+"&"+cr.getString(2));
        }
        cr.close();
        return res;
    }

//    public ArrayList<String> getAllMarks(){
//        ArrayList<String> res = new ArrayList<>();
//        db = getReadableDatabase();
//        Cursor cr = db.rawQuery("select * from recordTable;", null );
//        while(cr.moveToNext()){
//            res.add(cr.getString(0) + "          " + cr.getString(2)+","+cr.getString(1));
//        }
//        cr.close();
//        return res;
//    }

//    public boolean delete(String s){
//        db = getWritableDatabase();
//        int deletedRows = 0;
//        deletedRows += db.delete("recordTable", "title= ?", new String[]{s});
//        deletedRows += db.delete("recordTable", "id= ?", new String[]{s});
//        return deletedRows > 0;
//    }
//
//    public void update(String s1, String s2){
//        db = getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put("url", s1);
//        cv.put("title", s2);
//        db.update("recordTable", cv,  "url = ?", new String[]{s1});
//    }
}

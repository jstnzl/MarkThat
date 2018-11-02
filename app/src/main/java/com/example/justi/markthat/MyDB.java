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
    private static String DB_NAME = "recordings";
    private static String TABLE_NAME = "record_table";
    private  static int VERSION = 1;

    public MyDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, null, VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(filepath String primary key, title String, description String);");
        Toast.makeText(ctx, "DB is created", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        VERSION++;
        onCreate(db);
    }

    public long insert(String blob, String s2){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("url", blob);
        cv.put("title", s2);
        return db.insert(TABLE_NAME, null, cv);
    }

    public ArrayList<String> getAll(){
        ArrayList<String> res = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cr = db.rawQuery("select * from " + TABLE_NAME + ";", null );
        while(cr.moveToNext()){
            res.add(cr.getString(0) + "          " + cr.getString(2)+","+cr.getString(1));
        }
        cr.close();
        return res;
    }
    public boolean delete(String s){
        db = getWritableDatabase();
        int deletedRows = 0;
        deletedRows += db.delete(TABLE_NAME, "title= ?", new String[]{s});
        deletedRows += db.delete(TABLE_NAME, "id= ?", new String[]{s});
        return deletedRows > 0;
    }

    public void update(String s1, String s2){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("url", s1);
        cv.put("title", s2);
        db.update(TABLE_NAME, cv,  "url = ?", new String[]{s1});
    }
}

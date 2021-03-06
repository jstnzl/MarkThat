package com.example.justi.markthat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class MyDB extends SQLiteOpenHelper {

    Context ctx;
    SQLiteDatabase db;
    private static String DB_NAME = "MarkThat-DB";
    private  static int VERSION = 1;

    public MyDB(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, null, VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table recordTable(fileName String primary key, title String, description String);");
        db.execSQL("create table markTable(file String, title String, description String, position long);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists recordTable");
        db.execSQL("drop table if exists markTable");
        VERSION++;
        onCreate(db);
    }

    public void insertRecord(String fileName, String title, String description){
        if(title.matches(""))
            title = "Recording-"+(countRecords(fileName)+1);
        if(description.matches(""))
            description = "No description yet";
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("fileName", fileName);
        cv.put("title", title);
        cv.put("description", description);
        db.insert("recordTable", null, cv);
    }

    public void insertMark(String fileName, String title, String description, long position){
        if(title.matches(""))
            title = "Mark-"+(countMarks(fileName)+1);
        if(description.matches(""))
            description = "No description yet";
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("file", fileName);
        cv.put("title", title);
        cv.put("description", description);
        cv.put("position", position);
        db.insert("markTable", null, cv);
    }

    public List<List<String>> getAllRecords(){
        List<List<String>> res = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cr = db.rawQuery("select * from recordTable;", null );
        while(cr.moveToNext()){
            ArrayList<String> temp = new ArrayList<>();
            // file, title, desc
            temp.add(cr.getString(0));
            temp.add(cr.getString(1));
            temp.add(cr.getString(2));
            res.add(temp);
        }
        cr.close();
        return res;
    }

    public List<List<String>> getMarksForRecord(String fileName){
        String[] params = new String[]{fileName};
        List<List<String>> res = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cr = db.rawQuery("select * from markTable where file= ?;", params);
        while(cr.moveToNext()){
            // file, title, desc, position
            ArrayList<String> temp = new ArrayList<>();
            temp.add(cr.getString(0));
            temp.add(cr.getString(1));
            temp.add(cr.getString(2));
            temp.add(cr.getString(3));
            res.add(temp);
        }
        cr.close();
        return res;
    }

    public List<List<String>> searchRecords(String query){
        String[] params = new String[]{query, query};
        List<List<String>> res = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cr = db.rawQuery("select * from recordTable where title= ? or desciption= ?;", params );
        while(cr.moveToNext()){
            ArrayList<String> temp = new ArrayList<>();
            // file, title, desc
            temp.add(cr.getString(0));
            temp.add(cr.getString(1));
            temp.add(cr.getString(2));
            res.add(temp);
        }
        cr.close();
        return res;
    }

    public int countMarks(String fileName) {
        String[] params = new String[]{fileName};
        db = getReadableDatabase();
        Cursor cr = db.rawQuery("select * from markTable where file= ?;", params);
        cr.moveToFirst();
        return cr.getCount();
    }

    public int countRecords(String fileName) {
        String[] params = new String[]{fileName};
        db = getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "recordTable");
        db.close();
        return (int)count;
    }

    public void updateRecord(String file, String s1, String s2 ){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", s1);
        cv.put("description", s2);
        db.update("recordTable", cv,  "fileName = ?", new String[]{file});
    }

    public void updateMark(String file, String origPos, String s1, String s2, String s3){
        db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", s1);
        cv.put("description", s2);
        cv.put("position", s3);
        db.update("markTable", cv,  "file = ? AND position=?", new String[]{file, origPos});
    }

    public boolean deleteRecord(String s){
        db = getWritableDatabase();
        int deletedRows = 0;
        deletedRows += db.delete("recordTable", "fileName= ?", new String[]{s});
        db.delete("markTable", "file= ?", new String[]{s});
        return deletedRows > 0;
    }

    public boolean deleteMark(String s, String s1){
        db = getWritableDatabase();
        int deletedRows = 0;
        deletedRows += db.delete("markTable", "file= ? AND position=?", new String[]{s, s1});
        return deletedRows > 0;
    }

    public boolean deleteMarksForRecord(String s){
        db = getWritableDatabase();
        int deletedRows = 0;
        deletedRows += db.delete("markTable", "file= ?", new String[]{s});
        return deletedRows > 0;
    }

}

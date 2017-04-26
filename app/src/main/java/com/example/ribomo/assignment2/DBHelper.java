package com.example.ribomo.assignment2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ribomo on 4/24/17.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context){
        super(context, "Main",null,1);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Track(datetime VARCHAR,locations VARCHAR);");
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public boolean insertData(String datetime, String locations){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("datetime", datetime);
        contentValues.put("locations", locations);
        db.insert("Track", null, contentValues);
        return true;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.query(true, "Track", null, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}

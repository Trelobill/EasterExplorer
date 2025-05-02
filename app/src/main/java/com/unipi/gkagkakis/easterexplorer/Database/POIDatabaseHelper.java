package com.unipi.gkagkakis.easterexplorer.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class POIDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "poi_database.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_POI = "poi_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_PHOTO_PATH = "photo_path";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_INFO = "info";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_POI + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_RATING + " REAL, " +
                    COLUMN_PHOTO_PATH + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_TIMESTAMP + " INTEGER, " +
                    COLUMN_INFO + " TEXT);";


    public POIDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POI);
        onCreate(db);
    }
}

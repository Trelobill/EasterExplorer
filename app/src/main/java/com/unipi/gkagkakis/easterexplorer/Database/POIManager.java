package com.unipi.gkagkakis.easterexplorer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.unipi.gkagkakis.easterexplorer.Models.POI;

import java.util.ArrayList;
import java.util.List;

public class POIManager {
    private final SQLiteDatabase database;

    public POIManager(Context context) {
        POIDatabaseHelper dbHelper = new POIDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public long insertPOI(POI poi) {
        ContentValues values = new ContentValues();
        values.put(POIDatabaseHelper.COLUMN_TITLE, poi.getTitle());
        values.put(POIDatabaseHelper.COLUMN_CATEGORY, poi.getCategory());
        values.put(POIDatabaseHelper.COLUMN_RATING, poi.getRating());
        values.put(POIDatabaseHelper.COLUMN_PHOTO_PATH, poi.getPhotoPath());
        values.put(POIDatabaseHelper.COLUMN_LATITUDE, poi.getLatitude());
        values.put(POIDatabaseHelper.COLUMN_LONGITUDE, poi.getLongitude());
        values.put(POIDatabaseHelper.COLUMN_TIMESTAMP, poi.getTimestamp());
        values.put(POIDatabaseHelper.COLUMN_INFO, poi.getInfo());
        return database.insert(POIDatabaseHelper.TABLE_POI, null, values);
    }

    public int updatePOI(POI poi) {
        ContentValues values = new ContentValues();
        values.put(POIDatabaseHelper.COLUMN_TITLE, poi.getTitle());
        values.put(POIDatabaseHelper.COLUMN_CATEGORY, poi.getCategory());
        values.put(POIDatabaseHelper.COLUMN_RATING, poi.getRating());
        values.put(POIDatabaseHelper.COLUMN_PHOTO_PATH, poi.getPhotoPath());
        values.put(POIDatabaseHelper.COLUMN_LATITUDE, poi.getLatitude());
        values.put(POIDatabaseHelper.COLUMN_LONGITUDE, poi.getLongitude());
        values.put(POIDatabaseHelper.COLUMN_TIMESTAMP, poi.getTimestamp());
        values.put(POIDatabaseHelper.COLUMN_INFO, poi.getInfo());
        return database.update(POIDatabaseHelper.TABLE_POI, values, POIDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(poi.getId())});
    }

    public int deletePOI(int id) {
        return database.delete(POIDatabaseHelper.TABLE_POI, POIDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<POI> getAllPOIs() {
        List<POI> poiList = new ArrayList<>();
        Cursor cursor = database.query(POIDatabaseHelper.TABLE_POI, null, null, null, null, null, POIDatabaseHelper.COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                POI poi = new POI();
                poi.setId(cursor.getInt(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_ID)));
                poi.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_TITLE)));
                poi.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_CATEGORY)));
                poi.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_RATING)));
                poi.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_PHOTO_PATH)));
                poi.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_LATITUDE)));
                poi.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_LONGITUDE)));
                poi.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_TIMESTAMP)));
                poi.setInfo(cursor.getString(cursor.getColumnIndexOrThrow(POIDatabaseHelper.COLUMN_INFO)));
                poiList.add(poi);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return poiList;
    }
}
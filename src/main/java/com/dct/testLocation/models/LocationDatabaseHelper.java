package com.dct.testLocation.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Hazen on 11/24/2014.
 */
public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    public static final String LOCATION_TABLE_NAME = "location";
    public static final String NAME_FIELD = "name";
    public static final String STATUS_FIELD = "status";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String LONGITUDE_FIELD = "longitude";
    public static final String RADIUS_FIELD = "radius";
    private static final String LOCATION_TABLE_CREATE =
            "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                    NAME_FIELD + " TEXT, " +
                    STATUS_FIELD + " TEXT, " +
                    LATITUDE_FIELD + " NUMBER," +
                    LONGITUDE_FIELD + " NUMBER," +
                    RADIUS_FIELD + " NUMBER" +
                    ");";

    public LocationDatabaseHelper(Context context)
    {
        super(context, "locationdb", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


}
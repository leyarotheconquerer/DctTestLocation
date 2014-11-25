package com.dct.testLocation.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ContentHandler;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Hazen on 11/24/2014.
 */
public class Location {
    public String name;
    public String status;

    public double latitude;
    public double longitude;
    public double radius;

    public static final Location EMPTY = new Location("Create New", "", 0, 0, 0);

    public Location(String name, String status, double latitude, double longitude, double radius) {
        this.name = name;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    /**
     * Gets a LatLng object based on the location.
     *
     * @return
     */
    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    /**
     * Gets a geofence object based on the location.
     *
     * TODO: Implement and test this.
     *
     * @return
     */
    public Geofence getGeofence() {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(this.name)
                .setCircularRegion(this.latitude, this.longitude, (float)this.radius)
                .build();

        return geofence;
    }

    /**
     * Inserts the location into the database.
     *
     * @param database
     */
    public void insertLocation(SQLiteDatabase database) {
        ContentValues content = new ContentValues();
        content.put(LocationDatabaseHelper.NAME_FIELD, this.name);
        content.put(LocationDatabaseHelper.STATUS_FIELD, this.status);
        content.put(LocationDatabaseHelper.LATITUDE_FIELD, this.latitude);
        content.put(LocationDatabaseHelper.LONGITUDE_FIELD, this.longitude);
        content.put(LocationDatabaseHelper.RADIUS_FIELD, this.radius);

        // Count the current number of rows with the given name
        Cursor count = database.rawQuery("SELECT COUNT(*) FROM " + LocationDatabaseHelper.LOCATION_TABLE_NAME + " WHERE " + LocationDatabaseHelper.NAME_FIELD + " = ?", new String[]{this.name});
        count.moveToFirst();

        // Prevent duplicates
        if(count.getInt(0) > 0) {
            database.update(LocationDatabaseHelper.LOCATION_TABLE_NAME, content, LocationDatabaseHelper.NAME_FIELD + "= ?", new String[]{this.name});
        }
        else {
            database.insert(LocationDatabaseHelper.LOCATION_TABLE_NAME, "", content);
        }
    }

    /**
     * Deletes the location from the database.
     *
     * @param database
     */
    public void deleteLocation(SQLiteDatabase database) {
        // Currently matches only on name... that better be unique
        database.delete(LocationDatabaseHelper.LOCATION_TABLE_NAME,
                LocationDatabaseHelper.NAME_FIELD + "= ?",
                new String[]{this.name});
    }

    /**
     * Gets the list of locations in the database.
     *
     * @param database
     * @return
     */
    public static ArrayList<Location> getLocations(SQLiteDatabase database)
    {
        String[] projection = {
            LocationDatabaseHelper.NAME_FIELD,
            LocationDatabaseHelper.STATUS_FIELD,
            LocationDatabaseHelper.LATITUDE_FIELD,
            LocationDatabaseHelper.LONGITUDE_FIELD,
            LocationDatabaseHelper.RADIUS_FIELD};

        Cursor rows = database.query(LocationDatabaseHelper.LOCATION_TABLE_NAME, projection, null, null, null, null, null, null);

        ArrayList<Location> locationList = new ArrayList<Location>();

        rows.moveToFirst();
        while(!rows.isAfterLast())
        {
            locationList.add(new Location(
                    rows.getString(0),
                    rows.getString(1),
                    rows.getDouble(2),
                    rows.getDouble(3),
                    rows.getDouble(4)));
            rows.moveToNext();
        }

        return locationList;
    }
}

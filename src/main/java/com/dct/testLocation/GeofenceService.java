package com.dct.testLocation;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dct.testLocation.models.Location;
import com.dct.testLocation.models.LocationDatabaseHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

/**
 * Service for handling Geofence events.
 */
public class GeofenceService extends IntentService {

    SQLiteDatabase database;

    public GeofenceService() {
        super("GeofenceService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Store the database for later use
        this.database = (new LocationDatabaseHelper(getApplicationContext())).getWritableDatabase();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (LocationClient.hasError(intent))
        {
            Log.e("com.dct.testLocation.Geofence", "Location Services error: " + Integer.toString(LocationClient.getErrorCode(intent)));
        } else {
            int transitionType = LocationClient.getGeofenceTransition(intent);

            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    List<Geofence> geofenceList = LocationClient.getTriggeringGeofences(intent);

                    for(Geofence geofence : geofenceList) {
                        // Construct a location from the geofence data
                        Location location = new Location(geofence, transitionType, this.database);

                        // Update the location in the database
                        location.insertLocation(this.database);
                    }

                    // Send a notification for testing purposes
                    NotificationCompat.Builder testBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Geofence Notification")
                            .setContentText("You have " + (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType ? "entered" : "exited") + " at least " + geofenceList.get(0).getRequestId());

                    Intent notificationIntent = new Intent(this, StatusActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(StatusActivity.class);
                    stackBuilder.addNextIntent(notificationIntent);

                    PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    testBuilder.setContentIntent(notificationPendingIntent);
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, testBuilder.build());

                    break;
                default:
                    Log.e("com.dct.testLocation.Geofence", "Geofence transition was invalid: " + Integer.toString(transitionType));
                    break;
            }
        }
    }
}

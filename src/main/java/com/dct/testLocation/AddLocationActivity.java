package com.dct.testLocation;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.dct.testLocation.models.Location;
import com.dct.testLocation.models.LocationDatabaseHelper;
import com.dct.testLocation.models.LocationsAdapter;

import java.util.ArrayList;
import java.util.List;


public class AddLocationActivity extends Activity implements
        AdapterView.OnItemSelectedListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {

    private GoogleMap locationMap;
    private Marker geofenceOrigin;
    private Circle geofenceBounds;

    private SQLiteDatabase database;
    private LocationClient locationClient;

    private EditText editLocationName;
    private Spinner spinnerLocation;

    public enum TransactionType { NONE, ADD, REMOVE_INTENT, REMOVE_LIST };

    // Arbitrary connection resolution constant
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private List<String> geofenceMyList;
    private List<Geofence> geofenceList;
    private TransactionType geofenceTransactionType;
    private boolean geofenceTransactionRunning;
    private PendingIntent geofencePendingIntent;
    private List<String> geofenceIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        // Initialization
        this.geofenceTransactionType = TransactionType.NONE;
        this.geofenceTransactionRunning = false;

        // Store a references to view items
        this.editLocationName = (EditText)findViewById(R.id.location_text_name);
        this.spinnerLocation = (Spinner)findViewById(R.id.location_spinner);
        this.spinnerLocation.setOnItemSelectedListener(this);

        this.locationMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.location_map)).getMap();

        this.geofenceMyList = new ArrayList<String>();

        this.locationMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Remove the remnants of the previous geofence markers
                if(geofenceOrigin != null) {
                    geofenceOrigin.remove();
                }

                if(geofenceBounds != null) {
                    geofenceBounds.remove();
                }

                // Set up the new origin position
                geofenceOrigin = locationMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Origin")
                        .draggable(true));
            }
        });

        this.locationMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(geofenceBounds != null)
                {
                    geofenceBounds.remove();
                }

                if(geofenceOrigin != null) {
                    double distance = SphericalUtil.computeDistanceBetween(geofenceOrigin.getPosition(), latLng);

                    geofenceBounds = locationMap.addCircle(new CircleOptions()
                            .center(geofenceOrigin.getPosition())
                            .radius(distance));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.menu_add_location, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check to see if GooglePlay services are available
        if(this.googlePlayLoaded()) {
            // Set up the title to show we have GooglePlay
            TextView title = (TextView)findViewById(R.id.location_title);
            title.setText(R.string.location_title_googleplay);

            // Get the database if needed
            if(this.database == null || !this.database.isOpen()) {
                this.database = new LocationDatabaseHelper(getApplicationContext()).getWritableDatabase();
            }

            // Populate the locations spinner
            this.updateLocationSpinner();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(this.database != null && this.database.isOpen()) {
            database.close();
        }
    }

    /**
     * Adds a location to the database.
     *
     * @param view
     */
    public void onAddLocation(View view)
    {
        // Get the new name
        Editable text = this.editLocationName.getText();
        String name = text.toString();

        // Create a location object
        Location location = new Location(name, "unknown", geofenceOrigin.getPosition().latitude, geofenceOrigin.getPosition().longitude, geofenceBounds.getRadius());

        // Add the location to the database
        location.insertLocation(this.database);

        // Update the location spinner
        this.updateLocationSpinner();

        // Select the new location
        this.spinnerLocation.setSelection(this.findLocationInSpinner(location, this.spinnerLocation));

        // Reset location inputs
        this.resetLocationInputs();

        // Add the location to the geofence list
        ArrayList<Geofence> geofences = new ArrayList<Geofence>();
        geofences.add(location.getGeofence());
        this.addGeofences(geofences);
    }

    /**
     * Deletes the selected location
     *
     * @param view
     */
    public void onDeleteLocation(View view) {
        Location location = (Location)this.spinnerLocation.getSelectedItem();

        // Delete the location
        location.deleteLocation(this.database);

        // Update the location spinner
        this.updateLocationSpinner();

        // Reset location inputs
        this.resetLocationInputs();

        // Remove the corresponding geofences
        ArrayList<String> geofences = new ArrayList<String>();
        geofences.add(location.name);
        this.removeGeofences(geofences);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Reset location inputs
        this.resetLocationInputs();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (this.geofenceTransactionType)
        {
            case ADD:
                // Add those geofences!!!
                locationClient.addGeofences(this.geofenceList, this.getGeofenceIntent(), this);
                for(Geofence geofence:geofenceList) {
                    this.geofenceMyList.add(geofence.getRequestId());
                }
                break;
            case REMOVE_INTENT:
                // Remove those geofences!!!
                locationClient.removeGeofences(this.geofencePendingIntent, this);
                break;
            case REMOVE_LIST:
                // Remove those geofences!!!
                locationClient.removeGeofences(this.geofenceIdList, this);
                for(String id:geofenceIdList) {
                    for(String geofenceId: this.geofenceMyList) {
                        if(id.equals(geofenceId)) {
                            this.geofenceMyList.remove(geofenceId);
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDisconnected() {
        this.geofenceTransactionRunning = false;
        this.locationClient = null;
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if(statusCode != LocationStatusCodes.SUCCESS) {
            Log.e("com.dct.testLocation.Geofence","Failed to add geofences.");
        }

        this.geofenceTransactionRunning = false;
        this.locationClient.disconnect();
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] strings) {

        if(statusCode != LocationStatusCodes.SUCCESS) {
            Log.e("com.dct.testLocation.Geofence", "Failed to remove list of geofences.");
        }

        this.geofenceTransactionRunning = false;
        this.locationClient.disconnect();
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
        if(statusCode != LocationStatusCodes.SUCCESS) {
            Log.e("com.dct.testLocation.Geofence","Failed to remove geofences by intent.");
        }

        this.geofenceTransactionRunning = false;
        this.locationClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        this.geofenceTransactionRunning = false;

        // Attempt to resolve the connection failure
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            errorDialog.show();
        }
    }

    public void addGeofences(ArrayList<Geofence> geofenceList)
    {
        TransactionType temp = this.geofenceTransactionType;
        List<Geofence> tempList = this.geofenceList;

        this.geofenceTransactionType = TransactionType.ADD;
        this.geofenceList = geofenceList;

        // We need google play
        if(!this.googlePlayLoaded())
        {
            return;
        }

        // Don't worry location client... we'll listen to you...
        this.locationClient = new LocationClient(this, this, this);

        // No race conditions please
        if(!this.geofenceTransactionRunning) {
            // Let's add some stuff
            this.geofenceTransactionRunning = true;

            this.locationClient.connect();
        }
        else {
            // Reset transaction variables
            this.geofenceTransactionType = temp;
            this.geofenceList = tempList;

            this.locationClient.disconnect();

            // Retry
            android.os.SystemClock.sleep(1000); // Sleep for a second
            this.addGeofences(geofenceList);
        }
    }

    public void removeGeofences (PendingIntent requestIntent) {
        TransactionType temp = this.geofenceTransactionType;
        this.geofenceTransactionType = TransactionType.REMOVE_INTENT;

        if(!this.googlePlayLoaded()) {
            return;
        }

        // Store the given intent for later (we'll need it)
        this.geofencePendingIntent = requestIntent;

        // Once again, we listen. Ever so closely. To all the things.
        this.locationClient = new LocationClient(this, this, this);
        // Avoid race conditions
        if(!this.geofenceTransactionRunning)
        {
            this.geofenceTransactionRunning = true;
            this.locationClient.connect();
        }
        else {
            this.geofenceTransactionType = temp;

            this.locationClient.disconnect();

            android.os.SystemClock.sleep(1000);
            this.removeGeofences(requestIntent);
        }
    }

    public void removeGeofences(List<String> geofenceIdList) {
        TransactionType temp = this.geofenceTransactionType;
        List<String> tempList = this.geofenceIdList;

        this.geofenceTransactionType = TransactionType.REMOVE_LIST;

        if(!this.googlePlayLoaded()) {
            return;
        }

        this.geofenceIdList = geofenceIdList;

        // This, this, this is listening
        this.locationClient = new LocationClient(this, this, this);
        if(!this.geofenceTransactionRunning) {
            this.geofenceTransactionRunning = true;
            this.locationClient.connect();
        }
        else {
            this.geofenceTransactionType = temp;

            this.locationClient.disconnect();

            android.os.SystemClock.sleep(1000);
            this.removeGeofences(geofenceIdList);
        }
    }

    /**
     * Updates the contents of the location spinner.
     */
    private void updateLocationSpinner() {
        ArrayList<Location> locations = Location.getLocations(this.database);
        LocationsAdapter adapter = new LocationsAdapter(getApplicationContext(), locations);
        this.spinnerLocation.setAdapter(adapter);
    }

    /**
     * Resets the location inputs
     */
    private void resetLocationInputs() {
        // Clear all inputs
        this.editLocationName.setText("");
        if(this.geofenceOrigin != null) {
            this.geofenceOrigin.remove();
        }
        if(this.geofenceBounds != null) {
            this.geofenceBounds.remove();
        }

        // Get the current location
        Location currentLocation = (Location)this.spinnerLocation.getSelectedItem();

        // Update the shown location based on selection
        this.geofenceOrigin = locationMap.addMarker(new MarkerOptions()
                .position(currentLocation.getLatLng())
                .title(currentLocation.name));

        this.geofenceBounds = locationMap.addCircle(new CircleOptions()
                .center(currentLocation.getLatLng())
                .radius(currentLocation.radius));

        // Calculate the corner LatLng objects
        LatLng northeast = SphericalUtil.computeOffset(currentLocation.getLatLng(), currentLocation.radius * 1.5, 45.0f);
        LatLng southwest = SphericalUtil.computeOffset(currentLocation.getLatLng(), currentLocation.radius * 1.5, 225.0f);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

        // Move the view to the new location
        this.locationMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
    }

    private boolean googlePlayLoaded() {
        // Check to see if GooglePlay services are available
        int googlePlayAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(googlePlayAvailable != ConnectionResult.SUCCESS)
        {
            // Not sure where the 0 ends up
            GooglePlayServicesUtil.getErrorDialog(googlePlayAvailable, this, 0).show();

            // Presumably, this will show the dialog... though I haven't had a chance to see if this works
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Checks if the location map is loaded.
     *
     * @return
     */
    private boolean mapLoaded() {
        if (this.locationMap == null)
        {
            locationMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.location_map)).getMap();

            if(locationMap == null)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the index of an location in the spinner.
     *
     * @param spinner
     * @return The index of the location or -1 if the location wasn't found
     */
    private int findLocationInSpinner(Location location, Spinner spinner) {
        for(int i = 0; i < spinner.getCount(); i++) {
            if(((Location)spinner.getItemAtPosition(i)).name.equals(location.name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the intent by which GooglePlay services will contact us.
     *
     * @return
     */
    private PendingIntent getGeofenceIntent()
    {
        // Construct an Intent to notify the Geofence service when necessary
        Intent intent = new Intent(this, GeofenceService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

package com.hazen.testLocation;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import com.hazen.testLocation.models.Location;
import com.hazen.testLocation.models.LocationDatabaseHelper;
import com.hazen.testLocation.models.LocationsAdapter;

import java.util.ArrayList;


public class AddLocationActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private GoogleMap locationMap;
    private Marker geofenceOrigin;
    private Circle geofenceBounds;

    private SQLiteDatabase database;

    private EditText editLocationName;
    private Spinner selectLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        // Store a references to view items
        this.editLocationName = (EditText)findViewById(R.id.location_text_name);
        this.selectLocation = (Spinner)findViewById(R.id.location_spinner);
        this.selectLocation.setOnItemSelectedListener(this);

        this.locationMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.location_map)).getMap();

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
        int googlePlayAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(googlePlayAvailable != ConnectionResult.SUCCESS)
        {
            // Not sure where the 0 ends up
            GooglePlayServicesUtil.getErrorDialog(googlePlayAvailable, this, 0).show();

            // Presumably, this will show the dialog... though I haven't had a chance to see if this works
        }
        else
        {
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
        this.selectLocation.setSelection(this.findLocationInSpinner(location, this.selectLocation));

        // Reset location inputs
        this.resetLocationInputs();

    }

    /**
     * Deletes the selected location
     *
     * @param view
     */
    public void onDeleteLocation(View view) {
        Location location = (Location)this.selectLocation.getSelectedItem();

        // Delete the location
        location.deleteLocation(this.database);

        // Update the location spinner
        this.updateLocationSpinner();

        // Reset location inputs
        this.resetLocationInputs();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Reset location inputs
        this.resetLocationInputs();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Updates the contents of the location spinner.
     */
    private void updateLocationSpinner() {
        ArrayList<Location> locations = Location.getLocations(this.database);
        LocationsAdapter adapter = new LocationsAdapter(getApplicationContext(), locations);
        this.selectLocation.setAdapter(adapter);
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
        Location currentLocation = (Location)this.selectLocation.getSelectedItem();

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
}

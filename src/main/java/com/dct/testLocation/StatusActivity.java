package com.dct.testLocation;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.dct.testLocation.models.Location;
import com.dct.testLocation.models.LocationDatabaseHelper;
import com.dct.testLocation.models.LocationsAdapter;

import java.util.ArrayList;


public class StatusActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SQLiteDatabase database = (new LocationDatabaseHelper(getApplicationContext())).getWritableDatabase();

        ArrayList<Location> locations = Location.getLocations(database);

        LocationsAdapter locationsAdapter = new LocationsAdapter(getApplicationContext(), locations);

        ListView listView = (ListView)findViewById(R.id.status_list);

        listView.setAdapter(locationsAdapter);
    }
}

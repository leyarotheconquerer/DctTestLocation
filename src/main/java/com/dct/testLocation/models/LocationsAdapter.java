package com.dct.testLocation.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dct.testLocation.R;

import java.util.ArrayList;

/**
 * Created by Hazen on 11/24/2014.
 */
public class LocationsAdapter extends ArrayAdapter<Location> {
    public LocationsAdapter(Context context, ArrayList<Location> locations) {
        super(context, R.layout.list_location, locations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent)
    {
        // Get the item
        Location location = getItem(position);

        // Inflate a new view if no previous view was given
        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_location, parent, false);
        }

        // Get the view elements to fill
        TextView locationName = (TextView) convertView.findViewById(R.id.list_location_name);
        TextView locationStatus = (TextView) convertView.findViewById(R.id.list_location_status);

        // Fill the view elements
        locationName.setText(location.name);
        locationStatus.setText(location.status);

        return convertView;
    }
}

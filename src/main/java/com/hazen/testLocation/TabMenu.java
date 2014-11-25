package com.hazen.testLocation;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabMenu.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabMenu extends Fragment implements OnClickListener {

    /*private OnFragmentInteractionListener mListener;*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TabMenu.
     */
    // TODO: Rename and change types and number of parameters
    public static TabMenu newInstance() {
        TabMenu fragment = new TabMenu();
        return fragment;
    }

    public TabMenu() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_menu, container, false);

        // Get the buttons
        Button statusButton = (Button)view.findViewById(R.id.tab_button_status);
        Button addLocationButton = (Button)view.findViewById(R.id.tab_button_add_location);

        // Reset the click listener to this fragment
        statusButton.setOnClickListener(this);
        addLocationButton.setOnClickListener(this);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        /*if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tab_button_status:
                this.onStatus(view);
                break;
            case R.id.tab_button_add_location:
                this.onAddLocation(view);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        /*mListener = null;*/
    }

    /**
     * Starts the Status Activity.
     *
     * @param view
     */
    public void onStatus(View view) {
        if(this.getActivity().getClass() != StatusActivity.class) {
            Intent intent = new Intent(this.getActivity(), StatusActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Starts the Add Location Activity.
     *
     * @param view
     */
    public void onAddLocation(View view) {
        if(this.getActivity().getClass() != AddLocationActivity.class)
        {
            Intent intent = new Intent(this.getActivity(), AddLocationActivity.class);
            startActivity(intent);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }*/

}

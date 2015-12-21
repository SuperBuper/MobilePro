package com.processmap.mobilepro.ui.location;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.processmap.mobilepro.R;


public class LocationFragment extends Fragment {

    View fragment;

    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment = (View) inflater.inflate(R.layout.fragment_location, container, false);


        return fragment;
    }

}

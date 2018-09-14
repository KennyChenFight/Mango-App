package com.csim.scu.aibox.util;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.csim.scu.aibox.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by kenny on 2018/8/8.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Activity context;

    public CustomInfoWindowAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.custom_info_window, null);
//
//        TextView tvPlaceName = view.findViewById(R.id.tvPlaceName);
//        TextView tvDistance = view.findViewById(R.id.tvDistance);
//        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
//        Button btYes = view.findViewById(R.id.btYes);
//        Button btNo = view.findViewById(R.id.btNo);

        return view;
    }
}

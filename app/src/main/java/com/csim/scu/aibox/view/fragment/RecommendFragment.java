package com.csim.scu.aibox.view.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.network.Url;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RecommendFragment extends Fragment {

    private Map<String, String> needAndWeather = new HashMap<>();
    private TextView tvCity;
    private TextView tvWeatherDesc;
    private TextView tvTemp;
    private TextView tvMaxTemp;
    private TextView tvMinTemp;
    private TextView tvRainProb;
    private TextView tvWater;
    private TextView tvKcal;
    private LinearLayout llWeatherBackground;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);
        needAndWeather.put("city", this.getArguments().getString("city"));
        needAndWeather.put("weatherDesc", this.getArguments().getString("weatherDesc"));
        needAndWeather.put("water", this.getArguments().getString("water"));
        needAndWeather.put("cal", this.getArguments().getString("cal"));
        needAndWeather.put("highTemp", this.getArguments().getString("highTemp"));
        needAndWeather.put("lowTemp", this.getArguments().getString("lowTemp"));
        needAndWeather.put("averTemp", this.getArguments().getString("averTemp"));
        needAndWeather.put("rainPro", this.getArguments().getString("rainPro"));
        needAndWeather.put("info", this.getArguments().getString("info"));
        findViews(view);
        return view;
    }


    private void findViews(View view) {
        tvCity = view.findViewById(R.id.tvCity);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvRainProb = view.findViewById(R.id.tvRainProb);
        tvMaxTemp = view.findViewById(R.id.tvMaxTemp);
        tvMinTemp = view.findViewById(R.id.tvMinTemp);
        tvWater = view.findViewById(R.id.tvWater);
        tvKcal = view.findViewById(R.id.tvKcal);
        llWeatherBackground = view.findViewById(R.id.llWeatherBackground);
        setNeedAndWeatherInfo();
    }

    private void setNeedAndWeatherInfo() {
        tvCity.setText(needAndWeather.get("city"));
        tvWeatherDesc.setText(needAndWeather.get("weatherDesc"));
        tvTemp.setText(needAndWeather.get("averTemp"));
        tvRainProb.setText(needAndWeather.get("rainPro")+ " %");
        tvTemp.setText(needAndWeather.get("averTemp") + " °c");
        tvMaxTemp.setText(needAndWeather.get("highTemp") + " °c");
        tvMinTemp.setText(needAndWeather.get("lowTemp") + " °c");
        tvWater.setText(needAndWeather.get("water") + " ml");
        tvKcal.setText(needAndWeather.get("cal") + " kcal");
        switch (needAndWeather.get("info")) {
            case "sunny":
                llWeatherBackground.setBackgroundResource(R.drawable.sunny);
                break;
            case "rainy":
                llWeatherBackground.setBackgroundResource(R.drawable.rainy);
                break;
            case "cloudy":
                llWeatherBackground.setBackgroundResource(R.drawable.cloud);
                break;
        }
    }

}

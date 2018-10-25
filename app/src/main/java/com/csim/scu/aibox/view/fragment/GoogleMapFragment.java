package com.csim.scu.aibox.view.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beyondar.android.world.GeoObject;
import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.BusStation;
import com.csim.scu.aibox.model.GasStation;
import com.csim.scu.aibox.model.MRT;
import com.csim.scu.aibox.model.OpenActivity;
import com.csim.scu.aibox.model.WowLocation;
import com.csim.scu.aibox.network.GoogleMapNearbyDataParser;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.util.CustomInfoWindowAdapter;
import com.csim.scu.aibox.view.activity.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;


public class GoogleMapFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private MapFragment mapFragment;
    private Marker myMarker;
    private Location lastLocation;
    private HashMap<String, String> placeDetail;
    private ArrayList<OpenActivity> openActivityList;
    private List<HashMap<String, String>> nearbyPlaceInfo = new ArrayList<>();
    private android.app.AlertDialog alertDialog = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_googlemap, container, false);
        if ((HashMap<String, String>) this.getArguments().getSerializable("placeDetail") != null) {
            placeDetail = (HashMap<String, String>) this.getArguments().getSerializable("placeDetail");
        }
        // for 特定地區的活動查詢
        else {
            openActivityList = new Gson().fromJson(this.getArguments().getString("openActivityForRegion"), new TypeToken<ArrayList<OpenActivity>>(){}.getType());
        }
        connectGoogleApiClient();
        mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Logger.d();
    }

    // 連結GoogleApiClient取得LocationServices
    private void connectGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect();
        Logger.d();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
        updateMyMarkerLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setSmallestDisplacement(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);

            if (placeDetail != null) {
                if (placeDetail.get("region").equals("c")) {
                    WowLocationTask wowLocationTask = new WowLocationTask();
                    wowLocationTask.execute();
//                    switch (placeDetail.get("placeType")) {
//                        case "老人共餐":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "友善餐廳":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "長照中心":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "餐廳":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "旅館":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "安養中心":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "運動中心":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "銀髮友好站":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "美術館":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                        case "樂齡中心":
//                            wowLocationTask = new WowLocationTask();
//                            wowLocationTask.execute();
//                            break;
//                    }
                }
                else {
                    WowLocationTask wowLocationTask;
                    switch (placeDetail.get("placeType")) {
                        case "公車站":
                            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).build();
                            alertDialog.show();
                            BusStationTask busStationTask = new BusStationTask();
                            busStationTask.execute();
                            break;
                        case "捷運站":
                            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).build();
                            alertDialog.show();
                            MRTTask mrtTask = new MRTTask();
                            mrtTask.execute();
                            break;
                        case "加油站":
                            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).build();
                            alertDialog.show();
                            GasStationTask gasStationTask = new GasStationTask();
                            gasStationTask.execute();
                            break;
                        case "老人共餐":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "友善餐廳":
                            Logger.d("test");
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "長照中心":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "餐廳":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "旅館":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "安養中心":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "運動中心":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "銀髮友好站":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "美術館":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        case "樂齡中心":
                            wowLocationTask = new WowLocationTask();
                            wowLocationTask.execute();
                            break;
                        default:
                            String url = getNearbyPlacesUrl();
                            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                            getNearbyPlacesData.execute(url);
                    }
                }
            }
            else {
                showOpenActivityMarker();
            }
            if (lastLocation != null) {
                updateMyMarkerLocation();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Logger.d(String.valueOf(i));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.d(connectionResult.getErrorMessage());
        googleApiClient.connect();
    }

    private void updateMyMarkerLocation() {
        if (myMarker == null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            myMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("我的位置"));
            myMarker.showInfoWindow();
        }
    }

    private String getNearbyPlacesUrl() {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + placeDetail.get("lat") + "," + placeDetail.get("lng"));
        stringBuilder.append("&radius=" + placeDetail.get("distance"));
        stringBuilder.append("&key=" + getResources().getString(R.string.google_place_web_service_key));
        Logger.d("getNearbyPlacesUrl:" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private class GetNearbyPlacesData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer sb = new StringBuffer();

                String line = "";
                while((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                br.close();
                inputStream.close();
                urlConnection.disconnect();
                Logger.d("success get nearby place data");
            } catch (Exception ex) {
                Logger.e(ex.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            GoogleMapNearbyDataParser googleMapNearbyDataParser = new GoogleMapNearbyDataParser();
            int drawableType = chooseType();
            nearbyPlaceInfo = googleMapNearbyDataParser.parseNearbyPlaces(s, true, drawableType);
            showPlaceMarker();
        }
    }

    private int chooseType() {
        int type = -1;
        switch (placeDetail.get("placeType")) {
            case "餐廳":
                type = R.drawable.navigation_food;
                break;
            case "公車站":
                type = R.drawable.navigation_bus;
                break;
            case "醫院":
            case "診所":
                type = R.drawable.navigation_hospital;
                break;
            case "住宿":
                type = R.drawable.navigation_hotel;
                break;
            case "公園":
                type = R.drawable.navigation_park;
                break;
            case "停車場":
                type = R.drawable.navigation_parking;
                break;
            case "郵局":
                type = R.drawable.navigation_postoffice;
                break;
            case "便利商店":
                type = R.drawable.navigation_store;
                break;
            default:
                type = R.drawable.navigation;
                break;
        }
        return type;
    }

    private void showPlaceMarker() {
        Comparator<HashMap<String, String>> sortByRating = new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> m1, HashMap<String, String> m2) {
                return -m1.get("rating").compareTo(m2.get("rating"));
            }
        };
        Collections.sort(nearbyPlaceInfo, sortByRating);

        for (int i = 0; i < nearbyPlaceInfo.size(); i++) {
            HashMap<String, String> map = nearbyPlaceInfo.get(i);
            if (i > 10) {
                break;
            }
            else {
                String lat = map.get("lat");
                String lng = map.get("lng");
                String place_name = map.get("place_name");
                String rating = map.get("rating");

                GeoObject geoObject = new GeoObject(i);
                geoObject.setGeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
                String distance = String.valueOf(new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m";

                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                googleMap.addMarker(new MarkerOptions().position(latLng).title(place_name+","+rating+","+distance+","+lat+","+lng));
            }
        }
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(Double.parseDouble(placeDetail.get("lat")), Double.parseDouble(placeDetail.get("lng"))))
                .zoom(16)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().equals("我的位置")) {
                    String[] placeDetail = marker.getTitle().split(",");
                    final String place_name = placeDetail[0];
                    String rating = placeDetail[1];
                    String distance = placeDetail[2];
                    final String lat = placeDetail[3];
                    final String lng = placeDetail[4];
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.location_dialog);
                    TextView tvPlaceName = dialog.findViewById(R.id.tvPlaceName);
                    tvPlaceName.setText(place_name);
                    TextView tvDistance = dialog.findViewById(R.id.tvDistance);
                    tvDistance.setText(distance);
                    RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                    ratingBar.setRating(Float.parseFloat(rating));
                    Button btYes = dialog.findViewById(R.id.btYes);
                    final Button btNo = dialog.findViewById(R.id.btNo);
                    btYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", lastLocation.getLatitude(),
                                    lastLocation.getLongitude(), lat, lng);
                            Intent intent = new Intent();
                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                    btNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    return true;
                }
                else {
                    return false;
                }
            }
        });
    }
    // todo 尚未
    // show出特定區域活動的Marker測試
    private void showOpenActivityMarker() {
        String lat = "";
        String lng = "";
        for (int i = 0; i < openActivityList.size(); i++) {
            OpenActivity openActivity = openActivityList.get(i);
            lat = openActivity.getLatitude();
            lng = openActivity.getLongitude();
            String place_name = openActivity.getTitle();

            GeoObject geoObject = new GeoObject(i);
            geoObject.setGeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
            String distance = String.valueOf(new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m";

            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            googleMap.addMarker(new MarkerOptions().position(latLng).title(place_name+","+distance+","+lat+","+lng));
        }
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)))
                .zoom(16)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().equals("我的位置")) {
                    String[] placeDetail = marker.getTitle().split(",");
                    final String place_name = placeDetail[0];
                    String distance = placeDetail[1];
                    final String lat = placeDetail[2];
                    final String lng = placeDetail[3];
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.location_dialog);
                    TextView tvPlaceName = dialog.findViewById(R.id.tvPlaceName);
                    tvPlaceName.setText(place_name);
                    TextView tvDistance = dialog.findViewById(R.id.tvDistance);
                    tvDistance.setText(distance);
                    RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                    ratingBar.setRating(5);
                    Button btYes = dialog.findViewById(R.id.btYes);
                    final Button btNo = dialog.findViewById(R.id.btNo);
                    btYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", lastLocation.getLatitude(),
                                    lastLocation.getLongitude(), lat, lng);
                            Intent intent = new Intent();
                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                    btNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    return true;
                }
                else {
                    return false;
                }
            }
        });
    }

    //todo 尚未測試
    private class BusStationTask extends AsyncTask<String, Void, LatLng> {


        @Override
        protected LatLng doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                String region = placeDetail.get("region");
                URL url = new URL(Url.baseUrl + Url.getBusStation + region);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();

                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                httpURLConnection.disconnect();
                reader.close();

                LatLng latLng = null;
                try {
                    if (jsonObject.getString("status").equals("200")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        double lat = 0;
                        double lng = 0;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            final String place_name = jsonObject1.getJSONObject("StopName").getString("Zh_tw");
                            Logger.d("公車站名:" + place_name);
                            lat = jsonObject1.getJSONObject("StopPosition").getDouble("PositionLat");
                            lng = jsonObject1.getJSONObject("StopPosition").getDouble("PositionLon");

                            GeoObject geoObject = new GeoObject(i);
                            geoObject.setGeoPosition(lat, lng);
                            final String distance = String.valueOf(new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m";
                            double dis = new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
//                        if (Double.parseDouble(distance) < 1000) {
                            latLng = new LatLng(lat, lng);
                            final LatLng finalLatLng = latLng;
                            final double finalLat = lat;
                            final double finalLng = lng;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    googleMap.addMarker(new MarkerOptions().position(finalLatLng).title(place_name+","+distance+","+ finalLat +","+ finalLng));
                                }
                            });
//                        }
                        }
                    }
                    else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e.toString());
                }
                alertDialog.dismiss();
                return latLng;

            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            CameraPosition camPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (!marker.getTitle().equals("我的位置")) {
                        String[] placeDetail = marker.getTitle().split(",");
                        final String place_name = placeDetail[0];
                        String distance = placeDetail[1];
                        final String lat = placeDetail[2];
                        final String lng = placeDetail[3];
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.location_dialog);
                        TextView tvPlaceName = dialog.findViewById(R.id.tvPlaceName);
                        tvPlaceName.setText(place_name);
                        TextView tvDistance = dialog.findViewById(R.id.tvDistance);
                        tvDistance.setText(distance);
                        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                        ratingBar.setRating(5);
                        Button btYes = dialog.findViewById(R.id.btYes);
                        final Button btNo = dialog.findViewById(R.id.btNo);
                        btYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", lastLocation.getLatitude(),
                                        lastLocation.getLongitude(), lat, lng);
                                Intent intent = new Intent();
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        btNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });
        }
    }

    //todo 尚未測試
    private class MRTTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected LatLng doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.getMRT);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();

                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                httpURLConnection.disconnect();
                reader.close();

                LatLng latLng = null;
                try {
                    if (jsonObject.getString("status").equals("200")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        double lat = 0;
                        double lng = 0;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            lat = jsonObject1.getJSONObject("properties").getDouble("經度");
                            lng = jsonObject1.getJSONObject("properties").getDouble("緯度");
                            final String place_name = jsonObject1.getJSONObject("properties").getString("出入口名稱");
                            Logger.d("捷運站名:" + place_name);

                            GeoObject geoObject = new GeoObject(i);
                            geoObject.setGeoPosition(lat, lng);
                            final String distance = String.valueOf(new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m";
                            double dis = new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
//                        if (Double.parseDouble(distance) < 1000) {
                            latLng = new LatLng(lat, lng);
                            final LatLng finalLatLng = latLng;
                            final double finalLat = lat;
                            final double finalLng = lng;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    googleMap.addMarker(new MarkerOptions().position(finalLatLng).title(place_name+","+distance+","+ finalLat +","+ finalLng));
                                }
                            });
//                        }
                        }
                    }
                    else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e.toString());
                }
                alertDialog.dismiss();
                return latLng;

            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            CameraPosition camPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (!marker.getTitle().equals("我的位置")) {
                        String[] placeDetail = marker.getTitle().split(",");
                        final String place_name = placeDetail[0];
                        String distance = placeDetail[1];
                        final String lat = placeDetail[2];
                        final String lng = placeDetail[3];
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.location_dialog);
                        TextView tvPlaceName = dialog.findViewById(R.id.tvPlaceName);
                        tvPlaceName.setText(place_name);
                        TextView tvDistance = dialog.findViewById(R.id.tvDistance);
                        tvDistance.setText(distance);
                        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                        ratingBar.setRating(5);
                        Button btYes = dialog.findViewById(R.id.btYes);
                        final Button btNo = dialog.findViewById(R.id.btNo);
                        btYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", lastLocation.getLatitude(),
                                        lastLocation.getLongitude(), lat, lng);
                                Intent intent = new Intent();
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        btNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });
        }
    }

    //todo 尚未測試
    private class GasStationTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected LatLng doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.getGasStation);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();

                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                httpURLConnection.disconnect();
                reader.close();
                LatLng latLng = null;
                try {
                    if (jsonObject.getString("status").equals("200")) {
                        double lat = 0;
                        double lng = 0;
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            lat = jsonObject1.getDouble("經度");
                            lng = jsonObject1.getDouble("緯度");
                            final String place_name = jsonObject1.getString("站名");
                            Logger.d("加油站名:" + place_name);

                            GeoObject geoObject = new GeoObject(i);
                            geoObject.setGeoPosition(lat, lng);
                            final String distance = String.valueOf(new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m";
                            double dis = new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
//                        if (Double.parseDouble(distance) < 1000) {
                            latLng = new LatLng(lat, lng);
                            final double finalLat = lat;
                            final double finalLng = lng;
                            final LatLng finalLatLng = latLng;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    googleMap.addMarker(new MarkerOptions().position(finalLatLng).title(place_name+","+distance+","+ finalLat +","+ finalLng));
                                }
                            });
//                        }
                        }
                    }
                    else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e.toString());
                }

                alertDialog.dismiss();
                return latLng;

            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            CameraPosition camPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (!marker.getTitle().equals("我的位置")) {
                        String[] placeDetail = marker.getTitle().split(",");
                        final String place_name = placeDetail[0];
                        String distance = placeDetail[1];
                        final String lat = placeDetail[2];
                        final String lng = placeDetail[3];
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.location_dialog);
                        TextView tvPlaceName = dialog.findViewById(R.id.tvPlaceName);
                        tvPlaceName.setText(place_name);
                        TextView tvDistance = dialog.findViewById(R.id.tvDistance);
                        tvDistance.setText(distance);
                        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                        ratingBar.setRating(5);
                        Button btYes = dialog.findViewById(R.id.btYes);
                        final Button btNo = dialog.findViewById(R.id.btNo);
                        btYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", lastLocation.getLatitude(),
                                        lastLocation.getLongitude(), lat, lng);
                                Intent intent = new Intent();
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        btNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });
        }
    }

    private class WowLocationTask extends AsyncTask<Void, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.getWowLocation);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
               // httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                return (JSONObject) jsonObject;
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                Logger.d(jsonArray.length()+"");
                double lat = 0;
                double lng = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    WowLocation wowLocation = new WowLocation(jsonObject1.getString("type"), jsonObject1.getString("name"), jsonObject1.getString("addr"), jsonObject1.getString("phone"));

                    List<Address> addresses = null;
                    Geocoder geocoder = new Geocoder(getContext());
                    addresses = geocoder.getFromLocationName(wowLocation.getAddr(), 1);
                    Address address = addresses.get(0);

                    lat = address.getLatitude();
                    lng = address.getLongitude();
                    String place_name = wowLocation.getName();

                    GeoObject geoObject = new GeoObject(i);
                    geoObject.setGeoPosition(lat, lng);
                    String distance = String.valueOf(new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m";
                    double dis = new BigDecimal(Double.toString(geoObject.calculateDistanceMeters(lastLocation.getLongitude(), lastLocation.getLatitude()))).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
//                        if (Double.parseDouble(distance) < 1000) {
                    LatLng latLng = new LatLng(lat, lng);
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(place_name+","+distance+","+lat+","+lng));
                }
                CameraPosition camPosition = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng))
                        .zoom(16)
                        .build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (!marker.getTitle().equals("我的位置")) {
                            String[] placeDetail = marker.getTitle().split(",");
                            final String place_name = placeDetail[0];
                            String distance = placeDetail[1];
                            final String lat = placeDetail[2];
                            final String lng = placeDetail[3];
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.location_dialog);
                            TextView tvPlaceName = dialog.findViewById(R.id.tvPlaceName);
                            tvPlaceName.setText(place_name);
                            TextView tvDistance = dialog.findViewById(R.id.tvDistance);
                            tvDistance.setText(distance);
                            RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                            ratingBar.setRating(5);
                            Button btYes = dialog.findViewById(R.id.btYes);
                            final Button btNo = dialog.findViewById(R.id.btNo);
                            btYes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", lastLocation.getLatitude(),
                                            lastLocation.getLongitude(), lat, lng);
                                    Intent intent = new Intent();
                                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(url));
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            });
                            btNo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                });
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }
}

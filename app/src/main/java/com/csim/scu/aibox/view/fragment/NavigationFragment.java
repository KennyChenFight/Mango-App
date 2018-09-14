package com.csim.scu.aibox.view.fragment;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beyondar.android.fragment.BeyondarFragment;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.view.OnTouchBeyondarViewListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.Direction;
import com.csim.scu.aibox.network.GoogleMapNearbyDataParser;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.util.LocationCalc;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NavigationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FragmentManager fragmentManager;
    private HashMap<String, String> specificPlace;
    private NavigationFragment navigationFragment;
    private boolean isDefine;
    private int drawableType;
    private Location myLastLocation;
    private GoogleApiClient googleApiClient;
    private BeyondarFragment beyondarFragment;
    private World world;
    private List<HashMap<String, String>> nearbyPlaceInfo = new ArrayList<>();
    private ImageButton ibSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        if (this.getArguments().getSerializable("place_location") == null) {
            isDefine = this.getArguments().getBoolean("define");
            drawableType = this.getArguments().getInt("type");
        }
        else {
            specificPlace = (HashMap<String, String>) this.getArguments().getSerializable("place_location");
        }
        fragmentManager = getFragmentManager();
        setGoogleApiClient();
        findViews(view);
        return view;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            myLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setSmallestDisplacement(100);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
            Logger.d("connect to googleApiClient");
            if (specificPlace != null) {
                googleApiClient.disconnect();
                String url = getDirectionApiUrl(specificPlace.get("lat"), specificPlace.get("lng"));
                DirectionTask directionTask = new DirectionTask();
                directionTask.execute(url);
                Toast.makeText(getActivity(), "讀取中", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    private void findViews(View view) {
        ibSearch = view.findViewById(R.id.ibSearch);
        if (this.getArguments().getBoolean("which")) {
            ibSearch.setVisibility(View.GONE);
        }
        else {
            ibSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    TypeFragment typeFragment = new TypeFragment();
                    fragmentTransaction.add(R.id.container, typeFragment, typeFragment.getClass().getName());
                    fragmentTransaction.addToBackStack(typeFragment.getClass().getName());
                    fragmentManager.executePendingTransactions();
                    fragmentTransaction.commit();
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.d("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.d(connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        myLastLocation = location;
        if (specificPlace == null) {
            String url = getNearbyPlacesUrl(myLastLocation.getLatitude(), myLastLocation.getLongitude());
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
            getNearbyPlacesData.execute(url);
        }
    }

    private void buildArWorld() {
        world = new World(getActivity());
        world.setDefaultImage(R.drawable.navigation_food);
        world.setGeoPosition(myLastLocation.getLatitude(), myLastLocation.getLongitude());
        beyondarFragment = (BeyondarFragment) getChildFragmentManager().findFragmentById(R.id.beyondarFragment);

        List<GeoObject> geoObjectList = new ArrayList<>();
        int count = 0;
        for (Map<String, String> map : nearbyPlaceInfo) {
            String lat = map.get("lat");
            String lng = map.get("lng");
            String place_name = map.get("place_name");
            String rating = map.get("rating");
            int typeID = Integer.parseInt(map.get("typeID"));
            Logger.d("place_name:" + place_name);

            GeoObject geoObject = new GeoObject(count);
            geoObject.setGeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
            double distance = geoObject.calculateDistanceMeters(myLastLocation.getLongitude(), myLastLocation.getLatitude());

            Bitmap snapshot = null;
            View view = getLayoutInflater().inflate(R.layout.place_container, null);
            TextView name = view.findViewById(R.id.poi_container_name);
            TextView dist = view.findViewById(R.id.poi_container_dist);
            ImageView icon = view.findViewById(R.id.poi_container_icon);
            name.setText(place_name);
            dist.setText(String.valueOf(new BigDecimal(Double.toString(distance)).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue()) + " m");
            icon.setImageResource(typeID);
            // 畫圖
            view.setDrawingCacheEnabled(true);
            view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            view.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            snapshot = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight()
                    , Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(snapshot);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.draw(canvas);
            // 取得內存路徑
            String url = saveToInternalStorage(snapshot, "picture" + count);

            geoObject.setImageUri(url);
            geoObject.setName(place_name + "," + lat + "," + lng + "," + dist.getText().toString() + "," + rating);
            geoObjectList.add(geoObject);
            count++;
        }
        for (GeoObject geoObject : geoObjectList) {
            world.addBeyondarObject(geoObject);
        }
        beyondarFragment.setOnClickBeyondarObjectListener(new OnClickBeyondarObjectListener() {
            @Override
            public void onClickBeyondarObject(ArrayList<BeyondarObject> arrayList) {
                BeyondarObject beyondarObject = arrayList.get(0);
                String[] place_detail = beyondarObject.getName().split(",");
                String place_name = place_detail[0];
                final String lat = place_detail[1];
                final String lng = place_detail[2];
                String distance = place_detail[3];
                String rating = place_detail[4];

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
                Button btNo = dialog.findViewById(R.id.btNo);
                btYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        googleApiClient.disconnect();
                        String url = getDirectionApiUrl(lat, lng);
                        DirectionTask directionTask = new DirectionTask();
                        directionTask.execute(url);
                        Toast.makeText(getActivity(), "設定中", Toast.LENGTH_SHORT).show();
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
            }
        });
        beyondarFragment.setWorld(world);
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String name){
        ContextWrapper cw = new ContextWrapper(getActivity());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory,name);

        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mypath.toString();
    }

    // 取得要傳去Google Place Web Service的網址
    private String getNearbyPlacesUrl(double latitude, double longitude) {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + latitude + "," + longitude);
        stringBuilder.append("&radius=200");
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
            nearbyPlaceInfo = googleMapNearbyDataParser.parseNearbyPlaces(s, isDefine, drawableType);
            buildArWorld();
        }
    }

    // 取得要傳去Google Direction api的網址
    private String getDirectionApiUrl(String latitude, String longitude) {
        StringBuilder stringBuilder = new StringBuilder(Url.googleDirectionAPi);
        stringBuilder.append("origin=" + myLastLocation.getLatitude() + "," + myLastLocation.getLongitude());
        stringBuilder.append("&destination=" + latitude + "," + longitude);
        Logger.d("getDirectionApiUrl:" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private class DirectionTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
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
                JSONObject jsonObject = new JSONObject(data);
                br.close();
                inputStream.close();
                urlConnection.disconnect();
                Logger.d("success get direction api data");
                return jsonObject;
            } catch (Exception ex) {
                Logger.e(ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject.getString("status").equals("OVER_QUERY_LIMIT")) {
                    String url = getDirectionApiUrl(specificPlace.get("lat"), specificPlace.get("lng"));
                    DirectionTask directionTask = new DirectionTask();
                    directionTask.execute(url);
                }
                else {
                    Gson gson = new Gson();
                    Direction direction = gson.fromJson(jsonObject.toString(), Direction.class);
                    List<Direction.RoutesBean.LegsBean.StepsBean> steps =  direction.getRoutes().get(0).getLegs().get(0).getSteps();

                    beyondarFragment = (BeyondarFragment) getChildFragmentManager().findFragmentById(R.id.beyondarFragment);
                    world = new World(getActivity());
                    world.setDefaultImage(R.drawable.ar_sphere);
                    world.setGeoPosition(myLastLocation.getLatitude(), myLastLocation.getLongitude());
                    final List<GeoObject> geoObjectList = new ArrayList<>();
                    List<List<LatLng>> polylineLatLng = new ArrayList<>();
                    for (int i = 0; i < steps.size(); i++) {
                        polylineLatLng.add(PolyUtil.decode(steps.get(i).getPolyline().getPoints()));
                        String instructions = steps.get(i).getHtml_instructions();

                        if (i == 0) {
                            GeoObject signObject = new GeoObject(10000 + i);
                            signObject.setImageResource(R.drawable.start);
                            signObject.setGeoPosition(steps.get(i).getStart_location().getLat(), steps.get(i).getStart_location().getLng());
                            signObject.setName("start");
                            geoObjectList.add(signObject);
                        }

                        if (i == steps.size() - 1) {
                            GeoObject signObject = new GeoObject(10000 + i);
                            signObject.setImageResource(R.drawable.stop);
                            signObject.setName("stop");
                            LatLng latlng = SphericalUtil.computeOffset(
                                    new LatLng(steps.get(i).getEnd_location().getLat(), steps.get(i).getEnd_location().getLng()),
                                    4f, SphericalUtil.computeHeading(
                                            new LatLng(steps.get(i).getStart_location().getLat(), steps.get(i).getStart_location().getLng()),
                                            new LatLng(steps.get(i).getEnd_location().getLat(), steps.get(i).getEnd_location().getLng())));
                            signObject.setGeoPosition(latlng.latitude, latlng.longitude);
                            geoObjectList.add(signObject);
                        }

                        if (instructions.contains("右") || instructions.contains("right")) {
                            GeoObject signObject = new GeoObject(10000 + i);
                            signObject.setImageResource(R.drawable.turn_right);
                            signObject.setGeoPosition(steps.get(i).getStart_location().getLat(), steps.get(i).getStart_location().getLng());
                            geoObjectList.add(signObject);
                        }
                        else if (instructions.contains("左") || instructions.contains("left")) {
                            GeoObject signObject = new GeoObject(10000 + i);
                            signObject.setImageResource(R.drawable.turn_left);
                            signObject.setGeoPosition(steps.get(i).getStart_location().getLat(), steps.get(i).getStart_location().getLng());
                            geoObjectList.add(signObject);
                        }
                    }

                    int temp_polycount=0;
                    int temp_inter_polycount=0;

                    for (int j = 0;j < polylineLatLng.size(); j++) {
                        for (int k = 0; k < polylineLatLng.get(j).size(); k++) {
                            GeoObject polyGeoObj = new GeoObject(1000 + temp_polycount++);

                            polyGeoObj.setGeoPosition(polylineLatLng.get(j).get(k).latitude,
                                    polylineLatLng.get(j).get(k).longitude);
                            polyGeoObj.setImageResource(R.drawable.ar_sphere_150x);
                            polyGeoObj.setName("arObj"+ j + k);

                            try {
                                //Initialize distance of consecutive polyobjects
                                double dist = LocationCalc.haversine(polylineLatLng.get(j).get(k).latitude,
                                        polylineLatLng.get(j).get(k).longitude, polylineLatLng.get(j).get(k + 1).latitude,
                                        polylineLatLng.get(j).get(k + 1).longitude) * 1000;

                                //Check if distance between polyobjects is greater than twice the amount of space
                                // intended , here it is (3*2)=6 .
                                if (dist > 6) {

                                    //Initialize count of ar objects to be added
                                    int arObj_count = ((int) dist / 3) - 1;

                                    double heading = SphericalUtil.computeHeading(new LatLng(polylineLatLng.get(j).get(k).latitude,
                                                    polylineLatLng.get(j).get(k).longitude),
                                            new LatLng(polylineLatLng.get(j).get(k + 1).latitude,
                                                    polylineLatLng.get(j).get(k + 1).longitude));

                                    LatLng tempLatLng = SphericalUtil.computeOffset(new LatLng(polylineLatLng.get(j).get(k).latitude,
                                                    polylineLatLng.get(j).get(k).longitude)
                                            ,3f
                                            ,heading);

                                    //The distance to be incremented
                                    double increment_dist = 3f;

                                    for (int i = 0; i < arObj_count; i++) {
                                        GeoObject inter_polyGeoObj = new GeoObject(5000 + temp_inter_polycount++);

                                        //Store the Lat,Lng details into new LatLng Objects using the functions
                                        //in LocationCalc class.
                                        if (i > 0 && k < polylineLatLng.get(j).size()) {
                                            increment_dist += 3f;

                                            tempLatLng = SphericalUtil.computeOffset(new LatLng(polylineLatLng.get(j).get(k).latitude,
                                                            polylineLatLng.get(j).get(k).longitude),
                                                    increment_dist,
                                                    SphericalUtil.computeHeading(new LatLng(polylineLatLng.get(j).get(k).latitude
                                                            , polylineLatLng.get(j).get(k).longitude), new LatLng(
                                                            polylineLatLng.get(j).get(k + 1).latitude
                                                            , polylineLatLng.get(j).get(k + 1).longitude)));
                                        }

                                        //Set the Geoposition along with image and name
                                        inter_polyGeoObj.setGeoPosition(tempLatLng.latitude, tempLatLng.longitude);
                                        inter_polyGeoObj.setImageResource(R.drawable.ar_sphere_default_125x);
                                        inter_polyGeoObj.setName("inter_arObj" + j + k + i);

                                        //Add Intermediate ArObjects to Augmented Reality World
                                        geoObjectList.add(inter_polyGeoObj);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Logger.e(e.toString());
                            }
                        }
                    }

                    for (GeoObject geoObject : geoObjectList) {
                        world.addBeyondarObject(geoObject) ;
                    }
                    beyondarFragment.setWorld(world);
                    beyondarFragment.setOnClickBeyondarObjectListener(new OnClickBeyondarObjectListener() {
                        @Override
                        public void onClickBeyondarObject(ArrayList<BeyondarObject> arrayList) {
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.cancel_location_dialog);
                            Button btYes = dialog.findViewById(R.id.btYes);
                            Button btNo = dialog.findViewById(R.id.btNo);
                            btYes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    googleApiClient.connect();
                                    specificPlace = null;
                                    Toast.makeText(getActivity(), "取消中", Toast.LENGTH_SHORT).show();
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
                        }
                    });
                }
            } catch (Exception e) {
                Logger.d(e.toString());
            }
        }
    }
}

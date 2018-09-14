package com.csim.scu.aibox.network;

import android.util.Log;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GoogleMapNearbyDataParser {

    private boolean isRightType = false;
    private boolean isDefine = false;
    private int drawableType = -1;

    // 將googlePlace Web Service 回傳過來Json資料拆解
    // 取出name、vicinity、latitude、longtitude、place_id、rating
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        isRightType = false;
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String rating = "0";
        int typeID = 0;
        String latitude = "";
        String longitude = "";
        String place_id = "";
        try {
            if(!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            if (!googlePlaceJson.isNull("rating")) {
                rating = googlePlaceJson.getString("rating");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            place_id = googlePlaceJson.getString("place_id");
            JSONArray jsonArray = googlePlaceJson.getJSONArray("types");
            if (jsonArray.length() != 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    switch (jsonArray.get(i).toString()) {
                        case "cafe":
                        case "restaurant":
                            typeID = R.drawable.navigation_food;
                            isRightType = true;
                            break;
                        case "parking":
                            typeID = R.drawable.navigation_parking;
                            isRightType = true;
                            break;
                        case "hospital":
                        case "doctor":
                        case "dentist":
                            typeID = R.drawable.navigation_hospital;
                            isRightType = true;
                            break;
                        case "lodging":
                            typeID = R.drawable.navigation_hotel;
                            isRightType = true;
                            break;
                        case "bus_station":
                            typeID = R.drawable.navigation_bus;
                            isRightType = true;
                            break;
                        case "convenience_store":
                            typeID = R.drawable.navigation_store;
                            isRightType = true;
                            break;
                        case "post_office":
                            typeID = R.drawable.navigation_postoffice;
                            isRightType = true;
                            break;
                        case "park":
                            typeID = R.drawable.navigation_park;
                            isRightType = true;
                            break;
                        default:
                            typeID = R.drawable.navigation_location;
                            isRightType = true;
                            break;
                    }
                    if (isRightType) {
                        break;
                    }
                }
            }
            if (isDefine) {
                if (drawableType == typeID) {
                    googlePlaceMap.put("place_name", placeName);
                    googlePlaceMap.put("vicinity", vicinity);
                    googlePlaceMap.put("lat", latitude);
                    googlePlaceMap.put("lng", longitude);
                    googlePlaceMap.put("place_id", place_id);
                    googlePlaceMap.put("typeID", String.valueOf(typeID));
                    googlePlaceMap.put("rating", rating);
                }
                else {
                    return null;
                }
            }
            else {
                googlePlaceMap.put("place_name", placeName);
                googlePlaceMap.put("vicinity", vicinity);
                googlePlaceMap.put("lat", latitude);
                googlePlaceMap.put("lng", longitude);
                googlePlaceMap.put("place_id", place_id);
                googlePlaceMap.put("typeID", String.valueOf(typeID));
                googlePlaceMap.put("rating", rating);
            }
        } catch (Exception ex) {
            Logger.e(ex.toString());
        }
        Logger.d(googlePlaceMap.toString());
        return googlePlaceMap;
    }

    // 將每個景點的HashMap收集為一連串List
    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for(int i = 0; i < count; i++) {
            try {
                placeMap = getPlace((JSONObject)jsonArray.get(i));
                if (placeMap != null) {
                    placesList.add(placeMap);
                }
            } catch (Exception ex) {
                Logger.e(ex.toString());
            }
        }
        return placesList;
    }
    // 取得Google Place Web Service的NearbyPlaces的JsonData
    // 擷取"results"的JsonArray
    public List<HashMap<String, String>> parseNearbyPlaces(String jsonData, boolean isDefine, int drawableType) {
        this.isDefine = isDefine;
        this.drawableType = drawableType;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            if(jsonObject.get("status").equals("OK")) {
                jsonArray = jsonObject.getJSONArray("results");
                Logger.d("已順利偵測到地點並至少傳回一個結果:" + jsonArray.toString());
            }
            else {
                Logger.d("沒有順利偵測到:" + jsonArray.toString());
            }
        } catch (Exception ex) {
            Logger.e(ex.toString());
        }

        return getPlaces(jsonArray);
    }
}

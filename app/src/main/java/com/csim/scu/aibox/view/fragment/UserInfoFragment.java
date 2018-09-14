package com.csim.scu.aibox.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.network.Url;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserInfoFragment extends Fragment {

    private FragmentManager fragmentManager;
    private TextView tvUserName;
    private TextView tvUserHeight;
    private TextView tvUserWeight;
    private TextView tvUserAge;
    private TextView tvUserBmi;
    private LinearLayout llUserDiease;
    private LinearLayout llUserHabit;
    private LinearLayout llUserShare;
    private LinearLayout llUserBloodPressure;
    private ImageButton ibBack;
    private Map<String, String> map;
    private Map<String, String> userInfo = new HashMap<>();
    private boolean isShare = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userinfo, container, false);
        fragmentManager = getFragmentManager();
        map = new HashMap<>();
        map.put("nickname", this.getArguments().getString("nickname"));
        map.put("height", this.getArguments().getString("height"));
        map.put("weight", this.getArguments().getString("weight"));
        map.put("age", this.getArguments().getString("age"));
        map.put("bmi_value", this.getArguments().getString("bmi_value"));
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserHeight = view.findViewById(R.id.tvUserHeight);
        tvUserWeight = view.findViewById(R.id.tvUserWeight);
        tvUserAge = view.findViewById(R.id.tvUserAge);
        tvUserBmi = view.findViewById(R.id.tvUserBmi);
        llUserDiease = view.findViewById(R.id.llUserDisease);
        llUserHabit = view.findViewById(R.id.llUserHabit);
        llUserShare = view.findViewById(R.id.llUserShare);
        llUserBloodPressure = view.findViewById(R.id.llUserBloodPressure);
        ibBack = view.findViewById(R.id.ibBack);
        setUserInfo();
        setCardViewListenerAndItem();
        setBackListener();
    }

    private void setUserInfo() {
        tvUserName.setText(map.get("nickname"));
        tvUserHeight.setText(map.get("height") + " 公分");
        tvUserWeight.setText(map.get("weight") + " 公斤");
        tvUserAge.setText(map.get("age") + " 歲");
        tvUserBmi.setText(map.get("bmi_value"));
    }

    private void setCardViewListenerAndItem() {
        llUserDiease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserHealthTask userHealthTask = new UserHealthTask();
                userHealthTask.execute();
            }
        });

        llUserHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    UserHabitTask userHabitTask = new UserHabitTask();
                    userHabitTask.execute();
            }
        });

        llUserShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserProfileTask userProfileTask = new UserProfileTask();
                userProfileTask.execute();
            }
        });
        llUserBloodPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserDailyConcernTask userDailyConcernTask = new UserDailyConcernTask();
                userDailyConcernTask.execute();
            }
        });
    }

    private void setBackListener() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStackImmediate();
            }
        });
    }

    private class UserProfileTask extends AsyncTask<Void, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.userProfile);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                return (JSONObject) jsonObject.get("result");
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                String nickname = jsonObject.get("nickname").toString();
                String height = jsonObject.get("height").toString();
                String weight = jsonObject.get("weight").toString();
                String age = jsonObject.get("age").toString();
                String bmi_value = jsonObject.get("bmi_value").toString();
                userInfo.put("nickname", nickname);
                userInfo.put("height", height);
                userInfo.put("weight", weight);
                userInfo.put("age", age);
                userInfo.put("bmi_value", bmi_value);
                isShare = true;
                UserHealthTask userHealthTask = new UserHealthTask();
                userHealthTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private class UserHealthTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.userHealth);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                return (JSONObject) jsonObject.get("result");
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                String heart_problem = jsonObject.get("heart_problem").toString();
                String stroke = jsonObject.get("stroke").toString();
                String high_blood = jsonObject.get("high_blood").toString();
                String high_cholesterol = jsonObject.get("high_cholesterol").toString();
                String diabetes = jsonObject.get("diabetes").toString();
                String smoking = jsonObject.get("smoking").toString();
                String exercise = jsonObject.get("exercise").toString();

                if (!isShare) {
                    Bundle bundle = new Bundle();
                    bundle.putString("heart_problem", heart_problem);
                    bundle.putString("stroke", stroke);
                    bundle.putString("high_blood", high_blood);
                    bundle.putString("high_cholesterol", high_cholesterol);
                    bundle.putString("diabetes", diabetes);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    UserDiseaseFragment userDiseaseFragment = new UserDiseaseFragment();
                    userDiseaseFragment.setArguments(bundle);
                    UserInfoFragment userInfoFragment = UserInfoFragment.this;
                    fragmentTransaction.hide(userInfoFragment);
                    fragmentTransaction.add(R.id.container, userDiseaseFragment, userDiseaseFragment.getClass().getName());
                    fragmentTransaction.addToBackStack(userDiseaseFragment.getClass().getName());
                    fragmentManager.executePendingTransactions();
                    fragmentTransaction.commit();
                }
                else {
                    userInfo.put("heart_problem", heart_problem);
                    userInfo.put("stroke", stroke);
                    userInfo.put("high_blood", high_blood);
                    userInfo.put("high_cholesterol", high_cholesterol);
                    userInfo.put("diabetes", diabetes);
                    userInfo.put("smoking", smoking);
                    userInfo.put("exercise", exercise);
                    isShare = false;
                    shareInfo();
                }


            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private class UserHabitTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.userHealth);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                return (JSONObject) jsonObject.get("result");
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                String smoking = jsonObject.get("smoking").toString();
                String exercise = jsonObject.get("exercise").toString();

                Bundle bundle = new Bundle();
                bundle.putString("smoking", smoking);
                bundle.putString("exercise", exercise);


                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                UserHabitFragment userHabitFragment = new UserHabitFragment();
                userHabitFragment.setArguments(bundle);
                UserInfoFragment userInfoFragment = UserInfoFragment.this;
                fragmentTransaction.hide(userInfoFragment);
                fragmentTransaction.add(R.id.container, userHabitFragment, userHabitFragment.getClass().getName());
                fragmentTransaction.addToBackStack(userHabitFragment.getClass().getName());
                fragmentManager.executePendingTransactions();
                fragmentTransaction.commit();

            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private String loadCookie() {
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return preferences.getString("cookie", "");
    }

    private void shareInfo() {
        Logger.d(userInfo.toString());
        String heart_problem = userInfo.get("heart_problem").equals("True") ? "是" : "否";
        String stroke = userInfo.get("stroke").equals("True") ? "是" : "否";
        String high_blood = userInfo.get("high_blood").equals("True") ? "是" : "否";
        String high_cholesterol = userInfo.get("high_cholesterol").equals("True") ? "是" : "否";
        String diabetes = userInfo.get("diabetes").equals("True") ? "是" : "否";
        String smoking = userInfo.get("smoking").equals("True") ? "是" : "否";
        String exercise = userInfo.get("exercise").equals("True") ? "是" : "否";
        String info = "基本資訊:" + "\n" +
                      "名稱:" + userInfo.get("nickname") + "\n" +
                      "身高:" + userInfo.get("height") + " 公分" + "\n" +
                      "體重:" + userInfo.get("weight") + " 公斤" + "\n" +
                      "年齡:" + userInfo.get("age") + "歲" + "\n" +
                      "bmi:" + userInfo.get("bmi_value") + "\n" +
                      "病史紀錄:" + "\n" +
                      "心律不整:" + heart_problem + "\n" +
                      "中風:" + stroke + "\n" +
                      "高血壓:" + high_blood + "\n" +
                      "高膽固醇:" + high_cholesterol + "\n" +
                      "糖尿病:" + diabetes + "\n" +
                      "生活習慣:" + "\n" +
                      "吸菸:" + smoking + "\n" +
                      "運動:" + exercise;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "分享");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, info);
        startActivity(Intent.createChooser(sharingIntent, "選擇分享APP"));
    }

    private class UserDailyConcernTask extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.dailyConcern);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                return (JSONArray) jsonObject.getJSONArray("result");
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                List<Map<String, String>> concernList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Map<String, String> map = new HashMap<>();
                    String diastolic = jsonObject.getString("diastolic");
                    String systolic = jsonObject.getString("systolic");
                    String dateWeekNum = String.valueOf(jsonObject.getInt("dateWeekNum"));
                    String date = jsonObject.getString("date");
                    map.put("diastolic", diastolic);
                    map.put("systolic", systolic);
                    map.put("dateWeekNum", dateWeekNum);
                    map.put("date", date);
                    concernList.add(map);
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("concernList", (Serializable) concernList);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                BloodFragment bloodFragment = new BloodFragment();
                bloodFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.container, bloodFragment, bloodFragment.getClass().getName());
                fragmentTransaction.addToBackStack(bloodFragment.getClass().getName());
                fragmentTransaction.commit();
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }
}

package com.csim.scu.aibox.view.fragment;


import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.Emergency;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.util.EmergencyAdapter;
import com.csim.scu.aibox.view.activity.MainActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmergencyFragment extends Fragment {

    private ListView listView;
    private EmergencyAdapter emergencyAdapter;
    private List<Emergency> emergencyList = new ArrayList<>();
    private List<Map<String, String>> contactList;
    private ImageButton ibEdit;
    private ImageButton ibBack;
    private Dialog dialog;
    private EditText etName;
    private EditText etPhone;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);
        contactList = (List<Map<String, String>>) this.getArguments().get("contactList");
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        listView = view.findViewById(R.id.listView);
        ibEdit = view.findViewById(R.id.ibEdit);
        ibBack = view.findViewById(R.id.ibBack);
        setListView();
        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.emergency_dialog);
                etName = dialog.findViewById(R.id.etName);
                etPhone = dialog.findViewById(R.id.etPhone);
                Button btYes = dialog.findViewById(R.id.btYes);
                Button btNo = dialog.findViewById(R.id.btNo);
                btYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etName.getText().toString().equals("") || etPhone.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), "請輸入正確的資訊", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ECPsetTask ecPsetTask = new ECPsetTask();
                            String[] contact = new String[]{etName.getText().toString(), etPhone.getText().toString()};
                            ecPsetTask.execute(contact);
                        }
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
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void setListView() {
        Logger.d(contactList.toString());
        for (int i = 0; i < contactList.size(); i++) {
            Map<String, String> map = contactList.get(i);
            Emergency emergency = new Emergency(map.get("name"), map.get("phone"));
            emergencyList.add(emergency);
        }
        emergencyAdapter = new EmergencyAdapter(getActivity(), emergencyList);
        listView.setAdapter(emergencyAdapter);
        emergencyAdapter.notifyDataSetChanged();
    }

    private class ECPsetTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String ec_person = strings[0];
            String ec_phone = strings[1];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.setECP);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("ec_person", ec_person);
                jsonParam.put("ec_phone", ec_phone);
                writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                httpURLConnection.disconnect();
                writer.close();
                reader.close();

                return jsonObject;

            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject.getString("status").equals("200")) {
                    dialog.dismiss();
                    emergencyList.add(new Emergency(etName.getText().toString(), etPhone.getText().toString()));
                    emergencyAdapter.notifyDataSetChanged();
                }
                else {
                    Logger.d(jsonObject.toString());
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "設置聯絡人錯誤", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private String loadCookie() {
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return preferences.getString("cookie", "");
    }
}

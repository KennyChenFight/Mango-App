package com.csim.scu.aibox.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.ConversationMessage;
import com.csim.scu.aibox.model.Emergency;
import com.csim.scu.aibox.network.Url;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by kenny on 2018/9/3.
 */

public class EmergencyAdapter extends BaseAdapter {

    private Context context;
    private List<Emergency> emergencyList;
    private int index = -1;
    private Dialog dialog;

    public EmergencyAdapter(Context context, List<Emergency> emergencyList) {
        this.context = context;
        this.emergencyList = emergencyList;
    }

    @Override
    public View getView(final int i, View itemView, ViewGroup viewGroup) {
        Logger.d("test:" + i);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        int layoutResource = R.layout.emergency;
        itemView = inflater.inflate(layoutResource, viewGroup, false);

        final Emergency emergency = emergencyList.get(i);
        ViewHolder viewHolder = new ViewHolder(itemView);
        viewHolder.tvName.setText(emergency.getName());
        viewHolder.tvPhone.setText(emergency.getPhone());
        viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.emergency_delete_dialog);
                Button btYes = dialog.findViewById(R.id.btYes);
                Button btNo = dialog.findViewById(R.id.btNo);
                btYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        index = i;
                        String[] temp = new String[]{emergency.getName()};
                        ECPdelTask ecPdelTask = new ECPdelTask();
                        ecPdelTask.execute(temp);
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
        return itemView;
    }

    @Override
    public int getCount() {
        return emergencyList.size();
    }

    @Override
    public Emergency getItem(int i) {
        return emergencyList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder {
        private TextView tvName;
        private TextView tvPhone;
        private TextView tvDelete;

        public ViewHolder(View v) {
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvDelete = v.findViewById(R.id.tvDelete);
        }
    }

    private class ECPdelTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String ec_person = strings[0];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.delECP);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("ec_person", ec_person);
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
                    emergencyList.remove(index);
                    notifyDataSetChanged();
                }
                else {
                    dialog.dismiss();
                    Toast.makeText(context, "刪除聯絡人錯誤", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private String loadCookie() {
        SharedPreferences preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return preferences.getString("cookie", "");
    }

}

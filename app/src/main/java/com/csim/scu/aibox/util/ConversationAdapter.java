package com.csim.scu.aibox.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.ConversationMessage;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.view.fragment.ReminderFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kenny on 2018/7/20.
 */

public class ConversationAdapter extends BaseAdapter {

    private Context context;
    private List<ConversationMessage> conversationList;
    private String hospitalName;

    public ConversationAdapter(Context context, List<ConversationMessage> conversationLists) {
        this.context = context;
        this.conversationList = conversationLists;
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public ConversationMessage getItem(int i) {
        return conversationList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        int layoutResource;
        final ConversationMessage chatMessage = getItem(position);
        // 決定講話人是誰
        if (chatMessage.getType() == ConversationMessage.Type.PERSON) {
            layoutResource = R.layout.conversation_right;
            itemView = inflater.inflate(layoutResource, parent, false);
            ViewHolder holder = new ViewHolder(itemView);
            // 設定講話內容、回答時間
            holder.msg.setText(chatMessage.getContent());
            holder.time.setText(chatMessage.getDateString());
        }
        else if (chatMessage.getType() == ConversationMessage.Type.ROBOT) {
            layoutResource = R.layout.conversation_left;
            itemView = inflater.inflate(layoutResource, parent, false);
            ViewHolder holder = new ViewHolder(itemView);
            // 設定講話內容、回答時間
            holder.msg.setText(chatMessage.getContent());
            holder.time.setText(chatMessage.getDateString());
        }
        else {
            layoutResource = R.layout.conversation_left_hospital;
            itemView = inflater.inflate(layoutResource, parent, false);
            HospitalViewHolder hospitalViewHolder = new HospitalViewHolder(itemView);
            // 設定講話內容、回答時間
            hospitalName = chatMessage.getContent();
            hospitalViewHolder.msg.setText(chatMessage.getContent());
            hospitalViewHolder.time.setText(chatMessage.getDateString());
            hospitalViewHolder.link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HospitalInfoTask hospitalInfoTask = new HospitalInfoTask();
                    hospitalInfoTask.execute();
                }
            });
        }
        return itemView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }

    private class ViewHolder {
        private TextView msg;
        private TextView time;

        public ViewHolder(View v) {
            msg = v.findViewById(R.id.tv_send);
            time = v.findViewById(R.id.time_tv);
        }
    }

    private class HospitalViewHolder {
        private TextView msg;
        private TextView time;
        private Button link;

        public HospitalViewHolder(View v) {
            msg = v.findViewById(R.id.tv_send);
            time = v.findViewById(R.id.time_tv);
            link = v.findViewById(R.id.link);
        }
    }

    private class HospitalInfoTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.hospital + hospitalName);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
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

                return jsonObject;

            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                Logger.d(jsonObject.toString());
                JSONObject result = jsonObject.getJSONObject("result");
                String type = result.getString("權屬別");
                String address = result.getString("地址");
                String phone = result.getString("電話");
                String rating = result.getString("醫院評價");
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.hospital_info_dialog);
                TextView tvName = dialog.findViewById(R.id.tvName);
                ImageView ivImage = dialog.findViewById(R.id.ivImage);
                TextView tvType = dialog.findViewById(R.id.tvType);
                TextView tvAddress = dialog.findViewById(R.id.tvAddress);
                TextView tvPhone = dialog.findViewById(R.id.tvPhone);
                TextView tvRating = dialog.findViewById(R.id.tvRating);
                tvName.setText(hospitalName);
                Logger.d(hospitalName);
                tvType.setText("醫院類別：" + type);
                tvAddress.setText("地址：" + address);
                tvPhone.setText("電話：" + phone);
                if (rating.equals("0")) {
                    tvRating.setText("暫無評價");
                }
                else {
                    tvRating.setText("評價：" + rating);
                }
                Button btYes = dialog.findViewById(R.id.btYes);
                btYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                Logger.d(jsonObject.toString());
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

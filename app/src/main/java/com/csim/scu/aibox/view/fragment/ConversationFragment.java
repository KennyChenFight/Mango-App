package com.csim.scu.aibox.view.fragment;

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
import android.widget.ImageButton;
import android.widget.ListView;

import com.csim.scu.aibox.callback.ConversationCallback;
import com.csim.scu.aibox.model.Conversation;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.util.ConversationAdapter;
import com.csim.scu.aibox.model.ConversationMessage;
import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ConversationFragment extends Fragment implements ConversationCallback {

    private ArrayList<Conversation> conversationList;
    private ArrayList<ConversationMessage> conversationMessages = new ArrayList<>();
    private ConversationAdapter conversationAdapter;
    private ListView listView;
    private ImageButton ibBack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        conversationList = (ArrayList<Conversation>) this.getArguments().getSerializable("conversation");
        Logger.d(conversationList.toString());
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        listView = view.findViewById(R.id.listView);
        ibBack = view.findViewById(R.id.ibBack);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });
        setListViewItem();
    }

    private void setListViewItem() {
        conversationAdapter = new ConversationAdapter(getActivity(), conversationMessages);
        listView.setAdapter(conversationAdapter);
        ConversationMessage conversationMessage = null;
        for (int i = 0; i < conversationList.size(); i++) {
            String question = conversationList.get(i).getQuestion();
            String response = conversationList.get(i).getResponse();
            String date = conversationList.get(i).getDate();
            if (!response.contains("醫院名稱")) {
                conversationMessage = new ConversationMessage(question, ConversationMessage.Type.PERSON, date);
                conversationMessages.add(conversationMessage);
                conversationAdapter.notifyDataSetChanged();
                conversationMessage = new ConversationMessage(response, ConversationMessage.Type.ROBOT, date);
                conversationMessages.add(conversationMessage);
                conversationAdapter.notifyDataSetChanged();
            }
            else {
                conversationMessage = new ConversationMessage(response.replace("醫院名稱: ", ""), ConversationMessage.Type.HOSPITAL, date);
                conversationMessages.add(conversationMessage);
                conversationAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void updateConversation() {
        UserConversationTask userConversationTask = new UserConversationTask();
        userConversationTask.execute();
    }

    private class UserConversationTask extends AsyncTask<Void, Void, JSONArray> {


        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.userConversation);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                Logger.d(jsonObject.toString());
                return (JSONArray) jsonObject.get("result");
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                ConversationMessage conversationMessage = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    String question = jsonObject.get("question").toString();
                    String response = jsonObject.get("response").toString();
                    int dayIndex = jsonObject.get("date").toString().indexOf(" ");
                    int secondIndex = jsonObject.get("date").toString().lastIndexOf(":");
                    String date = jsonObject.get("date").toString().substring(dayIndex, secondIndex);
                    conversationMessage = new ConversationMessage(question, ConversationMessage.Type.PERSON, date);
                    conversationMessages.add(conversationMessage);
                    conversationMessage = new ConversationMessage(response, ConversationMessage.Type.ROBOT, date);
                    conversationMessages.add(conversationMessage);
                }
                conversationAdapter.notifyDataSetChanged();


            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private String loadCookie() {
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return preferences.getString("cookie", "");
    }
}

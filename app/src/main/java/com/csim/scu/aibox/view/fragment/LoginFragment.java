package com.csim.scu.aibox.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.csim.scu.aibox.R;
import com.csim.scu.aibox.callback.LoginFragmentCallback;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.util.SpeechRecognizerManager;
import android.widget.Switch;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginFragment extends Fragment {

    private FragmentManager fragmentManager;
    private LoginFragment loginFragment = LoginFragment.this;
    private LoginFragmentCallback loginFragmentCallback;
    private SpeechRecognizerManager speechRecognizerManager;
    private boolean isAlreadyCancelBluetooth = false;
    private boolean isListening = false;
    private String cookie = "";
    private Button btLogin;
    private Button btRegister;
    private Switch swBluetooth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        fragmentManager = getFragmentManager();
        hideBar();
        findViews(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        loginFragmentCallback = (LoginFragmentCallback) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    // 隱藏標題
    private void hideBar() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Logger.d();
    }

    // 找到元件
    private void findViews(View view) {
        btLogin = view.findViewById(R.id.btLogin);
        btRegister = view.findViewById(R.id.btRegister);
        swBluetooth = view.findViewById(R.id.swBluetooth);
        setClickListener();
    }

    private void setClickListener() {

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swBluetooth.setChecked(false);
                initSpeechListener();
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.login_toast, null);
                Toast toast = new Toast(getActivity());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setView(layout);
                toast.show();
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.signup_toast, null);
                Toast toast = new Toast(getActivity());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setView(layout);
                toast.show();
                swBluetooth.setChecked(true);
            }
        });

        swBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                if (check) {
                    if (isListening) {
                        speechRecognizerManager.cancelSpeechRecognizer();
                        isListening = false;
                    }
                    loginFragmentCallback.connectBluetoothDevice();
                }
                else {
                    if (!isAlreadyCancelBluetooth) {
                        isAlreadyCancelBluetooth = true;
                        loginFragmentCallback.stopBluetoothDevice();
                    }
                }
            }
        });
    }

    public void registerUser(boolean isSuccess) {
        if (isSuccess) {
            //isAlreadyCancelBluetooth = false;
            //swBluetooth.setChecked(false);
        }
    }


    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String status = "404";
            String nickName = strings[0];
            try {

                URL url = new URL(Url.baseUrl + Url.loginUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_nickname", nickName);
                httpURLConnection.connect();

                OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                status = (String) jsonObject.get("status");

                String cookieVal = httpURLConnection.getHeaderField("Set-Cookie");
                if(cookieVal != null) {
                    cookie = cookieVal.substring(0, cookieVal.indexOf(";"));
                    saveCookie();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return status;
        }

        @Override
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                Toast.makeText(getActivity(), "登入成功，為您轉至首頁", Toast.LENGTH_SHORT).show();
                speechRecognizerManager.cancelSpeechRecognizer();
                UserProfileTask userProfileTask = new UserProfileTask();
                userProfileTask.execute();
            }
            else {
                Toast.makeText(getActivity(), "登入失敗", Toast.LENGTH_SHORT).show();
                speechRecognizerManager.startListenering();
            }
        }
    }

    private class UserProfileTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL(Url.baseUrl + Url.userProfile);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", cookie);
                if (httpURLConnection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String line = "";
                    while((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(content.toString());
                    return (JSONObject) jsonObject.get("result");
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            Map<String, String> map = new HashMap<>();
            try {
                map.put("nickname", jsonObject.get("nickname").toString());
                map.put("height", jsonObject.get("height").toString());
                map.put("weight", jsonObject.get("weight").toString());
                map.put("age", jsonObject.get("age").toString());
                loginFragmentCallback.sendUserProfileData(map);
                loginFragmentCallback.isLoginFragment(true);
                FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
                fragmentTransaction.remove(loginFragment);
                fragmentTransaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private void saveCookie() {
        SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        preferences.edit().putString("cookie",  cookie).apply();
    }


    // 參數初始化
    private void initSpeechListener() {
        speechRecognizerManager = new SpeechRecognizerManager(getActivity());
        setSpeechRecognizerManagerListener();
        speechRecognizerManager.startListenering();
        isListening = true;
    }

    // 設定SpeechRecognizerManager監聽器，並實作onResults、onError的Methods
    private void setSpeechRecognizerManagerListener() {
        speechRecognizerManager.setSpeechRecognizerListener(new SpeechRecognizerManager.onListener() {
            @Override
            public void onResults(Bundle bundle) {
                List<String> resList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String sentence = resList.get(0);
                Logger.d("語音辨識的結果:" + sentence);
                LoginTask loginTask = new LoginTask();
                loginTask.execute(sentence);
                speechRecognizerManager.cancelSpeechRecognizer();
            }
            @Override
            public void onError(int i) {
                Logger.d("語音辨識失敗碼:" + i);
                speechRecognizerManager.cancelSpeechRecognizer();
                speechRecognizerManager.startListenering();
            }
        });
    }
}

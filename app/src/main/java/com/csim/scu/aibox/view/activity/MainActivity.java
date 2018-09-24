package com.csim.scu.aibox.view.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.csim.scu.aibox.callback.ConversationCallback;
import com.csim.scu.aibox.callback.LoginFragmentCallback;
import com.csim.scu.aibox.callback.ReminderCallback;
import com.csim.scu.aibox.callback.ReminderFragmentCallback;
import com.csim.scu.aibox.callback.TypeCallback;
import com.csim.scu.aibox.model.Conversation;
import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.OpenActivity;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.receiver.PhoneStateReceiver;
import com.csim.scu.aibox.receiver.ReminderReceiver;
import com.csim.scu.aibox.util.BluetoothHelperManager;
import com.csim.scu.aibox.util.NotifactionUtil;
import com.csim.scu.aibox.util.SpeechRecognizerManager;
import com.csim.scu.aibox.util.TextToSpeechManager;
import com.csim.scu.aibox.view.fragment.ConversationFragment;
import com.csim.scu.aibox.view.fragment.EmergencyFragment;
import com.csim.scu.aibox.view.fragment.GoogleMapFragment;
import com.csim.scu.aibox.view.fragment.LoginFragment;
import com.csim.scu.aibox.view.fragment.NavigationFragment;
import com.csim.scu.aibox.view.fragment.RecommendFragment;
import com.csim.scu.aibox.view.fragment.ReminderFragment;
import com.csim.scu.aibox.view.fragment.TypeFragment;
import com.csim.scu.aibox.view.fragment.UserInfoFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// todo 測試註冊流程，註冊完後不要自動關掉連接音箱
// todo 測試音箱多人使用
// todo 藍芽開關的敘述、緊急聯絡人的敘述

public class MainActivity extends AppCompatActivity implements LoginFragmentCallback,
        LocationListener, ReminderReceiver.ReminderMessageReceiver,
        PhoneStateReceiver.PhoneStateMessageReceiver, ReminderFragmentCallback{

    private static final int TTS_CHECK_CODE = 0;
    private static final int TIME_INTERVAL = 2000;
    // receiver and its callback
    private ReminderReceiver reminderReceiver = new ReminderReceiver();
    private static ReminderReceiver.ReminderMessageReceiver reminderMessageReceiver;
    private PhoneStateReceiver phoneStateReceiver = new PhoneStateReceiver();
    private static PhoneStateReceiver.PhoneStateMessageReceiver phoneStateMessageReceiver;
    private long backPressed;
    // locate user location
    private LocationManager locationManager;
    private Location myLocation;
    // textToSpeech, Bluetooth, SpeechRecognizer Manager
    private TextToSpeechManager textToSpeechManager;
    private BluetoothHelperManager bluetoothHelperManager;
    private SpeechRecognizerManager speechRecognizerManager;
    // fragment Manager
    private FragmentManager fragmentManager;
    // 與Server端互相溝通的shareFlag(判斷對話流程), shareResponse(回覆句子)
    private String shareFlag = "";
    private String shareResponse = "";
    // isStart:True => 已成功喚醒音箱(呼喊Mango), isStart:False => 尚未成功喚醒音箱
    private boolean isStart = false;
    // UI
    private TextView tvUserName;
    private TextView tvUserHeight;
    private TextView tvUserWeight;
    private TextView tvUserAge;
    private CardView cvUserInfo;
    private CardView cvConversation;
    private CardView cvRecommend;
    private CardView cvReminder;
    private CardView cvNavigation;
    private CardView cvGame;
    private Switch sbConnectToBluetooth;
    private ImageButton ibPhoneBook;
    // 判斷現在是否處於登入頁面
    private boolean isLoginFragment = false;
    // 判斷是否已經設定過個人化鬧鐘
    private boolean setUserReminder = false;
    private int userReminderCount = -1;
    private boolean setNonUserReminder = false;
    private int nonUserReminderCount = -1;
    // 取得電話(問醫院電話，並要撥打電話的流程)
    private String phoneNumber;
    // 確定當前是否講完電話
    private boolean isPhone = false;
    // 取得地址(
    private String hospitalAddress = "";
    // 與其他頁面的Callback
    private ConversationCallback conversationCallback;
    private ReminderCallback reminderCallback;
    private TypeCallback typeCallback;
    // 確認是否是附近活動的提醒
    private boolean isOpenActivityNotify = false;
    // 取得附近活動的標題、開始日期 => 前往提醒頁面放置
    private String openActivityTitle = "";
    private String openActivityStartDate = "";
    // 取得附近活動的List
    private List<OpenActivity> openActivityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getFragmentManager();
        reminderMessageReceiver = this;
        reminderReceiver.concernRegisterCallback(reminderMessageReceiver);
        phoneStateMessageReceiver = this;
        phoneStateReceiver.resumeRegisterCallback(phoneStateMessageReceiver);
        PhoneStateReceiver phoneStateReceiver = new PhoneStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(phoneStateReceiver, filter);
        findViews();
        openGPSSettings();
        jumpToLoginFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        // callback 實例化
        if (fragment.getTag().equals(ConversationFragment.class.getName())) {
            conversationCallback = (ConversationCallback) fragment;
        }
        else if (fragment.getTag().equals(ReminderFragment.class.getName())) {
            reminderCallback = (ReminderCallback) fragment;
            reminderCallback.updateLocation(myLocation);
        }
        else if (fragment.getTag().equals(TypeFragment.class.getName())) {
            typeCallback = (TypeCallback) fragment;
        }
    }

    // UI 實例化
    private void findViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserHeight = findViewById(R.id.tvUserHeight);
        tvUserWeight = findViewById(R.id.tvUserWeight);
        tvUserAge = findViewById(R.id.tvUserAge);
        cvUserInfo = findViewById(R.id.cvUserInfo);
        cvGame = findViewById(R.id.cvGame);
        cvConversation = findViewById(R.id.cvConversation);
        cvRecommend = findViewById(R.id.cvRecommend);
        cvReminder = findViewById(R.id.cvReminder);
        cvNavigation = findViewById(R.id.cvNavigation);
        sbConnectToBluetooth = findViewById(R.id.sbConnectToBluetooth);
        ibPhoneBook = findViewById(R.id.ibPhoneBook);
        ibPhoneBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ECPtask ecPtask = new ECPtask();
                ecPtask.execute();
            }
        });
        initParams();
        setCardViewClickListener();
        setConnectToBluetoothListener();
    }

    // 跳轉至登入頁面
    private void jumpToLoginFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        fragmentTransaction.add(R.id.container, loginFragment, loginFragment.getClass().getName());
        Logger.d("jump to login fragment");
        fragmentManager.executePendingTransactions();
        fragmentTransaction.commit();
        isLoginFragment = true;
    }

    @Override
    public void sendUserProfileData(Map<String, String> data) {
        String nickname = data.get("nickname");
        String height = data.get("height");
        String weight = data.get("weight");
        String age = data.get("age");
        tvUserName.setText(nickname);
        tvUserHeight.setText(height + " cm");
        tvUserWeight.setText(weight + " kg");
        tvUserAge.setText(age);
    }

    @Override
    public void connectBluetoothDevice() {
        sbConnectToBluetooth.setChecked(true);
    }

    @Override
    public void stopBluetoothDevice() {
        sbConnectToBluetooth.setChecked(false);
    }

    @Override
    public void isLoginFragment(boolean state) {
        if (state) {
            setNeedReminder();
            isLoginFragment = false;
        }
    }

    public void setCardViewClickListener() {
        cvUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserProfileTask userProfileTask = new UserProfileTask();
                userProfileTask.execute();
            }
        });

        cvGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.yini.ProductName");//com.yini.ProductName是我們unity的名稱((固定
                startActivityForResult(intent, RESULT_OK);
            }
        });

        cvConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserConversationTask userConversationTask = new UserConversationTask();
                userConversationTask.execute();
            }
        });

        cvRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecommendTask recommendTask = new RecommendTask();
                recommendTask.execute("台北");
            }
        });

        cvReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserReminderTask reminderTask = new UserReminderTask();
                reminderTask.execute();
            }
        });

        cvNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                if (!navigationFragment.isAdded()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", R.drawable.navigation_location);
                    bundle.putBoolean("define", false);
                    navigationFragment.setArguments(bundle);
                    fragmentTransaction.add(R.id.container, navigationFragment, navigationFragment.getClass().getName());
                    fragmentTransaction.addToBackStack(navigationFragment.getClass().getName());
                }
                else {
                    fragmentTransaction.show(navigationFragment);
                }
                fragmentManager.executePendingTransactions();
                fragmentTransaction.commit();
            }
        });
    }

    private void setConnectToBluetoothListener() {
        sbConnectToBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (bluetoothHelperManager.checkSupportBluetooth()) {
                        if (!bluetoothHelperManager.isEnableBluetooth()) {
                            bluetoothHelperManager.openBluetooth();
                            if (!isLoginFragment) {
                                setConcernReminder();
                                userReminderTask();
                            }
                            else {
                                nonUserReminderTask();
                            }
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "你的手機不支援藍芽", Toast.LENGTH_LONG).show();
                        compoundButton.setChecked(false);
                    }
                }
                else {
                    disconnectBluetoothDevice();
                    removeConcernReminder();
                    removeUserReminder();
                    removeNonUserReminder();
                }
            }
        });
    }

    private void setConcernReminder() {
        Calendar now = Calendar.getInstance();
        Calendar morning = Calendar.getInstance();
        morning.setTimeInMillis(System.currentTimeMillis());
        morning.set(Calendar.HOUR_OF_DAY, 8);
        morning.set(Calendar.MINUTE, 0);
        morning.set(Calendar.SECOND, 0);
        morning.set(Calendar.MILLISECOND, 0);

        Calendar noon = Calendar.getInstance();
        noon.setTimeInMillis(System.currentTimeMillis());
        noon.set(Calendar.HOUR_OF_DAY, 12);
        noon.set(Calendar.MINUTE, 0);
        noon.set(Calendar.SECOND, 0);
        noon.set(Calendar.MILLISECOND, 0);

        Calendar night = Calendar.getInstance();
        night.setTimeInMillis(System.currentTimeMillis());
        night.set(Calendar.HOUR_OF_DAY, 17);
        night.set(Calendar.MINUTE, 0);
        night.set(Calendar.SECOND, 0);
        night.set(Calendar.MILLISECOND, 0);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (now.before(morning)) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("concern", "morning");
            PendingIntent pi = PendingIntent.getBroadcast(this, ReminderReceiver.MORNING_CONCERN, intent, PendingIntent.FLAG_ONE_SHOT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, morning.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

            intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("concern", "noon");
            pi = PendingIntent.getBroadcast(this, ReminderReceiver.NOON_CONCERN, intent, PendingIntent.FLAG_ONE_SHOT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, noon.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

            intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("concern", "night");
            pi = PendingIntent.getBroadcast(this, ReminderReceiver.NIGHT_CONCERN, intent, PendingIntent.FLAG_ONE_SHOT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, night.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
        else if (now.before(noon)) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("concern", "noon");
            PendingIntent pi = PendingIntent.getBroadcast(this, ReminderReceiver.NOON_CONCERN, intent, PendingIntent.FLAG_ONE_SHOT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, noon.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

            intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("concern", "night");
            pi = PendingIntent.getBroadcast(this, ReminderReceiver.NIGHT_CONCERN, intent, PendingIntent.FLAG_ONE_SHOT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, night.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
        else if (now.before(night)) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("concern", "night");
            PendingIntent pi = PendingIntent.getBroadcast(this, ReminderReceiver.NIGHT_CONCERN, intent, PendingIntent.FLAG_ONE_SHOT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, night.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
        else {
            Logger.d("not setting concern");
        }
    }

    private void setNeedReminder() {
        Calendar morning = Calendar.getInstance();
        morning.setTimeInMillis(System.currentTimeMillis());
        morning.set(Calendar.HOUR_OF_DAY, 8);
        morning.set(Calendar.MINUTE, 30);
        morning.set(Calendar.SECOND, 0);
        morning.set(Calendar.MILLISECOND, 0);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("need", "need");
        PendingIntent pi = PendingIntent.getBroadcast(this, ReminderReceiver.MORNING_NEED, intent, PendingIntent.FLAG_ONE_SHOT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, morning.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    private void removeConcernReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, ReminderReceiver.MORNING_CONCERN, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        intent = new Intent(this, ReminderReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                this, ReminderReceiver.NOON_CONCERN, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        intent = new Intent(this, ReminderReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                this, ReminderReceiver.NIGHT_CONCERN, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private void setUserReminder(List<HashMap<String, String>> reminder) {
        for (int i = 0; i < reminder.size(); i++) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            HashMap<String, String> map = reminder.get(i);
            String time = map.get("remind_time");
            String dosomething = map.get("dosomething");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                Calendar now = Calendar.getInstance();
                date = sdf.parse(time);
                Logger.d("reminder:" + date.toString());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                if (now.before(calendar)) {
                    Intent intent = new Intent(this, ReminderReceiver.class);
                    intent.putExtra("remind", dosomething);
                    PendingIntent pi = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,  calendar.getTimeInMillis(), pi);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        userReminderCount = reminder.size();
        setUserReminder = false;
    }

    private void userReminderTask() {
        setUserReminder = true;
        UserReminderTask reminderTask = new UserReminderTask();
        reminderTask.execute();
    }

    private void removeUserReminder() {
        if (userReminderCount != -1) {
            for (int i = 0; i < userReminderCount; i++) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, ReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, i, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private void nonUserReminderTask() {
        setNonUserReminder = true;
        NonUserReminderTask userReminderTask = new NonUserReminderTask();
        userReminderTask.execute();
    }

    private void setNonUserReminder(List<HashMap<String, String>> reminder) {
        for (int i = 0; i < reminder.size(); i++) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            HashMap<String, String> map = reminder.get(i);
            String time = map.get("remind_time");
            String dosomething = map.get("dosomething");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(time);
                Logger.d("reminder:" + date.toString());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra("remind", dosomething);
                PendingIntent pi = PendingIntent.getBroadcast(this, 100 + i, intent, PendingIntent.FLAG_ONE_SHOT);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,  calendar.getTimeInMillis(), pi);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        nonUserReminderCount = reminder.size();
        setNonUserReminder = false;
    }

    private void removeNonUserReminder() {
        if (nonUserReminderCount != -1) {
            for (int i = 0; i < nonUserReminderCount; i++) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, ReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, 100 + i, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
        }
    }


    // 參數初始化
    private void initParams() {
        checkPermission();
        checkTextToSpeech();
        speechRecognizerManager = new SpeechRecognizerManager(this);
        setSpeechRecognizerManagerListener();
        bluetoothHelperManager = new BluetoothHelperManager(MainActivity.this, getResources().getString(R.string.bluetooth_device_name), speechRecognizerManager);
    }

    // 動態要求使用者允許的權限
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 24) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    // 檢查TTS在使用者手機上是否存在
    private void checkTextToSpeech() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == TTS_CHECK_CODE) {
            // 如果TTS Engine有成功找到的話
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Logger.d("TTS is install");
                initialTextToSpeech();
            }
            // 如果 TTS 沒有安裝的話，則要求安裝
            else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
                Logger.d("TTS is not install");
            }
        }
    }


    // 初始化一個TextToSpeech instance，並實作onDone(講完話時)的method，需重新語音監聽
    private void initialTextToSpeech() {
        textToSpeechManager = new TextToSpeechManager(this);
        textToSpeechManager.setTextToSpeechListener(new TextToSpeechManager.onDone() {
            @Override
            public void onDone(String s) {
                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                Runnable speechRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (shareFlag.equals("hospital_phone")) {
                            phoneNumber = shareResponse.substring(shareResponse.indexOf("是") + 1, shareResponse.indexOf(","));
                            Logger.d("phoneNumber:" + phoneNumber);
                            speechRecognizerManager.startListenering();
                        }
                        else if (shareFlag.equals("hospital_address")) {
                            hospitalAddress = shareResponse.substring(shareResponse.indexOf("是") + 1, shareResponse.indexOf(","));
                            Logger.d("hospitalAddress:" + hospitalAddress);
                            speechRecognizerManager.startListenering();
                        }
                        else if (shareFlag.contains("done")) {
                            if (conversationCallback != null) {
                                conversationCallback.updateConversation();
                            }
                            if (shareFlag.equals("morning_concern_done") || shareFlag.equals("noon_concern_done") || shareFlag.equals("night_concern_done")) {
                                ConcernRelease concernRelease = new ConcernRelease();
                                concernRelease.execute(tvUserName.getText().toString());
                                isStart = false;
                                shareFlag = "";
                                speechRecognizerManager.startListenering();
                            }
                            else {
                                if (shareFlag.equals("user_done")) {
                                    //sbConnectToBluetooth.setChecked(false);
                                    isStart = false;
                                    shareFlag = "";
                                    LoginFragment fragment = (LoginFragment) fragmentManager.findFragmentByTag(new LoginFragment().getClass().getName());
                                    fragment.registerUser(true);
                                    Toast.makeText(getApplicationContext(), "您可以點選登入鍵進行登入了", Toast.LENGTH_SHORT).show();
                                }
                                else if (shareFlag.equals("reminder_done")) {
                                    isStart = false;
                                    shareFlag = "";
                                    speechRecognizerManager.startListenering();
                                    if (!isLoginFragment) {
                                        userReminderTask();
                                        if (reminderCallback != null) {
                                            reminderCallback.updateReminder();
                                        }
                                    }
                                    else {
                                        nonUserReminderTask();
                                    }
                                }
                                else if (shareFlag.equals("location_done")) {
                                    isStart = false;
                                    shareFlag = "";
                                    speechRecognizerManager.startListenering();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("查詢地點通知")
                                            .setMessage("導引至GoogleMap")
                                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    LocationTask locationTask = new LocationTask();
                                                    locationTask.execute();
                                                }
                                            })
                                            .setNegativeButton("不要", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
//                                                    Toast.makeText(getApplicationContext(), "no", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .show();
                                }
                                else if (shareFlag.equals("hospital_done")) {
                                    if (shareResponse.equals("好的，馬上為您撥打電話")) {
                                        isPhone = true;
                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                        intent.setData(Uri.parse("tel:" + phoneNumber));
                                        startActivity(intent);
                                    }
                                    if (shareResponse.equals("好的，馬上為您導航")) {
                                        Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                        List<Address> addressLocation = null;
                                        try {
                                            addressLocation = geoCoder.getFromLocationName(hospitalAddress, 1);
                                            double latitude = addressLocation.get(0).getLatitude();
                                            double longitude = addressLocation.get(0).getLongitude();
                                            String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", myLocation.getLatitude(), myLocation.getLongitude(),
                                                    latitude, longitude);
                                            Intent intent = new Intent();
                                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(url));
                                            startActivity(intent);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Logger.d(e.toString());
                                        }
                                        isStart = false;
                                        shareFlag = "";
                                        speechRecognizerManager.startListenering();
                                    }
                                    else {
                                        isStart = false;
                                        shareFlag = "";
                                        speechRecognizerManager.startListenering();
                                    }
                                }
                                else if (shareFlag.equals("emergency_done") && shareResponse.equals("好的，馬上為您撥打電話")) {
                                    ECPphoneTask ecPphoneTask = new ECPphoneTask();
                                    ecPphoneTask.execute();
                                }
                                else {
                                    isStart = false;
                                    shareFlag = "";
                                    speechRecognizerManager.startListenering();
                                }
                            }
                        }
                        else {
                            speechRecognizerManager.startListenering();
                        }
                    }
                };
                mainHandler.post(speechRunnable);
                Logger.d("TTS onDone");
            }
        });
    }

    // 設定SpeechRecognizerManager監聽器，並實作onResults、onError的Methods
    private void setSpeechRecognizerManagerListener() {
        speechRecognizerManager.setSpeechRecognizerListener(new SpeechRecognizerManager.onListener() {
            @Override
            public void onResults(Bundle bundle) {
                List<String> resList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                shareResponse = resList.get(0);
                Logger.d("語音辨識的結果:" + shareResponse);

                if (isStart) {
                    ChatResponseTask chatResponseTask = new ChatResponseTask();
                    chatResponseTask.execute(shareFlag, shareResponse);
                }
                else if (shareResponse.contains(getResources().getString(R.string.chatbot_start_word)) ||
                        shareResponse.contains(getResources().getString(R.string.chatbot_similar_start_word))) {
                    if (shareResponse.equals(getResources().getString(R.string.chatbot_start_word)) ||
                            shareResponse.contains(getResources().getString(R.string.chatbot_similar_start_word))) {
                        Logger.d("only Mango");
                        textToSpeechManager.speak(tvUserName.getText().toString() + "，您好，需要什麼服務嗎");
                    }
                    else {
                        Logger.d("Mango with something");
                        shareResponse = shareResponse.replace(getResources().getString(R.string.chatbot_start_word), "");
                        shareResponse = shareResponse.replace(getResources().getString(R.string.chatbot_similar_start_word), "");
                        ChatResponseTask chatResponseTask = new ChatResponseTask();
                        chatResponseTask.execute(shareFlag, shareResponse);
                    }
                    isStart = true;
                }
                else {
                    Logger.d("not Mango");
                    speechRecognizerManager.startListenering();
                }

            }
            @Override
            public void onError(int i) {
                Logger.d("語音辨識失敗碼:" + i);
                speechRecognizerManager.cancelSpeechRecognizer();
                speechRecognizerManager.startListenering();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        OpenActivityTask openActivityTask = new OpenActivityTask();
        openActivityTask.execute();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void doConcernTask(String concernType) {
        switch (concernType) {
            case "morning":
                MorningConcern morningConcern = new MorningConcern();
                morningConcern.execute(tvUserName.getText().toString());
                // todo 尚未測試
                NotifactionUtil.sendNotification(this, "每日提醒", "也要喝水囉", true);
                break;
            case "noon":
                NoonConcern noonConcern = new NoonConcern();
                noonConcern.execute(tvUserName.getText().toString());
                break;
            case "night":
                NightConcern nightConcern = new NightConcern();
                nightConcern.execute(tvUserName.getText().toString());
                break;
        }
    }

    @Override
    public void doReminderTask(String dosomething) {
        if (sbConnectToBluetooth.isChecked()) {
            speechRecognizerManager.cancelSpeechRecognizer();
            textToSpeechManager.speak(dosomething);
            NotifactionUtil.sendNotification(this, "個人提醒", dosomething, true);
        }
        else {
            NotifactionUtil.sendNotification(this, "個人提醒", dosomething, true);
        }
    }

    @Override
    public void doNeedTask() {
        NeedTask needTask = new NeedTask();
        needTask.execute();
    }

    @Override
    public void resume() {
        if (isPhone) {
            isPhone = false;
            isStart = false;
            shareFlag = "";
            speechRecognizerManager.startListenering();
        }

    }

    @Override
    public void updateNewReminder() {
        userReminderTask();
    }


    // 發出Post請求server進行語意分析，並回傳response(回話)
    private class ChatResponseTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                // 發送Robot訊息
                String userFlag = strings[0];
                String userSentence = strings[1];
                URL url = new URL(Url.baseUrl + Url.chatbotResponse);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("flag", userFlag);
                jsonParam.put("response", userSentence);
                writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                // 取得Robot回應
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                return jsonObject;
            } catch (Exception e) {
                Logger.d(e.toString());
            } finally {
                try {
                    writer.close();
                    reader.close();
                } catch (Exception e) {
                    Logger.e(e.toString());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                shareFlag = jsonObject.getString("flag");
                shareResponse = jsonObject.getString("response");
                textToSpeechManager.speak(shareResponse);
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private void disconnectBluetoothDevice() {
        bluetoothHelperManager.closeBluetooth();
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
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                UserInfoFragment userInfoFragment = new UserInfoFragment();
                Bundle bundle = new Bundle();
                bundle.putString("nickname", nickname);
                bundle.putString("height", height);
                bundle.putString("weight", weight);
                bundle.putString("age", age);
                bundle.putString("bmi_value", bmi_value);
                userInfoFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.container, userInfoFragment, userInfoFragment.getClass().getName());
                fragmentTransaction.addToBackStack(userInfoFragment.getClass().getName());
                fragmentManager.executePendingTransactions();
                fragmentTransaction.commit();
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
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
                while((line = reader.readLine()) != null) {
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
                Bundle bundle = new Bundle();
                ArrayList<Conversation> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    String question = jsonObject.get("question").toString();
                    String response = jsonObject.get("response").toString();
                    int dayIndex = jsonObject.get("date").toString().indexOf(" ");
                    int secondIndex = jsonObject.get("date").toString().lastIndexOf(":");
                    String date = jsonObject.get("date").toString().substring(dayIndex, secondIndex);
                    Conversation conversation = new Conversation(question, response, date);
                    list.add(conversation);
                }
                bundle.putSerializable("conversation", list);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ConversationFragment conversationFragment = new ConversationFragment();
                conversationFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.container, conversationFragment, conversationFragment.getClass().getName());
                fragmentTransaction.addToBackStack(conversationFragment.getClass().getName());
                fragmentManager.executePendingTransactions();
                fragmentTransaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private class RecommendTask extends AsyncTask<String, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(String... strings) {
            try {
                Map<String, String> map = new HashMap<>();
                String city = "";
                String latlng = String.valueOf(myLocation.getLatitude()) + "," + String.valueOf(myLocation.getLongitude());
                URL cityUrl = new URL("http://maps.google.com/maps/api/geocode/json?latlng=" + latlng + "&language=zh-CN&sensor=true");
                Logger.d(cityUrl.toString());
                HttpURLConnection httpURLConnection = (HttpURLConnection) cityUrl.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                httpURLConnection.disconnect();
                reader.close();

                // todo 命名
                JSONObject jsonObject = new JSONObject(content.toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("results");
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(0);
                JSONArray jsonArray1 = jsonObject1.getJSONArray("address_components");
                for (int i = 0; i < jsonArray1.length(); i++) {
                    JSONArray jsonArray2 = (JSONArray) jsonArray1.getJSONObject(i).get("types");
                    for (int j = 0; j < jsonArray2.length(); j++) {
                        if (jsonArray2.get(j).toString().equals("administrative_area_level_1")) {
                            city = jsonArray1.getJSONObject(i).get("long_name").toString();
                        }
                    }
                }
                map.put("city", city);

                URL needUrl = new URL(Url.baseUrl + Url.userNeed);
                URL weatherUrl = new URL(Url.baseUrl + Url.getWeather + city);
                httpURLConnection = (HttpURLConnection) needUrl.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                content = new StringBuilder();
                line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                httpURLConnection.disconnect();
                reader.close();

                jsonObject = new JSONObject(content.toString());
                jsonObject = (JSONObject) jsonObject.get("result");
                Logger.d(jsonObject.toString());
                map.put("water", jsonObject.get("needwater").toString());
                map.put("calorie", jsonObject.get("needcalorie").toString());

                httpURLConnection = (HttpURLConnection) weatherUrl.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                content = new StringBuilder();
                line = "";
                while((line = reader.readLine()) != null) {
                    content.append(line);
                }
                jsonObject = new JSONObject(content.toString());
                Logger.d(jsonObject.toString());
                jsonObject = (JSONObject) jsonObject.get("result");
                map.put("weatherDesc", jsonObject.get("Wx").toString());
                map.put("rainPro", jsonObject.get("PoP").toString());
                int highTemp = Integer.parseInt(jsonObject.get("MaxT").toString());
                int lowTemp = Integer.parseInt(jsonObject.get("MinT").toString());
                map.put("highTemp", jsonObject.get("MaxT").toString());
                map.put("lowTemp", jsonObject.get("MinT").toString());
                map.put("averTemp", String.valueOf((highTemp + lowTemp) / 2));
                map.put("info", jsonObject.get("info").toString());
                httpURLConnection.disconnect();
                reader.close();
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String> map) {
            try {
                String city = map.get("city");
                String water = map.get("water");
                String cal = map.get("calorie");
                String weatherDesc = map.get("weatherDesc");
                String highTemp = map.get("highTemp");
                String averTemp = map.get("averTemp");
                String lowTemp = map.get("lowTemp");
                String rainPro = map.get("rainPro");
                String info = map.get("info");
                Logger.d(map.toString());

                Bundle bundle = new Bundle();
                bundle.putString("water", water);
                bundle.putString("cal", cal);
                bundle.putString("weatherDesc", weatherDesc);
                bundle.putString("highTemp", highTemp);
                bundle.putString("averTemp", averTemp);
                bundle.putString("lowTemp", lowTemp);
                bundle.putString("rainPro", rainPro);
                bundle.putString("city", city);
                bundle.putString("info", info);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                RecommendFragment recommendFragment = new RecommendFragment();
                recommendFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.container, recommendFragment, recommendFragment.getClass().getName());
                fragmentTransaction.addToBackStack(recommendFragment.getClass().getName());
                fragmentManager.executePendingTransactions();
                fragmentTransaction.commit();
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private void openGPSSettings() {
        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            locationManager.requestLocationUpdates(bestProvider, 100 * 1000, 500,
                    this);
            if (location != null) {
                myLocation = location;
            }
        }
        else {
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivityForResult(intent,0);
        }
    }

    private String loadCookie() {
        SharedPreferences preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        return preferences.getString("cookie", "");
    }

    private class MorningConcern extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String nickName = strings[0];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.userConcernLock);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_nickname", nickName);
                writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                // 取得Robot回應
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
                    speechRecognizerManager.cancelSpeechRecognizer();
                    ChatResponseTask chatResponseTask = new ChatResponseTask();
                    chatResponseTask.execute(shareFlag, "morningconcern");
                    isStart = true;
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class NoonConcern extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String nickName = strings[0];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.userConcernLock);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_nickname", nickName);
                writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                // 取得Robot回應
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
                    speechRecognizerManager.cancelSpeechRecognizer();
                    ChatResponseTask chatResponseTask = new ChatResponseTask();
                    chatResponseTask.execute(shareFlag, "noonconcern");
                    isStart = true;
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class NightConcern extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String nickName = strings[0];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.userConcernLock);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_nickname", nickName);
                writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                // 取得Robot回應
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
                    speechRecognizerManager.cancelSpeechRecognizer();
                    ChatResponseTask chatResponseTask = new ChatResponseTask();
                    chatResponseTask.execute(shareFlag, "nightconcern");
                    isStart = true;
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class ConcernRelease extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String nickName = strings[0];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.userConcernRelease);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_nickname", nickName);
                writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(jsonParam.toString());
                writer.flush();

                // 取得Robot回應
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
                    Logger.d("success release");
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class LogoutTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.logoutUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();

                // 取得Robot回應
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
                if (jsonObject.getString("status").equals("200")) {
                    Logger.d("success logout");
                    jumpToLoginFragment();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class UserReminderTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.userReminder);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                httpURLConnection.connect();

                // 取得Robot回應
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
                List<HashMap<String, String>> reminderList = new ArrayList<>();
                if (jsonObject.getString("status").equals("200")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject reminder = jsonArray.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("remind_time", reminder.getString("remind_time"));
                        if (reminder.getString("dosomething").contains("活動:")) {
                            map.put("dosomething", reminder.getString("dosomething").substring(reminder.getString("dosomething").indexOf("活"), reminder.getString("dosomething").length()));
                        }
                        else {
                            map.put("dosomething", reminder.getString("dosomething"));
                        }
                        reminderList.add(map);
                    }
                    if (!setUserReminder) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        ReminderFragment reminderFragment = new ReminderFragment();
                        Bundle bundle = new Bundle();
                        if (isOpenActivityNotify) {
                            bundle.putBoolean("isOpenActivityNotify", true);
                            bundle.putString("title", openActivityTitle);
                            bundle.putString("startDate", openActivityStartDate);
                            isOpenActivityNotify = false;
                        }
                        else {
                            bundle.putBoolean("isOpenActivityNotify", false);
                        }
                        if (!reminderFragment.isAdded()) {
                            bundle.putSerializable("remind", (Serializable) reminderList);
                            reminderFragment.setArguments(bundle);
                            fragmentTransaction.add(R.id.container, reminderFragment, reminderFragment.getClass().getName());
                            fragmentTransaction.addToBackStack(reminderFragment.getClass().getName());
                        }
                        else {
                            fragmentTransaction.show(reminderFragment);
                        }
                        fragmentManager.executePendingTransactions();
                        fragmentTransaction.commit();
                    }
                    else {
                        setUserReminder(reminderList);
                    }
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class NonUserReminderTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.nonUserReminder);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.connect();

                // 取得Robot回應
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
                List<HashMap<String, String>> reminderList = new ArrayList<>();
                if (jsonObject.getString("status").equals("200")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject reminder = jsonArray.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("remind_time", reminder.getString("remind_time"));
                        map.put("dosomething", reminder.getString("dosomething"));
                        reminderList.add(map);
                    }
                    Logger.d(reminderList.toString());
                    if (!setNonUserReminder) {

                    }
                    else {
                        setNonUserReminder(reminderList);
                    }
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class LocationTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.lastLocation);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.connect();

                // 取得Robot回應
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
                HashMap<String, String> hashMap = new HashMap<>();
                String placeType = "";
                String region = "";
                String distance = "";
                String unit = "";
                String date = "";
                if (jsonObject.getString("status").equals("200")) {
                    JSONObject locationDetail = jsonObject.getJSONObject("result");
                    placeType = locationDetail.getString("location");
                    region = locationDetail.getString("region");
                    distance = locationDetail.getString("number");
                    unit = locationDetail.getString("unit");
                    date = locationDetail.getString("date");

                    Geocoder gc = new Geocoder(MainActivity.this, Locale.TRADITIONAL_CHINESE);
                    List<Address> listAddress = gc.getFromLocationName(region, 1);
                    double lat = listAddress.get(0).getLatitude();
                    double lng = listAddress.get(0).getLongitude();
                    hashMap.put("placeType", placeType);
                    hashMap.put("lat", String.valueOf(lat));
                    hashMap.put("lng", String.valueOf(lng));
                    hashMap.put("distance", distance);
                    hashMap.put("unit", unit);
                    hashMap.put("date", date);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("placeDetail", hashMap);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    GoogleMapFragment googleMapFragment = new GoogleMapFragment();
                    googleMapFragment.setArguments(bundle);
                    fragmentTransaction.add(R.id.container, googleMapFragment, googleMapFragment.getClass().getName());
                    fragmentTransaction.addToBackStack(googleMapFragment.getClass().getName());
                    fragmentTransaction.commit();
                }
                Logger.d(jsonObject.toString());
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class ECPtask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.getECP);
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
                List<Map<String, String>> contactList = new ArrayList<>();
                String name = "";
                String phone = "";
                String date = "";
                if (jsonObject.getString("status").equals("200")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject contact = jsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        name = contact.getString("person");
                        phone = contact.getString("phone");
                        date = contact.getString("date");

                        map.put("name", name);
                        map.put("phone", phone);
                        map.put("date", date);
                        contactList.add(map);
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("contactList", (Serializable) contactList);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    EmergencyFragment emergencyFragment = new EmergencyFragment();
                    emergencyFragment.setArguments(bundle);
                    fragmentTransaction.add(R.id.container, emergencyFragment, emergencyFragment.getClass().getName());
                    fragmentTransaction.addToBackStack(emergencyFragment.getClass().getName());
                    fragmentManager.executePendingTransactions();
                    fragmentTransaction.commit();
                }
                Logger.d(jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private class ECPphoneTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.getECPphone);
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
                if (jsonObject.getString("status").equals("200")) {
                    String phoneNumber = jsonObject.getJSONObject("result").getString("phone");
                    isPhone = true;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
                else {
                    isStart = false;
                    shareFlag = "";
                    speechRecognizerManager.startListenering();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private class NeedTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.userNeed);
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
                if (jsonObject.getString("status").equals("200")) {
                    String needWater = jsonObject.getJSONObject("result").getString("needwater");
                    String needCal = jsonObject.getJSONObject("result").getString("needcalorie");
                    NotifactionUtil.sendNotification(MainActivity.this, "每日提醒", "今日應攝取的喝水量:" + needWater + "ml" + "\n" + "今日應攝取的熱量:" + needCal + "cal", true);
                }
                else {
                    isStart = false;
                    shareFlag = "";
                    speechRecognizerManager.startListenering();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    private class OpenActivityTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.getActivity);
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
                if (jsonObject.getString("status").equals("200")) {
                    Type listType = new TypeToken<List<OpenActivity>>() {}.getType();
                    List<OpenActivity> tempOpenActivityList = new Gson().fromJson(jsonObject.getJSONArray("result").toString(), listType);
                    openActivityList = new ArrayList<>();
                    for (OpenActivity openActivity : tempOpenActivityList) {
                        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
                        Date startDate = sdf.parse(openActivity.getStartDate());
                        Date endDate = sdf.parse(openActivity.getEndDate());
                        Calendar nowCalendar = Calendar.getInstance();
                        Calendar startCalendar = Calendar.getInstance();
                        Calendar endCalendar = Calendar.getInstance();
                        startCalendar.setTime(startDate);
                        endCalendar.setTime(endDate);
                        if (!openActivity.getLatitude().equals("") && !openActivity.getLongitude().equals("")) {
                            if (nowCalendar.before(startCalendar) || (nowCalendar.after(startCalendar) && nowCalendar.before(endCalendar))) {
                                double distance = getDistance(openActivity.getLatitude(), openActivity.getLongitude());
                                if (distance <= 0.5) {
                                    openActivityList.add(openActivity);
                                }
                            }
                        }
                    }
                    for (OpenActivity openActivity : openActivityList) {
                        Logger.d(openActivity.getTitle());
                        int length = 0;
                        if (openActivity.getDisc().length() > 250) {
                            length = 250;
                        }
                        else {
                            length = openActivity.getDisc().length();
                        }
                        NotifactionUtil.sendNotification(MainActivity.this, openActivity.getTitle(), "活動日期:" + openActivity.getStartDate() + "~" + openActivity.getEndDate() + "\n" + openActivity.getDisc().substring(0, length) + "......", false);
                    }
                }
                else {

                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    // 回傳公里
    private double getDistance(String lat1Str, String lng1Str) {
        final double EARTH_RADIUS = 6378.137;
        Double lat1 = Double.parseDouble(lat1Str);
        Double lng1 = Double.parseDouble(lng1Str);
        Double lat2 = myLocation.getLatitude();
        Double lng2 = myLocation.getLongitude();

        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = lng1 * Math.PI / 180.0 - lng2 * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    @Override
    public void onBackPressed()
    {
        int count = fragmentManager.getBackStackEntryCount();

        if (count == 0 && !isLoginFragment) {
            if (backPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                LogoutTask logoutTask = new LogoutTask();
                logoutTask.execute();
                return;
            }
            else {
                Toast.makeText(getApplicationContext(), "再點選一次即將登出", Toast.LENGTH_SHORT).show();
            }
            backPressed = System.currentTimeMillis();
        }
        else {
            if (fragmentManager.getBackStackEntryAt(count - 1).getName().equals(TypeFragment.class.getName())) {
                typeCallback.backToMain();
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //點擊訊息欄到主Activity時 會執行這個方法
        super.onNewIntent(intent);
        getNotify(intent);
        setIntent(intent);
    }

    private void getNotify(Intent intent) {
        //拿到參數后 首先判斷是不是為空
        String value = intent.getStringExtra("toValue");
        if (!TextUtils.isEmpty(value)) {
            switch (value) {
                //在進行判斷需要做的操作
                case "ReminderFragment":
                    isOpenActivityNotify = true;
                    openActivityTitle = intent.getStringExtra("title");
                    for (OpenActivity openActivity : openActivityList) {
                        if (openActivity.getTitle().equals(openActivityTitle)) {
                            openActivityStartDate = openActivity.getStartDate();
                            break;
                        }
                    }
                    UserReminderTask userReminderTask = new UserReminderTask();
                    userReminderTask.execute();
                    break;
            }
        }
        //做完操作以后必須將toValue的值初始化
        intent.putExtra("toValue", "");
        super.onNewIntent(intent);
    }

}

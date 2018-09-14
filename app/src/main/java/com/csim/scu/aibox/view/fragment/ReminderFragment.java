package com.csim.scu.aibox.view.fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.csim.scu.aibox.R;
import com.csim.scu.aibox.callback.LoginFragmentCallback;
import com.csim.scu.aibox.callback.ReminderCallback;
import com.csim.scu.aibox.callback.ReminderFragmentCallback;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.model.OpenActivity;
import com.csim.scu.aibox.network.Url;
import com.csim.scu.aibox.receiver.ReminderReceiver;
import com.csim.scu.aibox.util.NotifactionUtil;
import com.csim.scu.aibox.view.activity.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.LastOwnerException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ReminderFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener, ReminderCallback {

    private FragmentManager fragmentManager;
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView weekView;
    private Button btDateSelector;
    private Calendar startTime;
    private List<HashMap<String, String>> reminderList = new ArrayList<>();
    private Dialog addRemindDialog;
    private ReminderFragmentCallback reminderFragmentCallback;
    private boolean isOpenActivityNotify = false;
    private String openActivityTitle = "";
    private Location myLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        reminderList = (List<HashMap<String, String>>) this.getArguments().get("remind");
        fragmentManager = getFragmentManager();
        hideBar();
        isOpenActivityNotify = this.getArguments().getBoolean("isOpenActivityNotify");
        if (isOpenActivityNotify) {
            openActivityTitle = this.getArguments().getString("title");
        }
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        weekView = view.findViewById(R.id.weekView);
        btDateSelector = view.findViewById(R.id.btDateSelect);
        setWeekView();
        setDateSelectorListener();
        if (isOpenActivityNotify) {
            addRemindDialog = new Dialog(getActivity());
            addRemindDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            addRemindDialog.setCancelable(false);
            addRemindDialog.setContentView(R.layout.reminder_dialog);
            final EditText etDate = addRemindDialog.findViewById(R.id.etDate);
            final EditText etTime = addRemindDialog.findViewById(R.id.etTime);
            final EditText etThing = addRemindDialog.findViewById(R.id.etThing);
            etDate.setInputType(InputType.TYPE_NULL);
            etThing.setText(openActivityTitle);
            etTime.setInputType(InputType.TYPE_NULL);
            etDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean isFocus) {
                    if (isFocus) {
                        showDatePickerDialog(etDate);
                    }
                }
            });
            etDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialog(etDate);
                }
            });
            etTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean isFocus) {
                    if (isFocus) {
                        showTimePickerDialog(etTime);
                    }
                }
            });
            etTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTimePickerDialog(etTime);
                }
            });
            Button btYes = addRemindDialog.findViewById(R.id.btYes);
            Button btNo = addRemindDialog.findViewById(R.id.btNo);
            btYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (etDate.getText().toString().equals("") || etTime.getText().toString().equals("") || etThing.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "請輸入正確的資訊", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String remind_time = etDate.getText().toString() + " " + etTime.getText().toString();
                        String thing = etThing.getText().toString();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = sdf.format(Calendar.getInstance().getTime());
                        AddUserReminderTask addUserReminderTask = new AddUserReminderTask();
                        addUserReminderTask.execute(remind_time, thing, date);
                    }
                }
            });
            btNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addRemindDialog.dismiss();
                }
            });
            addRemindDialog.show();
        }
    }


    // 隱藏標題
    private void hideBar() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setWeekView() {
        weekView.setOnEventClickListener(this);
        weekView.setMonthChangeListener(this);
        weekView.setEventLongPressListener(this);
        weekView.setEmptyViewLongPressListener(this);
        weekView.setMonthChangeListener(this);
        setupDateTimeInterpreter(false);
    }

    private void setDateSelectorListener() {
        btDateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        weekView.goToDate(calendar);
                    }
                }, year, month, day).show();
            }
        });
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < reminderList.size(); i++) {
            HashMap<String, String> map = reminderList.get(i);
            String remind_time = map.get("remind_time");
            String dosomething = map.get("dosomething");
            try {
                Date date = sdf.parse(remind_time);
                Calendar startTime = Calendar.getInstance();
                startTime.setTime(date);
                if (startTime.get(Calendar.YEAR) == newYear && startTime.get(Calendar.MONTH) == newMonth) {
                    Calendar endTime = Calendar.getInstance();
                    endTime.setTime(date);
                    endTime.add(Calendar.HOUR, 1);
                    WeekViewEvent event = new WeekViewEvent(i, getEventTitle(startTime, dosomething), startTime, endTime);
                    event.setColor(getResources().getColor(R.color.event_color_01));
                    events.add(event);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Logger.d(e.toString());
            }
        }
        return events;
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(getActivity(), event.getName(), Toast.LENGTH_SHORT).show();
    }

    // todo 點擊之後出現dialog 更改時段
    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        if (event.getName().contains("活動:")) {
            final String title = event.getName().substring(event.getName().lastIndexOf(":") + 1, event.getName().length());
            Logger.d(title);
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.reminder_navigation_dialog);
            Button btYes = dialog.findViewById(R.id.btYes);
            Button btNo = dialog.findViewById(R.id.btNo);
            btYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OpenActivityTask openActivityTask = new OpenActivityTask(title);
                    openActivityTask.execute();
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
    }

    // todo 主動添加
    @Override
    public void onEmptyViewLongPress(Calendar time) {
        addRemindDialog = new Dialog(getActivity());
        addRemindDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addRemindDialog.setCancelable(false);
        addRemindDialog.setContentView(R.layout.reminder_dialog);
        final EditText etDate = addRemindDialog.findViewById(R.id.etDate);
        final EditText etTime = addRemindDialog.findViewById(R.id.etTime);
        final EditText etThing = addRemindDialog.findViewById(R.id.etThing);
        etDate.setInputType(InputType.TYPE_NULL);
        etTime.setInputType(InputType.TYPE_NULL);
        etDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (isFocus) {
                    showDatePickerDialog(etDate);
                }
            }
        });
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(etDate);
            }
        });
        etTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (isFocus) {
                    showTimePickerDialog(etTime);
                }
            }
        });
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(etTime);
            }
        });
        Button btYes = addRemindDialog.findViewById(R.id.btYes);
        Button btNo = addRemindDialog.findViewById(R.id.btNo);
        btYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etDate.getText().toString().equals("") || etTime.getText().toString().equals("") || etThing.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "請輸入正確的資訊", Toast.LENGTH_SHORT).show();
                }
                else {
                    String remind_time = etDate.getText().toString() + " " + etTime.getText().toString();
                    String thing = etThing.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    AddUserReminderTask addUserReminderTask = new AddUserReminderTask();
                    addUserReminderTask.execute(remind_time, thing, date);
                }
            }
        });
        btNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRemindDialog.dismiss();
            }
        });
        addRemindDialog.show();
    }

    private void showDatePickerDialog(final EditText etDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String chooseDate = sdf.format(calendar.getTime());
                etDate.setText(chooseDate);
            }
        }, year, month, day).show();
    }

    private void showTimePickerDialog(final EditText etTime) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                String chooseTime = sdf.format(calendar.getTime());
                etTime.setText(chooseTime);
            }
        }, hour, minute, false).show();
    }


    protected String getEventTitle(Calendar time, String dosomething) {
        return String.format("%02d:%02d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE)) + "\n" + dosomething;
    }

    private void setupDateTimeInterpreter(final boolean shortDate) {
        weekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    @Override
    public void updateReminder() {
        UserReminderTask userReminderTask = new UserReminderTask();
        userReminderTask.execute();
        Logger.d("updateReminder");
    }

    @Override
    public void updateLocation(Location location) {
        this.myLocation = location;
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
                reminderList = new ArrayList<>();
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
                    weekView.notifyDatasetChanged();
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

    private class AddUserReminderTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String reminder_time = strings[0];
            String thing = strings[1];
            String date = strings[2];
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(Url.baseUrl + Url.addUserReminder);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("cookie", loadCookie());
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("remind_time", reminder_time);
                jsonParam.put("dosomething", thing);
                jsonParam.put("date", date);
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
                if (!jsonObject.getString("status").equals("200")) {
                    addRemindDialog.dismiss();
                    Toast.makeText(getActivity(), "新增提醒事件失敗", Toast.LENGTH_SHORT).show();
                }
                else {
                    addRemindDialog.dismiss();
                    UserReminderTask userReminderTask = new UserReminderTask();
                    userReminderTask.execute();
                    reminderFragmentCallback.updateNewReminder();
                    Toast.makeText(getActivity(), "新增提醒事件成功", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }

    private class OpenActivityTask extends AsyncTask<String, Void, JSONObject> {

        private String openActivityTitle;
        public OpenActivityTask(String openActivityTitle) {
            this.openActivityTitle = openActivityTitle;
        }

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
                boolean hasActivity = false;
                if (jsonObject.getString("status").equals("200")) {
                    Type listType = new TypeToken<List<OpenActivity>>() {}.getType();
                    List<OpenActivity> tempOpenActivityList = new Gson().fromJson(jsonObject.getJSONArray("result").toString(), listType);
                    for (OpenActivity openActivity : tempOpenActivityList) {
                        if (openActivity.getTitle().equals(openActivityTitle)) {
                            String url = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s", myLocation.getLatitude(),
                                    myLocation.getLongitude(), openActivity.getLatitude(), openActivity.getLongitude());
                            Intent intent = new Intent();
                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                            hasActivity = true;
                            break;
                        }
                    }
                    if (!hasActivity) {
                        Toast.makeText(getActivity(), "找不到此活動", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getActivity(), "找不到此活動", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.toString());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        reminderFragmentCallback = (ReminderFragmentCallback) getActivity();
    }

}

package com.csim.scu.aibox.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.view.activity.MainActivity;

/**
 * Created by kenny on 2018/9/1.
 */

public class NotifactionUtil {

    private static NotificationManager manager;
    private static final String id = "chenxh_id";
    private static final int remind_id = 1;
    private static final int openActivity_id = 2;
    private static final String name = "chenxh_name";
    private static long[] vibrate = {0,100,200,300};
    //音樂Uri參數
    private static Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private static NotificationManager getManager(Context context){
        if (manager == null){
            manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @TargetApi(26)
    private static void createNotificationChannel(Context context){
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        getManager(context).createNotificationChannel(channel);
    }

    @TargetApi(26)
    private static Notification.Builder getChannelNotification(Context context, String title, String content, boolean isRemind){

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
//        remoteViews.setImageViewResource(R.mipmap.ic_launcher, R.mipmap.ic_launcher);
//        remoteViews.setTextViewText(R.id.title, "我是標題");
//        remoteViews.setTextViewText(R.id.content, "我是内容");
        Notification.BigTextStyle style = new Notification.BigTextStyle();
        PendingIntent contentIntent;
        if (isRemind) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(context, MainActivity.class));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
            style.setBigContentTitle("提醒");
        }
        else {
            Intent toFragment = new Intent(Intent.ACTION_MAIN);
            toFragment.putExtra("toValue", "ReminderFragment");
            toFragment.putExtra("title", "活動:" + title);
            toFragment.addCategory(Intent.CATEGORY_LAUNCHER);
            toFragment.setComponent(new ComponentName(context, MainActivity.class));
            toFragment.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            contentIntent = PendingIntent.getActivity(context, 0, toFragment, PendingIntent.FLAG_UPDATE_CURRENT);
            style.setBigContentTitle("附近活動:" + title);
        }
        style.bigText(content);
        return new Notification.Builder(context, id)
                        .setContentIntent(contentIntent)
                        .setFullScreenIntent(contentIntent, true)
                        .setSmallIcon(R.drawable.robot_avatar) // 設置狀態列裡面的圖示（小圖示）　　
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.robot_avatar)) // 下拉下拉清單裡面的圖示（大圖示）
                        .setTicker("notification on status bar.") // 設置狀態列的顯示的資訊
                        .setWhen(System.currentTimeMillis())// 設置時間發生時間
                        .setAutoCancel(false) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                        .setContentTitle(title) // 設置下拉清單裡的標題
                        .setStyle(style)
                        .setOngoing(false)      //true使notification變為ongoing，用戶不能手動清除// notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
                        .setDefaults(Notification.DEFAULT_VIBRATE) //使用所有默認值，比如聲音，震動，閃屏等等;
                        .setVibrate(vibrate) //自訂震動長度
                        .setAutoCancel(true);
                        //.setCustomContentView(remoteViews);

    }
    private static NotificationCompat.Builder getNotification_25(Context context, String title, String content){
        return new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(true);
    }

    public static void sendNotification(Context context,String title, String content, boolean isRemind){
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(context);//NotificationChannel 创建一个Notification，再去Builder
            Notification notification = getChannelNotification
                    (context,title, content , isRemind).build();
            // 重複的聲響,直到用戶響應。
            notification.flags = Notification.FLAG_INSISTENT;
            // 把指定ID的通知持久的發送到狀態條上
            if (isRemind) {
                getManager(context).notify(remind_id,notification);//管理者发送消息
            }
            else {
                getManager(context).notify(openActivity_id,notification);//管理者发送消息
            }
        }
        else {
            Notification notification = getNotification_25(context,title, content).build();//先Builder
            getManager(context).notify(1,notification);//管理者发送消息
        }
    }

}

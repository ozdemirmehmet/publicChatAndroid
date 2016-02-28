package com.likapalab.mehmet.publicchat.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.likapalab.mehmet.publicchat.R;
import com.likapalab.mehmet.publicchat.SplashScreen;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
 
    public GcmIntentService() {
        super("GcmIntentService");
    }
 
    @Override
    protected void onHandleIntent(Intent intent) {

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        // get the info from the currently running task
        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        //if  app is running
        if(!componentInfo.getPackageName().equalsIgnoreCase("com.likapalab.mehmet.publicchat")) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            //Gelen mesaj tipini aliyoruz
            String messageType = gcm.getMessageType(intent);
            String mesaj = intent.getExtras().getString("notification_message");

            if (!extras.isEmpty()) {
            
            /*if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            } else */
                if (GoogleCloudMessaging.
                        MESSAGE_TYPE_MESSAGE.equals(messageType)) {//Herhangi bir sorun yoksa Notification mizi olusturacak methodu cagiriyoruz
                    sendNotification(mesaj);
                }
            }
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void sendNotification(String msg) { //Burda Status barda gosterilecek Notificationin ayarlari yapiliyor(titresim,bildirim,text boyutu vs..)
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashScreen.class), 0);
 
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Public Chat")
                .setSmallIcon(R.drawable.app_logo)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());//Notification gosteriliyor.
    }
}
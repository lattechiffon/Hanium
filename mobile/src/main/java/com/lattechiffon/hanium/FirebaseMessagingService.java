package com.lattechiffon.hanium;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    //private static final String TAG = "FirebaseMsgService";
    SharedPreferences settingPref;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(remoteMessage.getData().get("message")); // 푸쉬 알림 발생
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        settingPref = PreferenceManager.getDefaultSharedPreferences(this);
        String customRingtone = settingPref.getString("notifications_new_message_ringtone", "default");
        Uri ringtoneSoundUri;

        if (customRingtone.equals("default")) {
            ringtoneSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            ringtoneSoundUri = Uri.parse(customRingtone);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setTicker(messageBody)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setSound(ringtoneSoundUri)
                .setContentIntent(pendingIntent)
                .setLights(Color.CYAN, 500, 2000);

        notificationBuilder.setPriority(Notification.PRIORITY_MAX);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        if (!pm.isInteractive()) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");

            wl.acquire(5000);
        }

        notificationManager.notify(2017, notificationBuilder.build());
    }

}
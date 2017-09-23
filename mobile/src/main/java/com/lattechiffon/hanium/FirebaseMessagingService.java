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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Firebase Cloud Message(FCM) 푸시 알림을 담당하는 서비스 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendEmergencyNotification(remoteMessage.getData().get("message"));
    }

    /**
     * 푸시로 통지된 낙상사고 발생 정보를 처리하는 메서드입니다.
     *
     * @param messageBody 낙상사고 발생 정보
     */
    private void sendEmergencyNotification(String messageBody) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ProtectorNotificationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            JSONObject json = new JSONObject(messageBody);

            intent.putExtra("user_name", json.getJSONObject("user").getString("name"));
            intent.putExtra("user_phone", json.getJSONObject("user").getString("phone"));
            intent.putExtra("location_address", json.getJSONObject("gps").getString("address"));
            intent.putExtra("location_longitude", json.getJSONObject("gps").getDouble("longitude"));
            intent.putExtra("location_latitude", json.getJSONObject("gps").getDouble("latitude"));
            intent.putExtra("location_accuracy", json.getJSONObject("gps").getDouble("accuracy"));
            intent.putExtra("beacon_spot", json.getJSONObject("beacon").getString("spot"));
            intent.putExtra("beacon_distance", json.getJSONObject("beacon").getInt("distance"));
        } catch (JSONException e) {
            return;
        }

        startActivity(intent);
    }

    /**
     * 푸시로 통지된 공지사항 정보를 처리하는 메서드입니다.
     *
     * @param messageBody 낙상사고 발생 정보
     */
    private void sendNewsNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        SharedPreferences settingPref = PreferenceManager.getDefaultSharedPreferences(this);

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
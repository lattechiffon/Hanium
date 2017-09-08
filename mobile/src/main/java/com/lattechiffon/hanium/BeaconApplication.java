package com.lattechiffon.hanium;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;

/**
 * 애플리케이션 최초 설치 및 실행 시점에 수행되는 클래스입니다.
 * 백그라운드에서 비콘을 모니터링하는 서비스가 활성화됩니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
public class BeaconApplication extends Application {
    private BeaconManager beaconManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super .onCreate();

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"),
                        34378, 4469));
            }
        });
        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> list) {
                pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
                editor = pref.edit();

                showNotification("비콘 영역으로 들어오셨습니다.", "비콘 영역에서의 서비스 설정에 따라 앱이 동작합니다.");

                if (!pref.getBoolean("beacon", false)) {
                    editor.putBoolean("beacon", true);
                    editor.apply();
                }
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) {
                pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
                editor = pref.edit();

                showNotification("비콘 영역에서 벗어나셨습니다.", "기본 서비스 설정에 따라 앱이 동작합니다.");

                if (pref.getBoolean("beacon", false)) {
                    editor.putBoolean("beacon", false);
                    editor.apply();
                }
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}

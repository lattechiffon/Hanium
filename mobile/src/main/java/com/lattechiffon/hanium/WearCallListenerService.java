package com.lattechiffon.hanium;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 웨어 디바이스로부터 데이터를 수신하는 기능을 담당하는 서비스 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
public class WearCallListenerService extends WearableListenerService {
    public static String SERVICE_CALLED_WEAR = "WearFallRecognition";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        String event = messageEvent.getPath();
        String [] message = event.split("--");

        if (message[0].equals(SERVICE_CALLED_WEAR)) {
            switch(message[1]) {
                case "fall":
                    if (!pref.getBoolean("fall", false)) {
                        editor.putBoolean("fall", true);
                        editor.apply();

                        String currentDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

                        databaseHelper.insert("INSERT INTO FALLING_RECORD(datetime, result, valid) VALUES('" + currentDatetime + "', 1, 1);");

                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), EmergencyActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        editor.putBoolean("fall", false);
                        editor.apply();
                    }

                    break;
                case "stop":
                    if (pref.getBoolean("fall", false)) {
                        editor.putBoolean("fall", false);
                        editor.commit();

                        int topNo = databaseHelper.selectTopNo();

                        databaseHelper.update("UPDATE FALLING_RECORD SET result = 0 WHERE no = " + topNo + ";");
                    }

                    break;
            }
        }
    }
}

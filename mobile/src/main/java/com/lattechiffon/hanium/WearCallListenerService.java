package com.lattechiffon.hanium;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearCallListenerService extends WearableListenerService {
    public static String SERVICE_CALLED_WEAR = "WearFallRecognition";

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        String event = messageEvent.getPath();

        String [] message = event.split("--");

        if (message[0].equals(SERVICE_CALLED_WEAR))  {
            Log.d("수신", message[1]);
            switch(message[1]) {
                case "fall":
                    Log.d("현재 상태", pref.getBoolean("fall", false) + "");
                    if (!pref.getBoolean("fall", false)) {
                        editor.putBoolean("fall", true);
                        editor.commit();

                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), EmergencyActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        editor.putBoolean("fall", false);
                        editor.commit();
                    }

                    break;
                case "stop":
                    Log.d("현재 상태", pref.getBoolean("fall", false) + "");
                    if (pref.getBoolean("fall", false)) {
                        editor.putBoolean("fall", false);
                        editor.commit();
                    }

                    break;
            }


            //startActivity(new Intent((Intent) SplashActivity.getInstance().tutorials.get(message[1]))
                    //.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}

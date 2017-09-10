package com.lattechiffon.hanium;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * 모바일 디바이스로부터 데이터를 수신하는 기능을 담당하는 서비스 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class MobileCallListenerService extends WearableListenerService {
    public static String SERVICE_CALLED_MOBILE = "MobileStopFeedback";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        String event = messageEvent.getPath();
        String [] message = event.split("--");

        SharedPreferences pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if (message[0].equals(SERVICE_CALLED_MOBILE)) {
            switch(message[1]) {
                case "stop":
                    if (pref.getBoolean("fall", false)) {
                        editor.putBoolean("fall", false);
                        editor.apply();
                    }

                    break;
            }
        }
    }
}

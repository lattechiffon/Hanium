package com.lattechiffon.hanium;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Firebase Cloud Message(FCM) 토큰 발급을 담당하는 서비스 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    // private static final String TAG = "FirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();

        // sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                .build();
        Request request = new Request.Builder()
                .url("http://www.lattechiffon.com/gauss/app/register.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
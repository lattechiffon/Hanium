package com.lattechiffon.hanium;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    //private static final String TAG = "FirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken(); // 새로운 Token 할당
        // Log.d(TAG, "Refreshed token: " + token);

        //sendRegistrationToServer(token); // 서버 등록
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                .build();

        //request
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
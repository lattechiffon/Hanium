package com.lattechiffon.hanium;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.unstoppable.submitbuttonview.SubmitButton;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DeviceRegisterActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false; // 앱 종료를 판별하기 위한 변수

    EditText nameInput;
    BackgroundTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_device_register));
        }


        final SubmitButton submitButton = (SubmitButton) findViewById(R.id.submitButton);
        nameInput = (EditText) findViewById(R.id.nameInput);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submitButton.doResult(true);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setClass(DeviceRegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                //DeviceRegisterActivity.this.finish();
                            }
                        }, 1500);

                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.toast_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {

        String name;
        String phone;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            name = nameInput.getText().toString();
        }

        @Override
        protected okhttp3.Response doInBackground(String... arg0) {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("name", name)
                    .add("phone", FirebaseInstanceId.getInstance().getToken())
                    .add("token", FirebaseInstanceId.getInstance().getToken())
                    .build();

            Request request = new Request.Builder()
                    .url("http://www.lattechiffon.com/hanium/register.php")
                    .post(body)
                    .build();

            try {
                return client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(okhttp3.Response a) {
            super.onPostExecute(a);
            try {
                Toast.makeText(DeviceRegisterActivity.this, "" + a.body().string(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {

            }
        }
    }
}
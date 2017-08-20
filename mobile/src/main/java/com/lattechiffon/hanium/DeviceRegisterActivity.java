package com.lattechiffon.hanium;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.unstoppable.submitbuttonview.SubmitButton;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DeviceRegisterActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false; // 앱 종료를 판별하기 위한 변수
    boolean nameIsEntered = false;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    SubmitButton submitButton;
    EditText nameInput;
    EditText phoneInput;
    TextView welcomeText;
    TextView infoText;
    BackgroundTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_device_register));
        }

        submitButton = (SubmitButton) findViewById(R.id.submitButton);
        nameInput = (EditText) findViewById(R.id.nameInput);
        phoneInput = (EditText) findViewById(R.id.phoneInput);
        welcomeText = (TextView) findViewById(R.id.deviceRegisterTitle);
        infoText = (TextView) findViewById(R.id.deviceRegisterTitle2);

        pref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!nameIsEntered) {
                    nameIsEntered = true;
                    infoText.setText(R.string.device_register_title2_change);
                    nameInput.setVisibility(View.GONE);
                    phoneInput.setVisibility(View.VISIBLE);
                    submitButton.reset();

                    return;
                }

                task = new BackgroundTask();
                task.execute();
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
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {

        String name;
        String phone;
        String push;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            name = nameInput.getText().toString();
            phone = phoneInput.getText().toString();
            push = FirebaseInstanceId.getInstance().getToken();
        }

        @Override
        protected okhttp3.Response doInBackground(String... arg0) {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("name", name)
                    .add("phone", phone)
                    .add("token", push)
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
                JSONObject json = new JSONObject(a.body().string());

                if (json.getString("result").equals("Authorized")) {
                    submitButton.doResult(true);

                    editor.putBoolean("deviceRegister", true);
                    editor.putString("name", name);
                    editor.putString("phone", phone);
                    editor.commit();

                    Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.setClass(DeviceRegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            DeviceRegisterActivity.this.finish();
                        }
                    }, 1500);
                } else {
                    submitButton.doResult(false);

                    welcomeText.setText(R.string.device_register_title_error);
                    infoText.setText(R.string.device_register_title2_error);

                    Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            submitButton.reset();

                            nameIsEntered = false;
                            nameInput.setText("");
                            phoneInput.setText("");
                            phoneInput.setVisibility(View.GONE);
                            nameInput.setVisibility(View.VISIBLE);
                            welcomeText.setText(R.string.device_register_title);
                            infoText.setText(R.string.device_register_title2);
                        }
                    }, 2000);
                }

            } catch (Exception e) {
                submitButton.doResult(false);

                welcomeText.setText(R.string.device_register_title_error);
                infoText.setText(R.string.device_register_title2_error);

                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submitButton.reset();

                        nameIsEntered = false;
                        nameInput.setText("");
                        phoneInput.setText("");
                        phoneInput.setVisibility(View.GONE);
                        nameInput.setVisibility(View.VISIBLE);
                        welcomeText.setText(R.string.device_register_title);
                        infoText.setText(R.string.device_register_title2);
                    }
                }, 2000);
            }
        }
    }
}
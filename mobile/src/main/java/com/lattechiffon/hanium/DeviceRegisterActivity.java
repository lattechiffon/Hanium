package com.lattechiffon.hanium;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
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

/**
 * 이용자 기기 정보를 서버에 등록하는 기능을 제공하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class DeviceRegisterActivity extends AppCompatActivity {
    public final int PERMISSIONS_READ_PHONE_STATE = 3;

    private BackgroundTask task;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private SubmitButton submitButton;
    private EditText nameInput;
    private EditText phoneInput;
    private TextView welcomeText;
    private TextView infoText;

    private boolean doubleBackToExitPressedOnce = false;
    private boolean nameIsEntered = false;
    private boolean autoPhoneNumber = false;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_device_register));
        }

        pref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        submitButton = (SubmitButton) findViewById(R.id.submitButton);
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

        nameInput = (EditText) findViewById(R.id.nameInput);
        phoneInput = (EditText) findViewById(R.id.phoneInput);
        welcomeText = (TextView) findViewById(R.id.deviceRegisterTitle);
        infoText = (TextView) findViewById(R.id.deviceRegisterTitle2);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceRegisterActivity.this);
                builder.setTitle(getString(R.string.permission_dialog_title_read_phone_state));
                builder.setMessage(getString(R.string.permission_dialog_body_read_phone_state)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(DeviceRegisterActivity.this, new String[] { android.Manifest.permission.READ_PHONE_STATE }, PERMISSIONS_READ_PHONE_STATE);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_PHONE_STATE }, PERMISSIONS_READ_PHONE_STATE);
            }
        } else {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                autoPhoneNumber = true;
                phoneNumber = telephonyManager.getLine1Number();
                phoneNumber = phoneNumber.replace("+82", "0");
                phoneInput.setText(phoneNumber);
                phoneInput.setFocusable(false);
                phoneInput.setClickable(false);
            } catch (NullPointerException ignored) { }
        }
    }

    /**
     * 새로운 기기 등록 프로세스를 수행하는 내부 클래스입니다.
     * 서버와의 통신을 담당하여 처리합니다.
     *
     * UI를 수정하는 작업은 메인 쓰레드에서 처리하여야 합니다.
     * onPreExecute() 혹은 onPostExecute() 메서드에서 처리하여 주십시오.
     *
     * @version 1.0
     * @author  Yongguk Go (lattechiffon@gmail.com)
     */
    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {

        private String name;
        private String phone;
        private String push;

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
                return null;
            }

        }

        @Override
        protected void onPostExecute(okhttp3.Response response) {
            super.onPostExecute(response);

            try {
                JSONObject json = new JSONObject(response.body().string());

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

                    return;
                }
            } catch (Exception ignored) { }

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
                    if (!autoPhoneNumber) {
                        phoneInput.setText("");
                    }
                    phoneInput.setVisibility(View.GONE);
                    nameInput.setVisibility(View.VISIBLE);
                    welcomeText.setText(R.string.device_register_title);
                    infoText.setText(R.string.device_register_title2);
                }
            }, 2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                    autoPhoneNumber = true;
                    phoneNumber = telephonyManager.getLine1Number();
                    phoneNumber = phoneNumber.replace("+82", "0");
                    phoneInput.setText(phoneNumber);
                    phoneInput.setFocusable(false);
                    phoneInput.setClickable(false);
                    Toast.makeText(DeviceRegisterActivity.this, getString(R.string.permission_toast_allow_read_phone_state), Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceRegisterActivity.this);
                    builder.setTitle(getString(R.string.permission_dialog_title_deny));
                    builder.setMessage(getString(R.string.permission_dialog_body_read_phone_state)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
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
}
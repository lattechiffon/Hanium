package com.lattechiffon.hanium;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 애플리케이션 초기 데이터 로드를 담당하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class SplashActivity extends Activity {
    public final int PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    private BackgroundTask task;
    private SharedPreferences pref, settingPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!networkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setTitle(getString(R.string.error_title_network));
            builder.setMessage(getString(R.string.error_body_network)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic("notice");
            FirebaseMessaging.getInstance().subscribeToTopic("emergency");
            FirebaseMessaging.getInstance().subscribeToTopic("feedback");
            FirebaseInstanceId.getInstance().getToken();

            pref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
            settingPref = PreferenceManager.getDefaultSharedPreferences(this);
            editor = pref.edit();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    builder.setTitle(getString(R.string.permission_dialog_title_access_fine_location));
                    builder.setMessage(getString(R.string.permission_dialog_body_access_fine_location)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
                }
            } else {
                if (pref.getBoolean("deviceRegister", false)) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            task = new BackgroundTask();
                            task.execute();
                        }
                    }, 1000);
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), DeviceRegisterActivity.class));
                            SplashActivity.this.finish();
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        }
                    }, 1000);
                }
            }
        }
    }

    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {
        private ProgressDialog progressDialog = new ProgressDialog(SplashActivity.this);
        private String name;
        private String phone;
        private String push;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog.getWindow() != null) {
                progressDialog.getWindow().setGravity(Gravity.BOTTOM);
            }

            progressDialog.setMessage(getString(R.string.info_load_user_data));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            name = pref.getString("name", "");
            phone = pref.getString("phone", "");
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
                    .url("http://104.199.248.120/login.php")
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
        protected void onPostExecute(okhttp3.Response response) {
            super.onPostExecute(response);

            progressDialog.dismiss();

            try {
                JSONObject json = new JSONObject(response.body().string());

                if (json.getString("result").equals("Authorized")) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                } else {
                    editor.putBoolean("deviceRegister", false);
                    editor.commit();

                    Toast.makeText(SplashActivity.this, getString(R.string.info_login_failed), Toast.LENGTH_LONG).show();

                    startActivity(new Intent(getApplicationContext(), DeviceRegisterActivity.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }

            } catch (Exception e) {
                editor.putBoolean("deviceRegister", false);
                editor.commit();

                Toast.makeText(SplashActivity.this, getString(R.string.info_login_failed), Toast.LENGTH_LONG).show();

                startActivity(new Intent(getApplicationContext(), DeviceRegisterActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        }
    }

    private boolean networkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(SplashActivity.this, getString(R.string.permission_toast_allow_access_fine_location), Toast.LENGTH_LONG).show();

                    if (pref.getBoolean("deviceRegister", false)) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                task = new BackgroundTask();
                                task.execute();
                            }
                        }, 1000);
                    } else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), DeviceRegisterActivity.class));
                                SplashActivity.this.finish();
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            }
                        }, 1000);
                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    builder.setTitle(getString(R.string.permission_dialog_title_deny));
                    builder.setMessage(getString(R.string.permission_dialog_body_access_fine_location)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
    }
}

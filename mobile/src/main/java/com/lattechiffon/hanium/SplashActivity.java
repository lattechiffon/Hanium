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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends Activity {
    public final int PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    SharedPreferences pref, settingPref;
    BackgroundTask task;
    String name, phone, result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        pref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        settingPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (!networkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setTitle("네트워크에 연결되지 않았습니다.");
            builder.setMessage("로그인 서버에 접근할 수 없습니다.\n기기의 네트워크 연결 상태를 확인하여 주십시오.").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        FirebaseMessaging.getInstance().subscribeToTopic("notice");
        FirebaseMessaging.getInstance().subscribeToTopic("emergency");
        FirebaseMessaging.getInstance().subscribeToTopic("feedback");
        FirebaseInstanceId.getInstance().getToken();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle(getString(R.string.permission_dialog_title_access_fine_location));
                builder.setMessage(getString(R.string.permission_dialog_body_access_fine_location)).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(SplashActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_ACCESS_FINE_LOCATION);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        } else {
            if (pref.getBoolean("deviceRegister", false)) {
                name = pref.getString("name", "");
                phone = pref.getString("phone", "");

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
                    }
                }, 1000);
            }
        }
    }

    private boolean loginValidation(String loginData) {
        try {
            JSONObject json = new JSONObject(loginData);

            if (json.getString("result").equals("Authorized")) {

                return true;
            } else if (json.getString("result").equals("UpdatesRecommended")) {
                Toast.makeText(SplashActivity.this, "이 버전에서는 더 이상 로그인할 수 없습니다. 구글 플레이에서 최신 버전으로 업데이트해주시기 바랍니다.", Toast.LENGTH_LONG).show();

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                    startActivity(intent);
                }

                return false;
            } else {
                Toast.makeText(SplashActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_LONG).show();

                return false;
            }
        } catch (JSONException e) {
            Toast.makeText(SplashActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_LONG).show();

            return false;
        }
    }

    private class BackgroundTask extends AsyncTask<String, Integer, String> {
        ProgressDialog AsycDialog = new ProgressDialog(SplashActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AsycDialog.setMessage("잠시만 기다려주십시오.");
            AsycDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            result = request("http://www.lattechiffon.com/gauss/app/login_query.php");

            return result;
        }

        @Override
        protected void onPostExecute(String a) {
            super.onPostExecute(a);
            AsycDialog.dismiss();

            if (loginValidation(result)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                SplashActivity.this.finish();
            } else {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                SplashActivity.this.finish();
            }
        }
    }

    private String request(String urlStr) {
        StringBuilder json = new StringBuilder();
        String parameter = "name=" + name + "&phone=" + phone + "&token=" + FirebaseInstanceId.getInstance().getToken() + "&push=" + settingPref.getBoolean("notifications_new_message", true) + "&version=" + getString(R.string.app_version_code) + "";

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                os.write(parameter.getBytes("utf-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        json.append(line).append("");
                    }

                    reader.close();

                }
                conn.disconnect();
            }
        } catch (Exception e) {
            Toast.makeText(SplashActivity.this, "서버와의 통신 과정에 문제가 발생했습니다.", Toast.LENGTH_LONG).show();
        }

        return json.toString();
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(SplashActivity.this, getString(R.string.permission_toast_allow_access_fine_location), Toast.LENGTH_LONG).show();

                    if (pref.getBoolean("deviceRegister", false)) {
                        name = pref.getString("name", "");
                        phone = pref.getString("phone", "");

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
                            }
                        }, 1000);
                    }

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    builder.setTitle(getString(R.string.permission_dialog_title_deny));
                    builder.setMessage(getString(R.string.permission_dialog_body_access_fine_location)).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return;
            }
        }
    }
}

package com.lattechiffon.hanium;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.unstoppable.submitbuttonview.SubmitButton;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EmergencyActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false; // 앱 종료를 판별하기 위한 변수
    int timerCount;

    BackgroundTask task;
    LocationManager locationManager;
    Vibrator vibrator;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_emergency));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        final SubmitButton stopButton = (SubmitButton) findViewById(R.id.stopButton);
        infoText = (TextView) findViewById(R.id.emergencyInfo);
        timerCount = 10;

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopButton.doResult(true);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setClass(EmergencyActivity.this, MainActivity.class);
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

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500};
        vibrator.vibrate(pattern, -1);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // ToDo: 사용자가 비콘 지역 내에 존재하는 경우 아래의 GPS를 통한 위치 정보는 가져오지 않도록 수정한다.

        /* GPS 데이터 호출 : 원칙적으로 네트워크 제공자와 GPS 데이터를 동시에 가져온다. */
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (mSec)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (mSec)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
        } catch (SecurityException e) {

        }

        delayTimer.sendEmptyMessage(0);
    }

    Handler delayTimer = new Handler() {
        public void handleMessage(Message msg) {
            if (!pref.getBoolean("fall", false)) {
                vibrator.cancel();
                locationManager.removeUpdates(mLocationListener);

                editor.putBoolean("fall", false);
                editor.commit();

                finish();
            } else {
                infoText.setText(timerCount + "초 후 지정된 보호자에게 응급 푸쉬가 발송됩니다.");

                if (timerCount-- > 0) {
                    delayTimer.sendEmptyMessageDelayed(0, 1000);
                } else {
                    locationManager.removeUpdates(mLocationListener);
                    Toast.makeText(EmergencyActivity.this, "등록된 모든 보호자에게 응급 푸쉬가 발송되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); // 경도
            double latitude = location.getLatitude();   // 위도
            double altitude = location.getAltitude();   // 고도
            float accuracy = location.getAccuracy();    // 정확도
            String provider = location.getProvider();   // 위치 제공자

            Toast.makeText(getApplicationContext(), "위치 정보를 가져오고 있습니다.", Toast.LENGTH_LONG).show();

            //Toast.makeText(getApplicationContext(), "위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude + "\n고도 : " + altitude + "\n정확도 : "  + accuracy, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

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

            name = "테스트";
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
                Toast.makeText(EmergencyActivity.this, "" + a.body().string(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {

            }
        }
    }
}
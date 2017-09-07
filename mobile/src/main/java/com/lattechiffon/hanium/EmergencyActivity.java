package com.lattechiffon.hanium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.unstoppable.submitbuttonview.SubmitButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EmergencyActivity extends AppCompatActivity {

    int timerCount;
    int protectorCount;

    BackgroundTask task;
    LocationManager locationManager;
    private BeaconManager beaconManager;
    Vibrator vibrator;

    SharedPreferences pref, userPref;
    SharedPreferences.Editor editor;

    TextView infoText;
    SubmitButton stopButton;

    BeaconRegion beaconRegion;
    int beaconDistance;
    double longitude, latitude, altitude;
    float accuracy;

    String provider;

    DatabaseHelper databaseHelper;

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

        databaseHelper = new DatabaseHelper(getApplicationContext());

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    beaconDistance = Math.abs(nearestBeacon.getRssi());
                }
            }
        });

        beaconRegion = new BeaconRegion(
                "monitored region",
                UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"),
                34378, 4469);

        pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
        userPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        stopButton = (SubmitButton) findViewById(R.id.stopButton);
        infoText = (TextView) findViewById(R.id.emergencyInfo);
        timerCount = 15;

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
        long[] pattern = {0, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 3000};
        vibrator.vibrate(pattern, -1);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        while (true) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);
                break;
            } catch (SecurityException e) {
                continue;
            }
        }

        delayTimer.sendEmptyMessage(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
              @Override
              public void onServiceReady() {
                  beaconManager.startRanging(beaconRegion);
              }
          });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(beaconRegion);

        super.onPause();
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
                if (timerCount == 5) {
                    stopButton.setVisibility(View.INVISIBLE);
                }

                infoText.setText(timerCount + "초 후 지정된 보호자에게 응급 푸쉬가 발송됩니다.");

                if (timerCount-- > 0) {
                    delayTimer.sendEmptyMessageDelayed(0, 1000);
                } else {
                    locationManager.removeUpdates(mLocationListener);

                    editor.putBoolean("fall", false);
                    editor.commit();

                    task = new BackgroundTask();
                    task.execute();
                }
            }
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude(); // 경도
            latitude = location.getLatitude();   // 위도
            altitude = location.getAltitude();   // 고도
            accuracy = location.getAccuracy();
            provider = location.getProvider();

            Toast.makeText(getApplicationContext(), "위치 정보를 가져오고 있습니다.", Toast.LENGTH_LONG).show();
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

    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {

        ProgressDialog progressDialog = new ProgressDialog(EmergencyActivity.this);
        JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.getWindow().setGravity(Gravity.BOTTOM);
            progressDialog.setMessage("지정된 모든 보호자에게 낙상사고를 알리는 중입니다.");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected okhttp3.Response doInBackground(String... arg0) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            jsonObject = getProtectorList();

            if (jsonObject == null) {
                return null;
            }

            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
                    .url("http://www.lattechiffon.com/hanium/send_notification.php")
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

            progressDialog.dismiss();

            if (a == null) {
                Toast.makeText(EmergencyActivity.this, "오류가 발생하였습니다.", Toast.LENGTH_LONG).show();

                return;
            }

            Toast.makeText(EmergencyActivity.this, "지정된 모든 보호자에게 낙상사고 발생을 통보하였습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private JSONObject getProtectorList() {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        String[] data = databaseHelper.selectProtectorAll();

        protectorCount = data.length;

        if (protectorCount == 0) {
            return null;
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray jsonProtectorArray = new JSONArray();
            for (int i = 0; i < protectorCount; i++) {
                JSONObject jsonDataObject = new JSONObject();

                jsonDataObject.put("no", data[i]);
                jsonProtectorArray.put(jsonDataObject);
            }

            jsonObject.put("protector", jsonProtectorArray);

            JSONObject jsonDataObject = new JSONObject();

            jsonDataObject.put("name", userPref.getString("name", "null"));
            jsonDataObject.put("phone", userPref.getString("phone", "null"));
            jsonObject.put("user", jsonDataObject);

            JSONObject jsonLocationDataObject = new JSONObject();

            jsonLocationDataObject.put("longitude", longitude);
            jsonLocationDataObject.put("latitude", latitude);
            jsonLocationDataObject.put("altitude", altitude);
            jsonObject.put("gps", jsonLocationDataObject);

            if (pref.getBoolean("beacon", false)) {
                JSONObject jsonBeaconDataObject = new JSONObject();

                jsonBeaconDataObject.put("spot", userPref.getString("beacon_spot", "home"));
                jsonBeaconDataObject.put("distance", beaconDistance);
                jsonObject.put("beacon", jsonBeaconDataObject);
            }
        } catch (JSONException e) {
            return null;
        }

        return jsonObject;
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
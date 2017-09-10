package com.lattechiffon.hanium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.unstoppable.submitbuttonview.SubmitButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 낙상사고 발생 감지 시 이용자에게 통지하는 기능을 담당하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class EmergencyActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    public static String SERVICE_CALLED_MOBILE = "MobileStopFeedback";

    private LocationManager locationManager;
    private BeaconManager beaconManager;
    private AudioManager audioManager;
    private GoogleApiClient mGoogleApiClient;
    private Vibrator vibrator;
    private BackgroundTask task;
    private SharedPreferences pref, userPref;
    private SharedPreferences.Editor editor;

    private SubmitButton stopButton;
    private TextView infoText;

    private Node mNode;
    private boolean mResolvingError = false;
    private BeaconRegion beaconRegion;
    private int timerCount, protectorCount, beaconDistance = -1;
    private double longitude, latitude;
    private float accuracy;
    private String provider;

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        while (true) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, locationListener);

                break;
            } catch (SecurityException ignored) {
            }
        }

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    beaconDistance = Math.abs(nearestBeacon.getRssi());

                    Toast.makeText(getApplicationContext(), getString(R.string.info_location), Toast.LENGTH_LONG).show();
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 3000};

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:

                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                vibrator.vibrate(pattern, -1);

                break;

            case AudioManager.RINGER_MODE_NORMAL:
                vibrator.vibrate(pattern, -1);

                break;
        }

        stopButton = (SubmitButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopButton.doResult(true);

                sendMessage("stop");
            }
        });
        infoText = (TextView) findViewById(R.id.emergencyInfo);
        timerCount = 15;

        delayTimer.sendEmptyMessage(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
              @Override
              public void onServiceReady() {beaconManager.startRanging(beaconRegion);
              }
          });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(beaconRegion);
        super.onPause();
    }

    private Handler delayTimer = new Handler() {
        public void handleMessage(Message msg) {
            if (!pref.getBoolean("fall", false)) {
                vibrator.cancel();
                locationManager.removeUpdates(locationListener);

                editor.putBoolean("fall", false);
                editor.apply();

                finish();
            } else {
                if (timerCount == 5) {
                    stopButton.setVisibility(View.INVISIBLE);
                }

                infoText.setText(String.format(getString(R.string.info_emergency_delay), timerCount));

                if (timerCount-- > 0) {
                    delayTimer.sendEmptyMessageDelayed(0, 1000);
                } else {
                    locationManager.removeUpdates(locationListener);

                    editor.putBoolean("fall", false);
                    editor.apply();

                    task = new BackgroundTask();
                    task.execute();
                }
            }
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            accuracy = location.getAccuracy();
            provider = location.getProvider();

            Toast.makeText(getApplicationContext(), getString(R.string.info_location), Toast.LENGTH_LONG).show();
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

    /**
     * 모든 보호자에게 낙상사고 발생 푸시 전송을 수행하는 내부 클래스입니다.
     * 서버와의 통신을 담당하여 처리합니다.
     *
     * UI를 수정하는 작업은 메인 쓰레드에서 처리하여야 합니다.
     * onPreExecute() 혹은 onPostExecute() 메서드에서 처리하여 주십시오.
     *
     * @version 1.0
     * @author  Yongguk Go (lattechiffon@gmail.com)
     */
    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {
        private ProgressDialog progressDialog = new ProgressDialog(EmergencyActivity.this);
        private JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (progressDialog.getWindow() != null) {
                progressDialog.getWindow().setGravity(Gravity.BOTTOM);
            }

            progressDialog.setMessage(getString(R.string.info_send_push));
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
                return null;
            }
        }

        @Override
        protected void onPostExecute(okhttp3.Response response) {
            super.onPostExecute(response);

            progressDialog.dismiss();

            if (response == null) {
                Toast.makeText(EmergencyActivity.this, getString(R.string.info_error), Toast.LENGTH_LONG).show();

                return;
            }

            Toast.makeText(EmergencyActivity.this, getString(R.string.info_send_push_complete), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * 이용자와 모든 보호자에 대한 정보를 반환하는 메서드입니다.
     *
     * @return 이용자 및 모든 보호자 정보
     */
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
            jsonLocationDataObject.put("accuracy", accuracy);
            jsonObject.put("gps", jsonLocationDataObject);

            if (pref.getBoolean("beacon", false)) {
                JSONObject jsonBeaconDataObject = new JSONObject();

                jsonBeaconDataObject.put("spot", userPref.getString("beacon_spot", "집"));
                jsonBeaconDataObject.put("distance", beaconDistance);
                jsonObject.put("beacon", jsonBeaconDataObject);
            } else {
                JSONObject jsonBeaconDataObject = new JSONObject();

                jsonBeaconDataObject.put("spot", userPref.getString("beacon_spot", "empty"));
                jsonBeaconDataObject.put("distance", -1);
                jsonObject.put("beacon", jsonBeaconDataObject);
            }
        } catch (JSONException e) {
            return null;
        }

        return jsonObject;
    }

    /**
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Send message to wear device
     */
    private void sendMessage(String Key) {

        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SERVICE_CALLED_MOBILE + "--" + Key, null).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                // error
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onBackPressed() {

    }
}
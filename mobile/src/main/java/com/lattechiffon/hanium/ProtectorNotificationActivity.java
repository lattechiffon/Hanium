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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ramotion.foldingcell.FoldingCell;
import com.unstoppable.submitbuttonview.SubmitButton;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProtectorNotificationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    boolean doubleBackToExitPressedOnce = false; // 앱 종료를 판별하기 위한 변수

    Intent intent;
    Vibrator vibrator;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    TextView nameTextView, locationTextView, locationDetailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protector_notification);

        intent = getIntent();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_emergency));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pref = getSharedPreferences("EmergencyData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500, 500};
        vibrator.vibrate(pattern, 6);

        nameTextView = (TextView) findViewById(R.id.protector_cell_header_name);
        nameTextView.setText(intent.getStringExtra("user_name") + " 님");

        locationTextView = (TextView) findViewById(R.id.content_body_address);
        locationTextView.setText(intent.getStringExtra("location_latitude") + " / " + intent.getStringExtra("location_longitude"));

        locationDetailTextView = (TextView) findViewById(R.id.content_detail_address);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        LatLng fallLocation = new LatLng(intent.getDoubleExtra("location_latitude", 0), intent.getDoubleExtra("location_longitude", 0));
        this.googleMap.addMarker(new MarkerOptions().position(fallLocation).title("낙상사고 발생 지점"));
        this.googleMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(fallLocation));
        this.googleMap.setMinZoomPreference(6.0f);
        this.googleMap.setMaxZoomPreference(20.0f);
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
}
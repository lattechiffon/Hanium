package com.lattechiffon.hanium;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * 낙상사고 발생 감지 시 보호자에게 통지하는 기능을 담당하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class ProtectorNotificationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Vibrator vibrator;
    private Intent intent;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protector_notification);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_emergency));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        long[] pattern = {0, 1500, 500, 1500, 500, 1500, 500, 1500, 500, 1500, 500};
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, 6);

        intent = getIntent();

        TextView nameTextView = (TextView) findViewById(R.id.protector_cell_header_name);
        nameTextView.setText(intent.getStringExtra("user_name") + " 님");

        TextView locationTextView = (TextView) findViewById(R.id.content_body_address);
        locationTextView.setText(intent.getStringExtra("location_address"));

        TextView locationDetailTextView = (TextView) findViewById(R.id.content_detail_address);
        locationDetailTextView.setText("68%의 정확도로 반경 " + (int) intent.getDoubleExtra("location_accuracy", 0) + "m 이내");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng fallLocation = new LatLng(intent.getDoubleExtra("location_latitude", 0), intent.getDoubleExtra("location_longitude", 0));

        this.googleMap = googleMap;
        this.googleMap.addMarker(new MarkerOptions().position(fallLocation).title("낙상사고 발생 지점"));
        this.googleMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(fallLocation));
        this.googleMap.setMinZoomPreference(6.0f);
        this.googleMap.setMaxZoomPreference(20.0f);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            vibrator.cancel();
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
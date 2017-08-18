package com.lattechiffon.hanium;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
//import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ramotion.foldingcell.FoldingCell;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

public class MainActivity extends AppCompatActivity {
    public final int PERMISSIONS_READ_CONTACTS = 2;
    boolean doubleBackToExitPressedOnce = false; // 앱 종료를 판별하기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        //SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
                switch (newState) {
                    case EXPANDED:
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(getString(R.string.activity_main_recent_data));
                        }

                        break;

                    case COLLAPSED:
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(getString(R.string.app_name));
                        }

                        break;
                }
            }
        });

        Button newContactsButton = (Button) findViewById(R.id.newContactsButton);

        newContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.permission_dialog_title_read_contacts));
                        builder.setMessage(getString(R.string.permission_dialog_title_read_contacts)).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_CONTACTS }, PERMISSIONS_READ_CONTACTS);
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_CONTACTS }, PERMISSIONS_READ_CONTACTS);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ContactsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            }
        });

        final FoldingCell fc = (FoldingCell) findViewById(R.id.folding_cell);
        fc.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               fc.toggle(false);
           }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this, getString(R.string.permission_toast_allow_read_contacts), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ContactsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.permission_dialog_title_deny));
                    builder.setMessage(getString(R.string.permission_dialog_body_read_contacts)).setCancelable(false).setPositiveButton("확인", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        SlidingUpPanelLayout panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (panel.getPanelState() == PanelState.EXPANDED) {
            panel.setPanelState(PanelState.COLLAPSED);
        } else {
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
}

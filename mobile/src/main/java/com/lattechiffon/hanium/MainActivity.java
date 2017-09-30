package com.lattechiffon.hanium;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ramotion.foldingcell.FoldingCell;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

/**
 * 애플리케이션 초기 화면을 담당하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class MainActivity extends AppCompatActivity {
    public final int PERMISSIONS_READ_CONTACTS = 2;

    private SwipeRefreshLayout swipeRefreshLayout;
    private FallingRecordListViewAdapter adapter;
    private SharedPreferences pref;

    private ListView listView;
    private TextView countTextView, welcomeTextView;

    private boolean doubleBackToExitPressedOnce = false;
    private int listItemDataCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        pref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);

        countTextView = (TextView) findViewById(R.id.falling_count_text_view);
        welcomeTextView = (TextView) findViewById(R.id.welcome_text_view);

        welcomeTextView.setText(String.format(getString(R.string.main_welcome), pref.getString("name", "비인증 사용자")));

        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                loadListItem();
            }
        });

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
                        builder.setMessage(getString(R.string.permission_dialog_title_read_contacts)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
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
        final FoldingCell fc2 = (FoldingCell) findViewById(R.id.folding_cell_2);
        final FoldingCell fc3 = (FoldingCell) findViewById(R.id.folding_cell_3);

        fc.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               fc.toggle(false);
               fc2.fold(false);
               fc3.fold(false);
           }
        });

        fc2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fc.fold(false);
                fc2.toggle(false);
                fc3.fold(false);
            }
        });

        fc3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fc.fold(false);
                fc2.fold(false);
                fc3.toggle(false);
            }
        });

        adapter = new FallingRecordListViewAdapter();
        loadListItem();

        listView = (ListView) findViewById(R.id.listViewFallingRecord);
        listView.setAdapter(adapter);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return adapter.getCount() > 1;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    databaseHelper.insert("UPDATE FALLING_RECORD SET valid = 0 WHERE no = " + adapter.getItemNo(adapter.getItem(position)) + ";");
                                    adapter.remove(adapter.getItem(position));
                                    listItemDataCount--;
                                    countTextView.setText(String.format(getString(R.string.main_sliding_up_panel_title), listItemDataCount));
                                }
                            }
                        });
        listView.setOnTouchListener(touchListener);
        listView.setOnScrollListener(touchListener.makeScrollListener());

    }

    public void loadListItem() {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        String[][] data = databaseHelper.selectFallingRecordAll();
        listItemDataCount = data.length;

        for (int i = 0; i < listItemDataCount; i++) {
            adapter.addItem(Integer.parseInt(data[i][0]), data[i][1], Integer.parseInt(data[i][2]));
        }

        if (Integer.parseInt(data[data.length - 1][0]) != 0) {
            countTextView.setText(String.format(getString(R.string.main_sliding_up_panel_title), listItemDataCount));
        } else {
            countTextView.setText(getString(R.string.main_sliding_up_panel_title_list_empty));
        }

        adapter.notifyDataSetChanged();

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this, getString(R.string.info_list_refresh), Toast.LENGTH_LONG).show();
        }


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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, getString(R.string.permission_toast_allow_read_contacts), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ContactsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.permission_dialog_title_deny));
                    builder.setMessage(getString(R.string.permission_dialog_body_read_contacts)).setCancelable(false).setPositiveButton(getString(R.string.dialog_ok), null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
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

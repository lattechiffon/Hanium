package com.lattechiffon.hanium;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ramotion.foldingcell.FoldingCell;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.Calendar;

/**
 * 애플리케이션 초기 화면을 담당하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
public class MainActivity extends AppCompatActivity {
    public final int PERMISSIONS_READ_CONTACTS = 2;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout birthLayout, genderLayout, bloodTypeLayout, diseaseLayout, beaconLocationLayout;
    private FallingRecordListViewAdapter adapter;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private ListView listView;
    private TextView countTextView, countProtectorView, welcomeTextView;

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
        editor = pref.edit();

        countTextView = (TextView) findViewById(R.id.falling_count_text_view);
        countProtectorView = (TextView) findViewById(R.id.content_body_protector);
        welcomeTextView = (TextView) findViewById(R.id.welcome_text_view);

        welcomeTextView.setText(String.format(getString(R.string.main_welcome), pref.getString("name", "비인증 사용자")));

        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        countProtectorView.setText(String.format(getString(R.string.main_cell_body_protector), databaseHelper.countProtector()));

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

        birthLayout = (RelativeLayout) findViewById(R.id.content_birth);
        genderLayout = (RelativeLayout) findViewById(R.id.content_gender);
        bloodTypeLayout = (RelativeLayout) findViewById(R.id.content_blood_type);
        diseaseLayout = (RelativeLayout) findViewById(R.id.content_disease);
        beaconLocationLayout = (RelativeLayout) findViewById((R.id.content_beacon_location));

        final TextView birthTextView = (TextView) findViewById(R.id.content_body_birth);
        final TextView ageTextView = (TextView) findViewById(R.id.content_detail_birth);
        final TextView genderTextView = (TextView) findViewById(R.id.content_body_gender);
        final TextView genderDetailTextView = (TextView) findViewById(R.id.content_detail_gender);
        final TextView bloodTypeTextView = (TextView) findViewById(R.id.content_body_blood_type);
        final TextView bloodTypeDetailTextView = (TextView) findViewById(R.id.content_detail_blood_type);
        final TextView diseaseTextView = (TextView) findViewById(R.id.content_body_disease);
        final TextView beaconLocationTextView = (TextView) findViewById(R.id.content_body_beacon_location);

        birthTextView.setText(pref.getString("birth", getString(R.string.main_cell_not_register)));
        bloodTypeTextView.setText(pref.getString("bloodType", getString(R.string.main_cell_not_register)));
        bloodTypeDetailTextView.setText(pref.getString("rhType", getString(R.string.main_cell_not_register)));
        diseaseTextView.setText(pref.getString("diseaseRecord", getString(R.string.main_cell_not_register)));
        beaconLocationTextView.setText(pref.getString("beacon_spot", getString(R.string.main_cell_body_beacon_location)));

        int age = pref.getInt("age", -1);
        if (age != -1) {
            ageTextView.setText(String.format(getString(R.string.main_cell_detail_birth), age));
        } else {
            ageTextView.setText(getString(R.string.main_cell_not_register));
        }

        String gender = pref.getString("gender", getString(R.string.main_cell_not_register));
        switch (gender) {
            case "M":
                genderTextView.setText(getString(R.string.main_cell_body_gender_male));
                genderDetailTextView.setText(getString(R.string.main_cell_detail_gender_male));

                break;
            case "F":
                genderTextView.setText(getString(R.string.main_cell_body_gender_female));
                genderDetailTextView.setText(getString(R.string.main_cell_detail_gender_female));

                break;
            default:
                genderTextView.setText(getString(R.string.main_cell_not_register));

                break;
        }

        birthLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int year, month, day;

                if (!pref.getString("birth", "null").equals("null")) {
                    String[] birth = birthTextView.getText().toString().split("/");

                    year = Integer.parseInt(birth[0]);
                    month = Integer.parseInt(birth[1]) - 1;
                    day = Integer.parseInt(birth[2]);
                } else {
                    Calendar current = Calendar.getInstance();
                    year = current.get(Calendar.YEAR);
                    month = current.get(Calendar.MONTH);
                    day = current.get(Calendar.DAY_OF_MONTH);
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        String birth = year + "/" + (month + 1) + "/" + day;
                        birthTextView.setText(birth);

                        Calendar current = Calendar.getInstance();
                        int currentYear = current.get(Calendar.YEAR);
                        int currentMonth = current.get(Calendar.MONTH) + 1;
                        int currentDay = current.get(Calendar.DAY_OF_MONTH);
                        int age = currentYear - year;

                        if (month * 100 + day > currentMonth * 100 + currentDay) {
                            age --;
                        }

                        ageTextView.setText(String.format(getString(R.string.main_cell_detail_birth), age));

                        editor.putString("birth", birth);
                        editor.putInt("age", age);
                        editor.apply();
                    }
                }, year, month, day);

                datePickerDialog.show();

                return true;
            }
        });

        genderLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(getString(R.string.main_cell_title_gender))
                        .setItems(R.array.gender_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        genderTextView.setText(getString(R.string.main_cell_body_gender_male));
                                        genderDetailTextView.setText(getString(R.string.main_cell_detail_gender_male));
                                        editor.putString("gender", "M");
                                        editor.apply();

                                        break;

                                    case 1:
                                        genderTextView.setText(getString(R.string.main_cell_body_gender_female));
                                        genderDetailTextView.setText(getString(R.string.main_cell_detail_gender_female));
                                        editor.putString("gender", "F");
                                        editor.apply();

                                        break;
                                }
                            }
                        });

                builder.show();

                return true;
            }
        });

        bloodTypeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(getString(R.string.main_cell_title_blood_type))
                        .setItems(R.array.blood_type_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        editor.putString("bloodType", "A");
                                        editor.putString("rhType", "Rh+");
                                        editor.apply();

                                        break;

                                    case 1:
                                        editor.putString("bloodType", "A");
                                        editor.putString("rhType", "Rh-");
                                        editor.apply();

                                        break;

                                    case 2:
                                        editor.putString("bloodType", "B");
                                        editor.putString("rhType", "Rh+");
                                        editor.apply();

                                        break;

                                    case 3:
                                        editor.putString("bloodType", "B");
                                        editor.putString("rhType", "Rh-");
                                        editor.apply();

                                        break;

                                    case 4:
                                        editor.putString("bloodType", "AB");
                                        editor.putString("rhType", "Rh+");
                                        editor.apply();

                                        break;

                                    case 5:
                                        editor.putString("bloodType", "AB");
                                        editor.putString("rhType", "Rh-");
                                        editor.apply();

                                        break;

                                    case 6:
                                        editor.putString("bloodType", "O");
                                        editor.putString("rhType", "Rh+");
                                        editor.apply();

                                        break;

                                    case 7:
                                        editor.putString("bloodType", "O");
                                        editor.putString("rhType", "Rh-");
                                        editor.apply();

                                        break;
                                }

                                bloodTypeTextView.setText(pref.getString("bloodType", getString(R.string.main_cell_not_register)));
                                bloodTypeDetailTextView.setText(pref.getString("rhType", getString(R.string.main_cell_not_register)));
                            }
                        });

                builder.show();

                return true;
            }
        });

        diseaseLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                final EditText editText = new EditText(MainActivity.this);
                editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                if (pref.getString("diseaseRecord", null) != null) {
                    editText.setText(pref.getString("diseaseRecord", getString(R.string.main_cell_not_register)));
                    editText.setSelection(editText.getText().length());
                }

                builder.setTitle(getString(R.string.main_cell_title_disease))
                        .setView(editText)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                diseaseTextView.setText(editText.getText().toString());
                                editor.putString("diseaseRecord", editText.getText().toString());
                                editor.apply();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                builder.show();

                return true;
            }
        });

        beaconLocationLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                final EditText editText = new EditText(MainActivity.this);
                editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                editText.setText(pref.getString("beacon_spot", getString(R.string.main_cell_body_beacon_location)));
                editText.setSelection(editText.getText().length());

                builder.setTitle(getString(R.string.main_cell_title_beacon_location))
                        .setView(editText)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                beaconLocationTextView.setText(editText.getText().toString());
                                editor.putString("beacon_spot", editText.getText().toString());
                                editor.apply();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                builder.show();

                return true;
            }
        });

        View.OnClickListener onClickListenerCell1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fc.toggle(false);
            }
        };
        View.OnClickListener onClickListenerCell3 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fc3.toggle(false);
            }
        };

        birthLayout.setOnClickListener(onClickListenerCell1);
        genderLayout.setOnClickListener(onClickListenerCell1);
        bloodTypeLayout.setOnClickListener(onClickListenerCell1);
        diseaseLayout.setOnClickListener(onClickListenerCell1);
        beaconLocationLayout.setOnClickListener(onClickListenerCell3);

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

        if (data[0][1].equals("null")) {
            adapter.addItem(Integer.parseInt(data[0][0]), getString(R.string.main_list_empty), Integer.parseInt(data[0][2]));
            countTextView.setText(getString(R.string.main_sliding_up_panel_title_list_empty));
        } else {
            for (int i = 0; i < listItemDataCount; i++) {
                adapter.addItem(Integer.parseInt(data[i][0]), data[i][1], Integer.parseInt(data[i][2]));
            }

            countTextView.setText(String.format(getString(R.string.main_sliding_up_panel_title), listItemDataCount));

        }

        adapter.notifyDataSetChanged();

        countProtectorView.setText(String.format(getString(R.string.main_cell_body_protector), databaseHelper.countProtector()));

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
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

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

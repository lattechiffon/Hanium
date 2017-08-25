package com.lattechiffon.hanium;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class OpenSourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("오픈 소스 라이선스");
        }

    }
}
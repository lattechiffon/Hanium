package com.lattechiffon.hanium;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("개발팀 정보");
        }

    }
}
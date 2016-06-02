package com.example.chris.situaware;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class TrackingChoice extends AppCompatActivity {

    private Button btnOffline, btnLive;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_choice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnOffline = (Button) findViewById(R.id.btnOffline);
        btnLive = (Button) findViewById(R.id.btnLive);

        btnOffline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TrackingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /**
         * Record video button click event
         */
        btnLive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TrackingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}

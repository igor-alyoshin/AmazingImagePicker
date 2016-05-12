package com.example.amazingimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.widget.amazingimagepicker.Picker;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    @Override
    @SuppressWarnings("all")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.start_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picker.Options options = new Picker.Options();
                options.setStatusBarColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
                options.setToolbarColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
                options.setNextTitle("Next");
                options.setToolbarTitle("Choose photo");
                Picker.get().withOptions(options).pickAll(MainActivity.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Picker.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "RESULT_OK: " + data.getData(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "RESULT_CANCELED", Toast.LENGTH_LONG).show();
            }
        }
    }
}

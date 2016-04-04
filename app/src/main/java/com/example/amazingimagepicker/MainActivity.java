package com.example.amazingimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.widget.amazingimagepicker.PickerActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_CODE = 111;
    private MediaScannerConnection mediaScanner;

    @Override
    @SuppressWarnings("all")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.start_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PickerActivity.class);
                intent.putExtra(PickerActivity.EXTRA_TOPBAR_ID, R.layout.topbar);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "RESULT_OK: " + data.getStringExtra(PickerActivity.EXTRA_RESULT), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "RESULT_CANCELED", Toast.LENGTH_LONG).show();
            }
        }
    }
}

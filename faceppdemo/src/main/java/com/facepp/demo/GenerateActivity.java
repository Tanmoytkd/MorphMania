package com.facepp.demo;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class GenerateActivity extends Activity {
    ArrayList<Uri> pictures = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        pictures = (ArrayList<Uri>) getIntent().getSerializableExtra("pictures");

    }
}

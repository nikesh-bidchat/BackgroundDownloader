package com.bidchat.nik.backgrounddownloader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class CancelDownloadActivity extends AppCompatActivity {
    public final String TAG = CancelDownloadActivity.this.getClass().getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        Intent i = getIntent();
        int downloadId = i.getIntExtra(MainActivity.DOWNLOAD_ID, 0);
        Log.d(TAG, "Download ID : " + downloadId);
    }
}

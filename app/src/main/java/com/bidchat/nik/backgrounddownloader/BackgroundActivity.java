package com.bidchat.nik.backgrounddownloader;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class BackgroundActivity extends AppCompatActivity {
    private String TAG = getClass().getCanonicalName();

    public static String DOWNLOAD_CANCEL = "download_cancel";
    public static String DOWNLOAD_ID = "download_id";
    // static String DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=0B_dZD4JMRRVecmNvMHVLWlUxUGM";
    static String DOWNLOAD_URL = "https://bidchatlivecdn156.bidchat.tv/UyT7ij3QQg2trtM-jiX5x/12588-360.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);

        Button buttonStartDownload = (Button) findViewById(R.id.button_start_download);
        buttonStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Onclick Listener");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(DOWNLOAD_URL));
                request.setTitle("Download Name");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                long enqueue = dm.enqueue(request);
                Log.d(TAG, "Returned value -> Download ID : " + enqueue);
                Intent intent = new Intent(BackgroundActivity.this, BackgroundDownloadService.class);
                startService(intent);
            }
        });
    }
}

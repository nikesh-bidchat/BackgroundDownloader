package com.bidchat.nik.backgrounddownloader;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity {
    public static String DOWNLOAD_ID = "download_id";
    public final String TAG = MainActivity.this.getClass().getCanonicalName();

    // static String DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=0B_dZD4JMRRVecmNvMHVLWlUxUGM";
    static String DOWNLOAD_URL = "https://bidchatlivecdn156.bidchat.tv/UyT7ij3QQg2trtM-jiX5x/12588-360.mp4";
    private DownloadManager dm;
    private long enqueue;

    private int downloadId = 67;
    private int notiDownloadId = 67;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonStartDownload = (Button) findViewById(R.id.button_start_download);
        buttonStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(DOWNLOAD_URL));
                enqueue = dm.enqueue(request);

                customeDownloadNotification();

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        boolean downloading = true;

                        while (downloading) {

                            DownloadManager.Query q = new DownloadManager.Query();
                            q.setFilterById(enqueue);
                            Cursor cursor = dm.query(q);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int bytes_downloaded = cursor.getInt(cursor
                                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                final long downLoadId = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                    downloading = false;
                                }

                                Log.d(TAG, "Bytes Downloaded : " + bytes_downloaded);
                                Log.d(TAG, "Bytes Total : " + bytes_total);
                                Log.d(TAG,"Download ID : "+downLoadId);
                                final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                                Log.d(TAG, "Percent Downloaded : " + dl_progress);

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mBuilder.getContentView().setProgressBar(R.id.progress_download, 100, dl_progress, true);
                                        mNotifyManager.notify(notiDownloadId, mBuilder.build());
                                        if (dl_progress > 4) {
                                            Log.d(TAG,"Cancel Download ID : "+downLoadId);
                                            dm.remove(downLoadId);
                                            mNotifyManager.cancel(notiDownloadId);
                                        }
                                    }
                                });
                                cursor.close();
                            }
                        }

                    }
                }).start();
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long tempDownloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    downloadId = (int) tempDownloadId;
                    Log.d(TAG, "Download ID : " + downloadId);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {
                            String uriString = c
                                    .getString(c
                                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            Log.d(TAG, "URI : " + uriString);
                        }
                    }
                    mNotifyManager.cancel(notiDownloadId);
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void customeDownloadNotification() {

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification_layout);
        contentView.setImageViewResource(R.id.image_icon, R.drawable.ic_notification);
        contentView.setTextViewText(R.id.text_title, "Picture Download");
        contentView.setProgressBar(R.id.progress_download, 100, 0, true);
        contentView.setTextViewText(R.id.text_status, "Download in progress");
        contentView.setImageViewResource(R.id.right_icon, R.drawable.ic_cancel);

        mBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_notification).setCustomContentView(contentView);
        mNotifyManager.notify(notiDownloadId, mBuilder.build());

        /*
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        Intent cancelDownloadIntent = new Intent(this, CancelDownloadActivity.class);
        cancelDownloadIntent.putExtra(MainActivity.DOWNLOAD_ID, downloadId);
        mBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_notification)
                .addAction(R.drawable.ic_cancel, "Cancel", PendingIntent.getActivity(getApplicationContext(), 0, cancelDownloadIntent, 0));
        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int incr;
                        // Do the "lengthy" operation 20 times
                        for (incr = 0; incr <= 100; incr += 5) {
                            // Sets the progress indicator to a max value, the
                            // current completion percentage, and "determinate"
                            // state
                            mBuilder.setProgress(100, incr, false);
                            // Displays the progress bar for the first time.
                            mNotifyManager.notify(downloadId, mBuilder.build());
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 5 seconds
                                Thread.sleep(5 * 1000);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "sleep failure");
                            }
                        }
                        // When the loop is finished, updates the notification
                        mBuilder.setContentText("Download complete")
                                // Removes the progress bar
                                .setProgress(0, 0, false);
                        mNotifyManager.notify(downloadId, mBuilder.build());
                    }
                }
                // Starts the thread by calling the run() method in its Runnable
        ).start();
        */
    }
}

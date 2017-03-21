package com.bidchat.nik.backgrounddownloader;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nikesh on 3/17/2017.
 */

public class BackgroundDownloadService extends Service {
    public String TAG = "Call From Service";
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        registerReceiver(receiverDownloadCancel, new IntentFilter(
                BackgroundActivity.DOWNLOAD_CANCEL));
        registerReceiver(receiverDownloadComplete, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    BroadcastReceiver receiverDownloadCancel = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Canceling -> Download ID : " + intent.getExtras().getInt(BackgroundActivity.DOWNLOAD_ID));
            int downloadId = intent.getExtras().getInt(BackgroundActivity.DOWNLOAD_ID);
            mNotifyManager.cancel(downloadId);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.remove(downloadId);
        }
    };

    BroadcastReceiver receiverDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c
                            .getInt(columnIndex)) {
                        String uriString = c
                                .getString(c
                                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        Log.d(TAG, "Download Complete -> Download ID : " + downloadId);
                        updateNotification(context, (int) downloadId, 100, "Download Title", "Download complete");
                        Log.d(TAG, "URI : " + uriString);
                    }
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Started");
        final Context context = this;
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Call through timer");
                updateNotification(context);
            }
        }, 100, 1000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        unregisterReceiver(receiverDownloadCancel);
        unregisterReceiver(receiverDownloadComplete);
    }

    public void updateNotification(final Context context) {
        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Query query = new DownloadManager.Query();
                        Cursor cursor = downloadManager.query(query);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                int bytes_downloaded = cursor.getInt(cursor
                                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                long downloadId = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                                String downloadTitle = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {
                                    final int downloadProgress = (int) Math.ceil((bytes_downloaded * 100) / bytes_total);
                                    Log.d(TAG, "Download Progress : " + downloadProgress);
                                    updateNotification(context, (int) downloadId, downloadProgress, downloadTitle, "Download in progress");
                                    if (downloadProgress > 3) {
                                        downloadManager.remove(downloadId);
                                    }
                                } else
                                    downloadManager.remove(downloadId);
                            }
                            cursor.close();
                        }
                    }
                }
                // Starts the thread by calling the run() method in its Runnable
        ).start();
    }

    public void updateNotification(Context context, int downloadId, int downloadProgress, String downloadTitle, String downloadMessage) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification_layout);
        contentView.setImageViewResource(R.id.image_icon, R.drawable.ic_notification);
        contentView.setTextViewText(R.id.text_title, downloadTitle);
        contentView.setProgressBar(R.id.progress_download, 100, downloadProgress, false);
        contentView.setTextViewText(R.id.text_status_message, downloadMessage);
        contentView.setTextViewText(R.id.text_download_progress, downloadProgress + "%");
        if (downloadProgress == 100) {
            contentView.setViewVisibility(R.id.right_icon, View.GONE);
            mBuilder.setOngoing(false);
        } else
            mBuilder.setOngoing(true);
        contentView.setImageViewResource(R.id.right_icon, R.drawable.ic_cancel);

        mBuilder.setContentTitle(downloadTitle)
                .setContentText(downloadMessage)
                .setSmallIcon(R.drawable.ic_notification).setCustomContentView(contentView);

        Intent cancelIntent = new Intent(BackgroundActivity.DOWNLOAD_CANCEL);
        cancelIntent.putExtra(BackgroundActivity.DOWNLOAD_ID, downloadId);
        cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, downloadId, cancelIntent, 0);

        mBuilder.getContentView().setOnClickPendingIntent(R.id.right_icon,
                pendingIntent);
        mNotifyManager.notify(downloadId, mBuilder.build());
    }
}
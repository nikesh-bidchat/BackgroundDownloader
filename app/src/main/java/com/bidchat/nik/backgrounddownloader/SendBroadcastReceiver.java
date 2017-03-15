package com.bidchat.nik.backgrounddownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by AndroidTest on 3/15/2017.
 */

public class SendBroadcastReceiver extends BroadcastReceiver {
    private final String DEBUG_TAG = getClass().getSimpleName().toString();

    // When the SMS has been sent
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(DEBUG_TAG, "Receiver Download ID : " + action);
    }
}
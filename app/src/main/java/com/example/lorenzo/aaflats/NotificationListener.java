package com.example.lorenzo.aaflats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Lorenzo on 23/04/2016.
 */
public class NotificationListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // If device reboots call service
        Intent startServiceIntent = new Intent(context, MyService.class);
        startServiceIntent.setAction("com.example.lorenzo.aaflats.action.startforeground");
        context.startService(startServiceIntent);
    }

}

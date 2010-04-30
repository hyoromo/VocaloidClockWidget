package jp.hyoromo.vocaloidclockwidget.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClockReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        intent = new Intent(context, ClockService.class);
        intent.setAction(action);
        context.startService(intent);
    }
}
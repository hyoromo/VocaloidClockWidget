package jp.hyoromo.vocaloidclockwidget.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.hyoromo.vocaloidclockwidget.R;
import jp.hyoromo.vocaloidclockwidget.util.PreferencesUtil;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.widget.RemoteViews;

public class ClockService extends Service {
    private static final int NUM_MAX = 10;
    private static Bitmap[] mBtmNum;
    private static ClockProvider mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        String action = intent.getAction();
        if (intent.getIntExtra("count", 0) == 0) {
            unregistIntentFilter(getApplicationContext());
            mBtmNum = null;
            stopSelf();
            return;
        } else {
            registIntentFilter(getApplicationContext(), action);
        }

        initialize(action);

        // 共通情報取得
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName thisWidget = new ComponentName("jp.hyoromo.vocaloidclockwidget",
                "jp.hyoromo.vocaloidclockwidget.widget.ClockProvider");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_clock);

        // 時間取得
        String date = getDate();

        // 時
        remoteViews.setImageViewBitmap(R.id.hour1, mBtmNum[Integer.valueOf(date.substring(0, 1))]);
        remoteViews.setImageViewBitmap(R.id.hour2, mBtmNum[Integer.valueOf(date.substring(1, 2))]);
        // 分
        remoteViews.setImageViewBitmap(R.id.minute1, mBtmNum[Integer.valueOf(date.substring(2, 3))]);
        remoteViews.setImageViewBitmap(R.id.minute2, mBtmNum[Integer.valueOf(date.substring(3, 4))]);
        // 秒
        remoteViews.setImageViewBitmap(R.id.second1, mBtmNum[Integer.valueOf(date.substring(4, 5))]);
        remoteViews.setImageViewBitmap(R.id.second2, mBtmNum[Integer.valueOf(date.substring(5, 6))]);

        manager.updateAppWidget(thisWidget, remoteViews);
    }

    // 初期読み込み
    private void initialize(String action) {
        if (mBtmNum == null || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            String num = PreferencesUtil.getPreferences(getApplicationContext(), "char", "");
            if ("".equals(num)) {
                return;
            }
            String[] arrNum = num.split(",");
            Resources res = getResources();
            int charId;
            int resId;
            mBtmNum = new Bitmap[NUM_MAX];
            for (int i = 0; i < NUM_MAX; i++) {
                charId = Integer.valueOf(arrNum[i]);
                resId = res.getIdentifier("time_" + String.format("%1$02d", charId) + "_" + i, "drawable",
                        "jp.hyoromo.vocaloidclockwidget");
                mBtmNum[i] = BitmapFactory.decodeResource(res, resId);
            }
        }
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");

        return sdf.format(date);
    }

    // 1秒毎に更新してるため不要。
    public static void registIntentFilter(Context context, String action) {
        if (mReceiver == null || action == null) {
            unregistIntentFilter(context);

            mReceiver = new ClockProvider();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            context.registerReceiver(mReceiver, filter);
        }
    }

    private static void unregistIntentFilter(Context context) {
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
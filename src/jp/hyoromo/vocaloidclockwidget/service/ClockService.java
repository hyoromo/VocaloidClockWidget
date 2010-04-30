package jp.hyoromo.vocaloidclockwidget.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.hyoromo.vocaloidclockwidget.R;
import jp.hyoromo.vocaloidclockwidget.util.Construct;
import jp.hyoromo.vocaloidclockwidget.util.PreferencesUtil;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;

public class ClockService extends Service {
    private static final int NUM_MAX = 10;
    private static final long INTERVAL = 1000;
    private static ClockReceiver mReceiver;
    private static Bitmap[] mBtmNum;
    private static boolean mIsUpdate = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Context context = getApplicationContext();
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            mIsUpdate = false;
            return;
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            mIsUpdate = true;
        } else if (!mIsUpdate) {
            return;
        }

        // Manager情報取得
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName("jp.hyoromo.vocaloidclockwidget",
                "jp.hyoromo.vocaloidclockwidget.widget.ClockProvider");

        // 初回起動時にチェック
        Uri uri = intent.getData();
        int appWidgetId = 0;
        if (uri != null) {
            appWidgetId = (int) ContentUris.parseId(uri);
            if (manager.getAppWidgetInfo(appWidgetId) == null) {
                // AppWidgetIdの配置数を減算設定
                PreferencesUtil.countUpPreferences(context, Construct.APP_WIDGET_ID_COUNT, 0, -1);
                return;
            }
        }

        initialize(action);

        // 共通情報取得
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_clock);

        // 更新対象のAppWidgetを取得(AppWidgetを初めて設置した時 or 二回目以降の初回処理でない場合)
        int count = PreferencesUtil.getPreferences(context, Construct.APP_WIDGET_ID_COUNT, 0);
        if (count != 0) {
            if (count == 1 || (count > 1 && appWidgetId == 0)) {
                setAlarm(context);
            }
        } else {
            unregistIntentFilter(context);
            return;
        }

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
        if (mBtmNum == null || action == null) {
            Context context = getApplicationContext();
            if (mReceiver != null) {
                unregistIntentFilter(context);
            }
            registIntentFilter(context);
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

    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, ClockReceiver.class);
        alarmIntent.setAction(Construct.ACTION_ALARM);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis() + 1;
        long oneHourAfter = now + INTERVAL - now % (INTERVAL);
        am.set(AlarmManager.RTC, oneHourAfter, operation);
    }

    // 1秒毎に更新してるため不要。
    public static void registIntentFilter(Context context) {
        mReceiver = new ClockReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        context.registerReceiver(mReceiver, filter);
    }

    private static void unregistIntentFilter(Context context) {
        if (mReceiver == null) {
            mReceiver = new ClockReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        }
        context.unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");

        return sdf.format(date);
    }
}
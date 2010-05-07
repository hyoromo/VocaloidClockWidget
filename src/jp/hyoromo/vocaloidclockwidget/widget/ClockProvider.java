package jp.hyoromo.vocaloidclockwidget.widget;

import jp.hyoromo.vocaloidclockwidget.util.Construct;
import jp.hyoromo.vocaloidclockwidget.util.PreferencesUtil;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ClockProvider extends AppWidgetProvider {
    private static final long INTERVAL = 1000;
    private static boolean mIsUpdate = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action) || Construct.ACTION_ALARM.equals(action)
                || Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_SCREEN_OFF.equals(action)) {
            updateClock(context, intent.getAction(), intent.getData());
        }
        super.onReceive(context, intent);
    }

    /**
     * 時計更新
     */
    public static void updateClock(Context context, String action, Uri uri) {
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

        // 初回起動時にチェック
        int appWidgetId = 0;
        if (uri != null) {
            appWidgetId = (int) ContentUris.parseId(uri);
            if (manager.getAppWidgetInfo(appWidgetId) == null) {
                // AppWidgetIdの配置数を減算設定
                PreferencesUtil.countUpPreferences(context, Construct.APP_WIDGET_ID_COUNT, 0, -1);
                return;
            }
        }

        // 更新対象のAppWidgetを取得(AppWidgetを初めて設置した時 or 二回目以降の初回処理でない場合)
        int count = PreferencesUtil.getPreferences(context, Construct.APP_WIDGET_ID_COUNT, 0);
        if (count != 0) {
            if (count == 1 || (count > 1 && appWidgetId == 0)) {
                setAlarm(context);
            }
        }

        // 時計の更新はServiceで行う
        Intent intent = new Intent(context, ClockService.class);
        intent.setAction(action);
        intent.putExtra("count", count);
        context.startService(intent);
    }

    /**
     * アラームは1秒後に設定
     */
    private static void setAlarm(Context context) {
        Intent intent = new Intent(context, ClockProvider.class);
        intent.setAction(Construct.ACTION_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis() + 1;
        long oneHourAfter = now + INTERVAL - now % (INTERVAL);
        alarmManager.set(AlarmManager.RTC_WAKEUP, oneHourAfter, pendingIntent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // AppWidgetIdの配置数を減算設定
        PreferencesUtil.countUpPreferences(context, Construct.APP_WIDGET_ID_COUNT, 0, -1);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        // 一応削除しておく
        PreferencesUtil.removePreferences(context, "char");
    }
}
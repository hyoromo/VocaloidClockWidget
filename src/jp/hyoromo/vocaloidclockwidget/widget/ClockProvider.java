package jp.hyoromo.vocaloidclockwidget.widget;

import jp.hyoromo.vocaloidclockwidget.service.ClockService;
import jp.hyoromo.vocaloidclockwidget.util.Construct;
import jp.hyoromo.vocaloidclockwidget.util.PreferencesUtil;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ClockProvider extends AppWidgetProvider {
    private static boolean mIsUpdate = true;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        mIsUpdate = false;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if (appWidgetIds.length > 0 && mIsUpdate) {
            Intent intent = new Intent(context, ClockService.class);
            context.startService(intent);
        }
        mIsUpdate = false;
    }

    /**
     * appWidget設置処理
     */
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // AppWidgetIdの配置数を加算設定
        PreferencesUtil.countUpPreferences(context, Construct.APP_WIDGET_ID_COUNT, 0, 1);

        Intent intent = new Intent(context, ClockService.class);
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://jp.hyoromo.vocaloidclockwidget"), appWidgetId);
        intent.setData(uri);
        context.startService(intent);
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
        PreferencesUtil.removePreferences(context, "char");
    }
}
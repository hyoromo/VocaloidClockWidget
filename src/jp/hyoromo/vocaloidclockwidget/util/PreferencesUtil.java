package jp.hyoromo.vocaloidclockwidget.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {
    private static final String TAG = "VocaloidClockWidget";

    /**
     * プリファレンス情報取得(string)
     */
    public static final String getPreferences(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return settings.getString(key, value);
    }

    /**
     * プリファレンス情報取得(int)
     */
    public static final int getPreferences(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return settings.getInt(key, value);
    }

    /**
     * プリファレンス情報取得(boolean)
     */
    public static final boolean getPreferences(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return settings.getBoolean(key, value);
    }

    /**
     * プリファレンス情報設定(string)
     */
    public static final void setPreferences(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * プリファレンス情報設定(boolean)
     */
    public static final void setPreferences(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * プリファレンス情報削除(string)
     */
    public static final void removePreferences(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * プリファレンス加算情報設定(int)
     */
    public static final void countUpPreferences(Context context, String key, int def, int value) {
        SharedPreferences settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        value += settings.getInt(key, def);

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
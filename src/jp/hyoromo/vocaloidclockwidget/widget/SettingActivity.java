package jp.hyoromo.vocaloidclockwidget.widget;

import jp.hyoromo.vocaloidclockwidget.ManualActivity;
import jp.hyoromo.vocaloidclockwidget.R;
import jp.hyoromo.vocaloidclockwidget.util.Construct;
import jp.hyoromo.vocaloidclockwidget.util.PreferencesUtil;
import jp.hyoromo.vocaloidclockwidget.widget.data.ListData;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingActivity extends Activity implements OnItemClickListener {
    private static final int REQUEST_SET_MANUAL = 1;
    private static final int RESULT_EXIT = 9;
    private static final int PICT_MAX = 13;
    private static final int NUM_MAX = 10;
    private static final int[] LIST_NUM = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private static ListAdapter mAdapter;
    private static int[] mPictIndex = new int[NUM_MAX];
    private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static Drawable[] mPict;
    private static String[] mCharList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        // 初期化
        mPict = new Drawable[PICT_MAX];
        mCharList = new String[PICT_MAX];

        // appWidgetId取得
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // AdapterをListActivityに設定
        mAdapter = new ListAdapter(getApplicationContext());
        ListView list = (ListView) findViewById(R.id.setting_list);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);

        Resources res = getResources();
        int resId;
        for (int i = 0; i < PICT_MAX; i++) {
            // キャラ名を読み込む
            resId = res.getIdentifier("setting_dialog_char_" + i, "string", "jp.hyoromo.vocaloidclockwidget");
            mCharList[i] = res.getString(resId);
            // 絵を読み込む
            resId = res.getIdentifier("icon_" + String.format("%1$02d", i), "drawable", "jp.hyoromo.vocaloidclockwidget");
            mPict[i] = res.getDrawable(resId);
        }

        for (int i = 0; i < NUM_MAX; i++) {
            mPictIndex[i] = i;
        }
    }

    /**
     * ListViewのアダプター
     */
    public class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // 1行ごとのビューを生成する
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ListData data;

            if (convertView == null || convertView.getTag() == null) {
                view = mInflater.inflate(R.layout.setting_list_row, null);
                data = new ListData();
                data.txt = (TextView) view.findViewById(R.id.setting_list_row_txt);
                data.img = (ImageView) view.findViewById(R.id.setting_list_row_img);
            } else {
                data = (ListData) convertView.getTag();
            }

            // 現在参照しているリストの位置からItemを取得する
            data.txt.setText(Integer.toString(position));
            data.img.setBackgroundDrawable(mPict[mPictIndex[position]]);

            return view;
        }

        @Override
        public int getCount() {
            return LIST_NUM.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    /**
     * 選択行がクリックされたら呼ばれる
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int finalPosition = position;
        new AlertDialog.Builder(this)
        .setTitle(R.string.setting_dialog_title)
        .setItems(mCharList, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int index) {
                        mPictIndex[finalPosition] = index;
                        mAdapter.notifyDataSetChanged();
            }
        })
        .show();
    }

    /**
     * ウィジェット配置
     */
    public void onClickOk(View v) {
        // データ保存
        String save = "";
        for (int i = 0; i < NUM_MAX; i++) {
            if (i != 0) {
                save += ",";
            }
            save += mPictIndex[i];
        }

        // キャラ情報を保存
        PreferencesUtil.setPreferences(getApplicationContext(), "char", save);
        // AppWidget数をインクリメント
        PreferencesUtil.countUpPreferences(this, Construct.APP_WIDGET_ID_COUNT, 0, 1);

        // AppWidget配置
        Intent intent = new Intent(SettingActivity.this, ClockProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        //        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
        //        intent.setAction(Construct.ACTION_ALARM);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        sendBroadcast(intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * menuボタン作成
     */
    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting, menu);
        return true;
    }

    /**
     * meny押下時のイベント
     */
    @Override
    public final boolean onOptionsItemSelected(MenuItem items) {
        switch (items.getItemId()) {
        case R.id.menu_setting_manual:
            Intent intent = new Intent(this, ManualActivity.class);
            startActivityForResult(intent, REQUEST_SET_MANUAL);
            return true;
        case R.id.menu_setting_exit:
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_EXIT) {
            finish();
        } else if (resultCode == RESULT_CANCELED) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCharList = null;
        mPict = null;
        mAdapter = null;
    }
}
package com.android.wukong.viewer.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.wukong.viewer.R;
import com.android.wukong.viewer.adapter.WuKongListAdapter;
import com.android.wukong.viewer.adapter.WuKongListJsonAdapter;
import com.android.wukong.viewer.util.WuKongReflect;
import com.android.wukong.viewer.WuKongApi;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;

public class WuKongListActivity extends AppCompatActivity {

    protected ListView listView;

    protected WuKongListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout id
        int layout_resource_id = getIntent().getIntExtra("layoutId", -1);
        if (layout_resource_id == -1) {
            Integer defaultId = getDefaultLayoutId();
            if (defaultId != null) {
                layout_resource_id = defaultId;
            }
        }
        if (layout_resource_id == -1) {
            layout_resource_id = R.layout.wukong_activity_base_list;
        }
        setContentView(layout_resource_id);

        // Toolbar
        String title = getIntent().getStringExtra("title");
        if (title == null) {
            title = "__To_Be_Set__";
        }
        Toolbar toolbar = findViewById(R.id.kWuKong_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }

        // list view and its adapter
        listView = findViewById(R.id.kWuKong_list_view);

        String adapterClazzName = getIntent().getStringExtra("AdapterClazzName");
        if (adapterClazzName == null || adapterClazzName.isEmpty()) {
            adapterClazzName = this.getClass().getName().replace("activity", "adapter").replace("Activity", "Adapter");
        }
        listAdapter = getBaseListAdapter(adapterClazzName, this);
        if (listAdapter == null) {
            listAdapter = new WuKongListAdapter();
        }
        listAdapter.setOwnerActivity(this);
        listAdapter.setOwnerListView(listView);
        if (listAdapter.inflater == null) {
            listAdapter.inflater = LayoutInflater.from(this);
        }
        listView.setOnItemClickListener(listAdapter);
        listView.setOnItemLongClickListener(listAdapter);

        // 设置向左右滑动事件 -----------------
        // WuKongHelper.setupSwipeEvents(this, listView);

        // TODO ...
//        JSONObject config = Config.getRenderJsonOf(this).optJSONObject("config_of_list_view");
//        WuKongListJsonAdapter.setViewsValuesAccordingJson(config, listView);

        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            event.onCreate(this);
        }

        listView.setAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            event.onResume(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            event.onStop(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            event.onDestroy(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            Boolean result = event.onCreateMenu(this, menu);
            if (result != null) {
                return result;
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            Boolean result = event.onMenuItemSelected(this, item);
            if (result != null) {
                return result;
            }
        }

        if (item.getItemId() == android.R.id.home) {    // back button
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        WuKongListAdapter.Event event = getEvent();
        if (event != null) {
            Boolean result = event.onBackPressed(this);
            if (result != null && result) {
                return;
            }
        }
        super.onBackPressed();
    }

    public ListView getListView() {
        return listView;
    }

    public WuKongListAdapter getListAdapter() {
        return listAdapter;
    }

    public WuKongListAdapter.Event getEvent() {
        return getListAdapter().getEvent();
    }

    // Protected Methods
    protected Integer getDefaultLayoutId() {
        // 获取默认的 layout resource id 的值
        // 把名字 UserAccountListActivity 转成 activity_user_account_list 来获取 R.layout.activity_user_account_list
        String activityName = this.getClass().getSimpleName();
        String temp = "activity" + activityName.replace("Activity", "");
        // 遇到大写字母转成小写，并在其前面加下划线
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < temp.length(); i++) {
            char ch = temp.charAt(i);
            if (Character.isUpperCase(ch)) {
                builder.append("_");
            }
            builder.append(String.valueOf(ch).toLowerCase());
        }
        // 反射获取 layout id 的值
        String layoutName = builder.toString();
        return (Integer) WuKongReflect.getFieldValue(R.layout.class, layoutName);  // TODO ...
    }

    // Override by subclass
    public void swipeRightEvent() {
    }

    // Override by subclass
    public void swipeLeftEvent() {
    }

    /**
     * Private Methods
     */
    private WuKongListAdapter getBaseListAdapter(String adapterClazzName, Context mContext) {
        WuKongListAdapter adapter = null;
        try {
            Class adapterClazz = Class.forName(adapterClazzName);
            // 尝试有参数的构造函数
            try {
                Constructor constructor = adapterClazz.getConstructor(Context.class);
                adapter = (WuKongListAdapter) constructor.newInstance(mContext);
            } catch (Exception e) {
                // NoSuchMethodException ... regard as nothing ...
                // e.printStackTrace();
            }
            // 尝试无参数的构造函数
            if (adapter == null) {
                adapter = (WuKongListAdapter) adapterClazz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return adapter;
    }

    /**
     * Static Methods
     */

    public static void startNewActivityInstance(String title,
                                                String assetsRenderJson,
                                                Map<String, Serializable> parameters,
                                                WuKongListAdapter.Event event) {
        startNewActivityInstance(title,
                assetsRenderJson,
                parameters == null ? null : new JSONObject(parameters),
                event);
    }

    public static void startNewActivityInstance(String title,
                                                String assetsRenderJson,
                                                JSONObject parameters,
                                                WuKongListAdapter.Event event) {
        startNewActivityInstance(title,
                assetsRenderJson,
                parameters,
                WuKongListActivity.class,
                assetsRenderJson != null ? WuKongListJsonAdapter.class : WuKongListAdapter.class,
                event
        );
    }

    public static void startNewActivityInstance(String title,
                                                String assetsRenderJson,
                                                JSONObject parameters,
                                                Class<? extends WuKongListActivity> activityClazz,
                                                Class<? extends WuKongListAdapter> adapterClazz,
                                                WuKongListAdapter.Event event) {
        if (activityClazz == null) {
            activityClazz = WuKongListActivity.class;
        }
        if (adapterClazz == null) {
            adapterClazz = assetsRenderJson != null ? WuKongListJsonAdapter.class : WuKongListAdapter.class;
        }
        Activity activity = WuKongApi.getTopActivity();
        if (activity == null) return;
        Intent intent = new Intent(activity, activityClazz);
        if (parameters != null) {
            Iterator<String> it = parameters.keys();
            while (it.hasNext()) {
                String key = it.next();
                Object value = parameters.opt(key);
                if (value == JSONObject.NULL) value = "";
                intent.putExtra(key, (Serializable) value);
            }
        }

        intent.putExtra("title", title);
        if (assetsRenderJson != null) {
            intent.putExtra("JsonNameOfListRender", assetsRenderJson);
        }
        intent.putExtra("AdapterClazzName", adapterClazz.getName());
        if (event != null) WuKongListAdapter.pushEvent(event);
        activity.startActivity(intent);
    }

}


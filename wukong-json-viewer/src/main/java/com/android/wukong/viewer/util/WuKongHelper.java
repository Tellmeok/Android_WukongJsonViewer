package com.android.wukong.viewer.util;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.wukong.viewer.R;
import com.android.wukong.viewer.WuKongApi;
import com.android.wukong.viewer.activity.WuKongListActivity;
import com.android.wukong.viewer.adapter.WuKongListAdapter;
import com.android.wukong.viewer.adapter.WuKongListJsonAdapter;
import com.android.wukong.viewer.util.json.WuKongJSONObject;
import com.android.wukong.viewer.util.json.WuKongJSONUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WuKongHelper {

    public static void dispatchEvent(WuKongListAdapter adapter, String title, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        String[] values = value.split("\\|");
        String className = values[0];
        String methodName = values[1];

        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (aClass == null) {
            return;
        }

        try {
            Method method = WuKongReflect.searchMethod(aClass, methodName, new Class[]{String.class});
            if (method != null) {
                method.invoke(aClass, new Object[]{title});
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Method method = WuKongReflect.searchMethod(aClass, methodName, new Class[]{});
            if (method != null) {
                method.invoke(aClass, new Object[]{});
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClickItemTitle(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = getClickItemTextView(parent, view, position, id);
        if (textView == null) {
            return null;
        }
        String title = textView.getText().toString();
        return title;
    }

    public static TextView getClickItemTextView(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = view.findViewById(R.id.wukongItemLeftTextView);
        if (textView == null) {
            textView = view.findViewById(R.id.wukongItemCenterTextView);
        }
        return textView;
    }

    /**
     * Swipe Events
     */
    public static void setupSwipeEvents(final WuKongListActivity activity, ListView listView) {
        final float[] x1 = new float[1];
        final float[] y1 = new float[1];
        final float[] x2 = new float[1];
        final float[] y2 = new float[1];
        final long[] startTimes = new long[]{0};
        final long[] stopTimes = new long[]{0};
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        startTimes[0] = System.currentTimeMillis();

                        x1[0] = 0;
                        y1[0] = 0;
                        x2[0] = 0;
                        y2[0] = 0;

                        x1[0] = event.getX();
                        y1[0] = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        x2[0] = event.getX();
                        y2[0] = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        stopTimes[0] = System.currentTimeMillis();

                        int swipeDistance = 450;
                        if (stopTimes[0] - startTimes[0] < 700) {
                            if (x1[0] != 0 && x2[0] != 0 && x2[0] - x1[0] > 0 && (Math.abs(x2[0] - x1[0]) > swipeDistance)) {
                                //向右滑动
                                activity.swipeRightEvent();
                            } else if (x1[0] != 0 && x2[0] != 0 && x2[0] - x1[0] < 0 && (Math.abs(x2[0] - x1[0]) > swipeDistance)) {
                                //向左滑動
                                activity.swipeLeftEvent();
                            }
                        }

                        x1[0] = 0;
                        y1[0] = 0;
                        x2[0] = 0;
                        y2[0] = 0;
                        break;
                }

                return false;
            }
        });
    }

    public static View getToolBarView(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        Object object = WuKongReflect.getFieldValue(actionBar, "mDecorToolbar");     // ToolbarWidgetWrapper
        View mToolbar = (View) WuKongReflect.getFieldValue(object, "mToolbar");      // Toolbar
        return mToolbar;
    }

    public static EditText createEditText(Context mContext, String text, int textSize, int textColor, int bgColor, TextWatcher textWatcher) {
        EditText searchEditText = new EditText(mContext);
        searchEditText.setText(text);
        searchEditText.setTextSize(textSize);
        searchEditText.setGravity(Gravity.CENTER);
        searchEditText.setFocusable(true);
        searchEditText.setFocusableInTouchMode(true);
        searchEditText.setTextColor(textColor);
        searchEditText.setBackgroundColor(bgColor);
        searchEditText.addTextChangedListener(textWatcher);
        return searchEditText;
    }

    public static void showSearchEditTextToActivity(AppCompatActivity activity, TextWatcher textWatcher) {
        try {
            ViewGroup parent = (ViewGroup) WuKongHelper.getToolBarView(activity).getParent();
            EditText searchEditText = (EditText) parent.getTag();
            if (searchEditText == null) {
                int colorGreen = Color.GREEN;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colorGreen = activity.getResources().getColor(R.color.wukong_green, null);
                }
                searchEditText = createEditText(activity, "", 18, colorGreen, Color.WHITE, textWatcher);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.weight = 1;
                searchEditText.setLayoutParams(layoutParams);

                parent.setTag(searchEditText);
            }

            if (searchEditText.getParent() == null) {
                searchEditText.requestFocus();
                parent.addView(searchEditText);
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).
                        showSoftInput(searchEditText, 0);
            } else {
                searchEditText.setText("");
                parent.removeView(searchEditText);
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static interface ListMapElementDatasourceAccessor {
        public Object getDatasourceList(WuKongListActivity activity);

        public void setDatasourceList(WuKongListActivity activity, Object object);
    }

    public static void showSearchEditTextToAccountsActivity(final WuKongListActivity activity, final ListMapElementDatasourceAccessor datasourceAccessor) {

        WuKongHelper.showSearchEditTextToActivity(activity, new TextWatcher() {

            // save original data first
            private ArrayList<Map<String, Object>> dataList = (ArrayList<Map<String, Object>>) datasourceAccessor.getDatasourceList(activity);

            private String[] searchKeysInMap = new String[]{"kAccountId", "kNickName", "kBindPhone", "kUserName", "kDeviceManufacture", "kLastActiveTime"};

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();

                ArrayList<Map<String, Object>> resultsList = dataList;
                if (!string.isEmpty()) {
                    resultsList = new ArrayList();
                    for (int i = 0; i < dataList.size(); i++) {
                        Map<String, Object> profileMap = dataList.get(i);

                        for (String key : searchKeysInMap) {
                            String value = (String) profileMap.get(key);
                            if (value != null && value.contains(string)) {
                                resultsList.add(profileMap);
                                break;
                            }
                        }

                    }
                }

                datasourceAccessor.setDatasourceList(activity, resultsList);
                activity.getListAdapter().notifyDataSetChanged();
            }
        });

    }

    /**
     *
     */
    public static abstract class ClickTitleEvent {
        public void click(View view, int position, String title) {
        }

        public boolean longClick(View view, int position, String title) {
            return false;
        }
    }

    public static void startNewActivityInstanceUseClickTitleEvent(String title, String assetsRenderJson, JSONObject parameters, final ClickTitleEvent clickTitleEvent) {
        WuKongListAdapter.Event event = null;
        if (clickTitleEvent != null) {
            event = new WuKongListAdapter.Event() {
                @Override
                public void onCellClicked(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
                    String title = WuKongHelper.getClickItemTitle(parent, view, position, id);
                    if (title == null || title.isEmpty()) {
                        return;
                    }
                    clickTitleEvent.click(view, position, title);
                }

                @Override
                public Boolean onCellLongClicked(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
                    String title = WuKongHelper.getClickItemTitle(parent, view, position, id);
                    if (title == null || title.isEmpty()) {
                        return false;
                    }
                    return clickTitleEvent.longClick(view, position, title);
                }
            };
        }
        WuKongListActivity.startNewActivityInstance(title, assetsRenderJson, parameters, event);
    }

    /**
     * Create List View
     */
    public static ListView createListViewFromJson(String renderJsonPathInAssets, WuKongListAdapter.Event event) {
        Context mContext = WuKongApi.getAppContext();

        WuKongListJsonAdapter listAdapter = new WuKongListJsonAdapter(mContext);
        listAdapter.setEvent(event);

        JSONObject json = WuKongAssetser.getAssetsAsJsonObject("render/" + renderJsonPathInAssets);
        if (json != null) {
            listAdapter.initializeRenderJson(json);
        }

        ListView listView = new ListView(mContext);
        listView.setDividerHeight(0);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(listAdapter);
        listView.setOnItemLongClickListener(listAdapter);
        listView.setAdapter(listAdapter);

        return listView;
    }

    public static ListView createListView(final List<String> values) {
        final Context mContext = WuKongApi.getAppContext();

        // create list view
        ListView listView = new ListView(mContext);
        listView.setFastScrollEnabled(false);
        listView.setDividerHeight(1);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));

        // set up list view data source & events
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return values.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.wukong_item_center_text, null);
                }
                String text = values.get(position);
                ((TextView) convertView.findViewById(R.id.wukongItemCenterTextView)).setText(text);
                return convertView;
            }
        });

        return listView;
    }

    /**
     * Abstract Columns List Activity
     */
    public static abstract class CommonColumnsListHandler {
        public Object[] repositories = null;

        public void onActivityCreate(WuKongListActivity activity) {
        }

        public void onActivityCreateOptionsMenu(WuKongListActivity activity, Menu menu) {
        }

        public void onActivityOptionsMenuSelected(WuKongListActivity activity, MenuItem item) {
        }

        public int onGetDatasourceCount(WuKongListAdapter adapter) {
            return 0;
        }

        public void onFillDatasourceView(WuKongListAdapter adapter, int position, LinearLayout linearLayout) {
        }

        public void onHeaderClick(WuKongListActivity activity, View view) {
        }

        public void onRowClick(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
        }

        public boolean onRowLongClick(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
            return true;
        }
    }

    public static void startCommonColumnsListActivityInstance(String title, final String[] columnsTitles, final CommonColumnsListHandler listHandler) {
        startCommonColumnsListActivityInstance(title, null, columnsTitles, listHandler);
    }

    public static void startCommonColumnsListActivityInstance(String title, Map args, final String[] columnsTitles, final CommonColumnsListHandler listHandler) {
        if (args == null) {
            args = new HashMap();
        }
        if (!args.containsKey("layoutId")) {
            args.put("layoutId", R.layout.wukong_activity_base_list_header);
        }
        WuKongListActivity.startNewActivityInstance(title, "whatever" + title, new JSONObject(args), new WuKongListAdapter.Event() {

            @Override
            public void onCreate(final WuKongListActivity activity) {
                super.onCreate(activity);

                int colorGreen = Color.GREEN;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colorGreen = activity.getResources().getColor(R.color.wukong_green, null);
                }
                activity.getListView().setDivider(new ColorDrawable(colorGreen));
                activity.getListView().setDividerHeight(28);

                LinearLayout headersLinearLayout = activity.findViewById(R.id.kWuKong_list_header_layout);
                for (int i = 0; i < columnsTitles.length; i++) {
                    String columnTitle = columnsTitles[i];

                    TextView columnTextView = new TextView(activity);
                    columnTextView.setText(columnTitle);
                    columnTextView.setTextSize(12);
                    columnTextView.setGravity(Gravity.CENTER);
                    columnTextView.setTextColor(colorGreen);
                    columnTextView.setBackgroundColor(Color.TRANSPARENT);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.weight = 1;
                    columnTextView.setLayoutParams(layoutParams);

                    headersLinearLayout.addView(columnTextView);
                }

                headersLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listHandler.onHeaderClick(activity, v);
                    }
                });

                listHandler.onActivityCreate(activity);
            }

            @Override
            public Boolean onCreateMenu(WuKongListActivity activity, Menu menu) {
                listHandler.onActivityCreateOptionsMenu(activity, menu);
                return null;
            }

            @Override
            public Boolean onMenuItemSelected(WuKongListActivity activity, MenuItem item) {
                listHandler.onActivityOptionsMenuSelected(activity, item);
                return null;
            }

            @Override
            public int onGetCount(WuKongListAdapter adapter) {
                return listHandler.onGetDatasourceCount(adapter);
            }

            @Override
            public View onGetView(WuKongListAdapter adapter, int position, View convertView, ViewGroup parent) {
                boolean isNeedSerialNumber = adapter.getOwnerActivity().getIntent().getBooleanExtra("is_need_serial_number", true);

                if (convertView == null) {
                    convertView = adapter.inflater.inflate(R.layout.wukong_item_base_container, null);

                    int itemH = 120;

                    // --------------- 序号 ---------------
                    if (isNeedSerialNumber) {
                        TextView positionTextView = new TextView(adapter.getOwnerActivity());
                        positionTextView.setMaxLines(1);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            positionTextView.setAutoSizeTextTypeUniformWithConfiguration(
                                    2, 10, 1, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                        }
                        positionTextView.setGravity(Gravity.CENTER);
                        int colorGreen = Color.GREEN;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            colorGreen = adapter.getOwnerActivity().getResources().getColor(R.color.wukong_green, null);
                        }
                        positionTextView.setTextColor(colorGreen);
                        positionTextView.setBackgroundColor(Color.WHITE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            positionTextView.setZ(1.0f);
                        }
                        ((ViewGroup) convertView).addView(positionTextView, 0);

                        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(20, itemH);
                        p.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
                        positionTextView.setLayoutParams(p);
                    }
                    // --------------- 序号 ---------------

                    LinearLayout linearLayout = convertView.findViewById(R.id.kWuKong_item_container_layout);
                    linearLayout.setBackgroundResource(R.drawable.wukong_item_common_back_shape_rectangle);
                    ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
                    params.height = itemH;
                    linearLayout.setLayoutParams(params);

                    for (int i = 0; i < columnsTitles.length; i++) {
                        String columnTitle = columnsTitles[i];

                        TextView columnTextView = new TextView(adapter.getOwnerActivity());
                        columnTextView.setText(columnTitle);
                        columnTextView.setTextSize(12);
                        columnTextView.setGravity(Gravity.CENTER);
                        columnTextView.setTextColor(Color.GRAY);
                        columnTextView.setBackgroundColor(Color.TRANSPARENT);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        layoutParams.weight = 1;
                        columnTextView.setLayoutParams(layoutParams);

                        linearLayout.addView(columnTextView);
                    }
                }

                // --------------- 序号 ---------------
                if (isNeedSerialNumber) {
                    TextView positionTextView = (TextView) ((ViewGroup) convertView).getChildAt(0);
                    positionTextView.setText(String.valueOf(position));
                }
                // --------------- 序号 ---------------

                LinearLayout linearLayout = convertView.findViewById(R.id.kWuKong_item_container_layout);
                listHandler.onFillDatasourceView(adapter, position, linearLayout);

                return super.onGetView(adapter, position, convertView, parent);
            }

            @Override
            public void onCellClicked(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
                listHandler.onRowClick(adapter, parent, view, position, id);
            }

            @Override
            public Boolean onCellLongClicked(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
                return listHandler.onRowLongClick(adapter, parent, view, position, id);
            }
        });
    }

    /**
     * Behaviour or Content for Column Title
     */
    public static void showTimePickerForTagTextView(TextView textView) {
        String title = textView.getText().toString();
        String tag = (String) textView.getTag();
        showTimePickerForPreferenceTag(tag);
    }

    public static void showTimePickerForPreferenceTag(String tag) {
        final String key = WuKongListJsonAdapter.getSharedPreferenceKeyFromTag(tag);
        final String file = WuKongListJsonAdapter.getSharedPreferenceFileFromTag(tag);

        String defValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE);
        String hourMinute = (String) WuKongPreferences.getWithFile(file, key, defValue);
        String[] strings = hourMinute.split(":");
        int hourOfDay = Integer.parseInt(strings[0]);
        int minute = Integer.parseInt(strings[1]);
        TimePickerDialog timePickerDialog = new TimePickerDialog(WuKongApi.getTopActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                new WuKongPreferences(file).setValue(key, hourOfDay + ":" + (minute < 10 ? "0" + minute : minute));
                Activity activity = WuKongApi.getTopActivity();
                if (activity instanceof WuKongListActivity) {
                    WuKongListAdapter adapter = ((WuKongListActivity) activity).getListAdapter();
                    adapter.notifyDataSetChanged();
                }
            }
        }, hourOfDay, minute, true);
        timePickerDialog.show();
    }

    public static void setTitle2TimePickerDialog(TimePickerDialog timePickerDialog, String title) {
        TextView titleTextView = new TextView(timePickerDialog.getContext());
        titleTextView.setText(title);
        titleTextView.setTextSize(16);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        titleTextView.setBackgroundColor(Color.TRANSPARENT);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        timePickerDialog.getWindow().addContentView(titleTextView, params);
    }


}

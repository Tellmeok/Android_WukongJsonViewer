package com.android.wukong.viewer.util.json;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.wukong.viewer.R;
import com.android.wukong.viewer.WuKongApi;
import com.android.wukong.viewer.activity.WuKongListActivity;
import com.android.wukong.viewer.activity.WuKongTextActivity;
import com.android.wukong.viewer.adapter.WuKongListAdapter;
import com.android.wukong.viewer.util.WuKongAction;
import com.android.wukong.viewer.util.WuKongAlert;
import com.android.wukong.viewer.util.WuKongReflect;
import com.android.wukong.viewer.util.WuKongHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WuKongJSONViewer {

    public static abstract class JsonEvent {
        // NOTE: currentJsonObj may be JSONObject or JSONArray

        public void onKeyChanged(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj, String oldKey, String newKey) {
        }

        public void onSaved(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj, Object oldValue, Object newValue) {
        }

        public void onAdded(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj) {
        }

        public void onDeleted(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj) {
        }

        // Override following method with return true, for customize your own actions methods
        public boolean onBackEvent(WuKongJSONViewer viewer, String keyPath, Object currentJsonObj) {
            return false;
        }

        public boolean onCellTouch(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj) {
            return false;
        }

        public boolean onCellLongTouch(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj) {
            return false;
        }

        public View onCellGetView(WuKongJSONViewer viewer, String keyPath, int position, View convertView, ViewGroup parent) {
            return null;
        }

    }

    public static JsonEvent DEFAULT_EVENT = new JsonEvent() {
        @Override
        public void onKeyChanged(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj, String oldKey, String newKey) {
            if (viewer.getJsonPath() != null) {
                WuKongJSONUtil.writeTextToFile(viewer.getJson().toString(), viewer.getJsonPath());
            }
        }

        @Override
        public void onSaved(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj, Object oldValue, Object newValue) {
            if (viewer.getJsonPath() != null) {
                WuKongJSONUtil.writeTextToFile(viewer.getJson().toString(), viewer.getJsonPath());
            }
        }

        @Override
        public void onAdded(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj) {
            if (viewer.getJsonPath() != null) {
                WuKongJSONUtil.writeTextToFile(viewer.getJson().toString(), viewer.getJsonPath());
            }
        }

        @Override
        public void onDeleted(WuKongJSONViewer viewer, String keyPath, int position, Object currentJsonObj) {
            if (viewer.getJsonPath() != null) {
                WuKongJSONUtil.writeTextToFile(viewer.getJson().toString(), viewer.getJsonPath());
            }
        }
    };

    public JsonEvent jsonEvent = null;

    // JSONObject or JSONArray
    private Object wholeJson = null;

    private String wholeJsonFilePath = null;

    public List<WuKongListActivity> activityStack = new ArrayList<>();

    public void show(String title, Object json, String jsonFilePath) {
        wholeJson = json;
        wholeJsonFilePath = jsonFilePath;
        __showJsonElements__(title, null);
    }

    // JSONObject or JSONArray
    public Object getJson() {
        return wholeJson;
    }

    public String getJsonPath() {
        return wholeJsonFilePath;
    }

    public WuKongListActivity getTopJsonActivity() {
        return !activityStack.isEmpty() ? activityStack.get(activityStack.size() - 1) : null;
    }

    public void refreshTopJsonActivity() {
        WuKongListActivity act = getTopJsonActivity();
        if (act != null) {
            act.getListAdapter().notifyDataSetChanged();
        }
    }

    // for recursively call
    private void __showJsonElements__(String title, final String keyPath) {

        WuKongListActivity.startNewActivityInstance(title, null, (JSONObject) null, new WuKongListAdapter.Event() {
            @Override
            public void onDestroy(WuKongListActivity activity) {
                // when the last activity is destroy, release all resources
                if (objects[0] == wholeJson) {
                    wholeJson = null;
                    objects[0] = null;
                    objects = null;
                }
                activityStack.remove(activity);
                super.onDestroy(activity);
            }

            @Override
            public void onCreate(WuKongListActivity activity) {
                super.onCreate(activity);
                objects = new Object[1];
                objects[0] = WuKongJSONViewer.getCurrentJsonObjByKeyPath(wholeJson, keyPath);
                activityStack.add(activity);
            }

            public Boolean onCreateMenu(WuKongListActivity activity, Menu menu) {
                MenuItem item = menu.add(Menu.NONE, 0, Menu.NONE, "搜索");
                item.setIcon(R.mipmap.wukong_icon_search);
                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return null;
            }

            public Boolean onBackPressed(WuKongListActivity activity) {
                final Object currentJsonObj = objects[0];
                if (jsonEvent != null && jsonEvent.onBackEvent(WuKongJSONViewer.this, keyPath, currentJsonObj)) {
                    return true;
                }
                return false;
            }

            public Boolean onMenuItemSelected(final WuKongListActivity activity, MenuItem item) {
                if (item.getItemId() == android.R.id.home) {
                    final Object currentJsonObj = objects[0];
                    if (jsonEvent != null && jsonEvent.onBackEvent(WuKongJSONViewer.this, keyPath, currentJsonObj)) {
                        return true;
                    }
                }
                CharSequence title = item.getTitle();
                if (title == null || !title.equals("搜索")) {
                    return null;
                }
                WuKongHelper.showSearchEditTextToActivity(activity, new TextWatcher() {
                    private final Object currentJsonObj = objects[0];

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Object newDatasource = currentJsonObj;
                        String string = s.toString();
                        if (!string.isEmpty()) {
                            if (currentJsonObj instanceof JSONObject) {
                                newDatasource = createJsonObjectThatContains((JSONObject) currentJsonObj, string);
                            } else if (currentJsonObj instanceof JSONArray) {
                                newDatasource = createJsonArrayThatContains((JSONArray) currentJsonObj, string);
                            }
                        }
                        objects[0] = newDatasource;
                        activity.getListAdapter().notifyDataSetChanged();
                    }
                });
                return null;
            }

            @Override
            public int onGetCount(WuKongListAdapter adapter) {
                Object currentJsonObj = objects[0];
                if (currentJsonObj instanceof JSONObject) {
                    JSONArray names = ((JSONObject) currentJsonObj).names();
                    return names != null ? names.length() : 0;
                } else if (currentJsonObj instanceof JSONArray) {
                    return ((JSONArray) currentJsonObj).length();
                }
                return 0;
            }

            @Override
            public View onGetView(WuKongListAdapter adapter, int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = adapter.inflater.inflate(R.layout.wukong_item_key_value_arrow, null);
                }

                String key = null;

                Object currentJsonObj = objects[0];
                if (currentJsonObj instanceof JSONObject) {
                    ArrayList<String> names = WuKongJSONViewer.getSortedNames(((JSONObject) currentJsonObj));
                    key = names.get(position);
                } else if (currentJsonObj instanceof JSONArray) {
                    key = position + "";
                }

                ((TextView) convertView.findViewById(R.id.wukongItemLeftTextView)).setText(key);

                Object valueObj = null;

                if (key != null) {
                    if (currentJsonObj instanceof JSONObject) {
                        valueObj = ((JSONObject) currentJsonObj).opt(key);
                    } else if (currentJsonObj instanceof JSONArray) {
                        valueObj = position < ((JSONArray) currentJsonObj).length() ? ((JSONArray) currentJsonObj).opt(position) : null;
                    }
                }

                String value = null;
                if (valueObj != null && !(valueObj instanceof JSONObject) && !(valueObj instanceof JSONArray)) {
                    value = valueObj.toString();
                }
                value = value != null && value.length() > 18 ? value.substring(0, 18) + "..." : value;

                ((TextView) convertView.findViewById(R.id.wukongItemRightTextView)).setText(value);

                if (jsonEvent != null) {
                    View v = jsonEvent.onCellGetView(WuKongJSONViewer.this, keyPath, position, convertView, parent);
                    if (v != null) {
                        return v;
                    }
                }

                return convertView;
            }

            @Override
            public void onCellClicked(final WuKongListAdapter adapter, AdapterView<?> parent, View view, final int position, long id) {
                final Object currentJsonObj = objects[0];
                if (jsonEvent != null && jsonEvent.onCellTouch(WuKongJSONViewer.this, keyPath, position, currentJsonObj)) {
                    return;
                }

                String key = null;
                if (currentJsonObj instanceof JSONObject) {
                    ArrayList<String> names = WuKongJSONViewer.getSortedNames(((JSONObject) currentJsonObj));
                    key = names.get(position);
                } else if (currentJsonObj instanceof JSONArray) {
                    key = position + "";
                }

                Object valueObj = null;

                if (key != null) {
                    if (currentJsonObj instanceof JSONObject) {
                        valueObj = ((JSONObject) currentJsonObj).opt(key);
                    } else if (currentJsonObj instanceof JSONArray) {
                        valueObj = position < ((JSONArray) currentJsonObj).length() ? ((JSONArray) currentJsonObj).opt(position) : null;
                    }
                }

                if ((valueObj instanceof JSONObject) || (valueObj instanceof JSONArray)) {
                    String newKeyPath = keyPath != null ? keyPath + "~" + key : key;
                    __showJsonElements__(key, newKeyPath);

                } else {
                    Intent intent = new Intent(WuKongApi.getTopActivity(), WuKongTextActivity.class);
                    intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_TITLE, key);
                    intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_EDITABLE, jsonEvent != null);
                    intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_CONTENTS, valueObj != null ? valueObj.toString() : null);
                    final Object finalValueObj = valueObj;
                    final String finalKey = key;
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Activity act = WuKongApi.getTopActivity();
                            if (act instanceof WuKongTextActivity) {
                                ((WuKongTextActivity) act).saveEvent = new WuKongTextActivity.SaveEvent() {
                                    @Override
                                    public void onSave(String content) {

                                        Object realVal = null;
                                        if (finalValueObj instanceof Number) {
                                            realVal = WuKongReflect.parseValueOfString(finalValueObj.getClass(), content);
                                        }
                                        if (currentJsonObj instanceof JSONObject) {
                                            WuKongJSONObject.putJSONObject(((JSONObject) currentJsonObj), finalKey, realVal);
                                        } else if (currentJsonObj instanceof JSONArray) {
                                            WuKongJSONArray.put(((JSONArray) currentJsonObj), position, realVal);
                                        }

                                        adapter.notifyDataSetChanged();
                                        if (jsonEvent != null) {
                                            jsonEvent.onSaved(WuKongJSONViewer.this, keyPath, position, currentJsonObj, finalValueObj, content);
                                        }
                                    }
                                };
                            }
                        }
                    }, 1000);
                    WuKongApi.getTopActivity().startActivity(intent);
                }

            }

            @Override
            public Boolean onCellLongClicked(final WuKongListAdapter adapter, AdapterView<?> parent, View view, final int position, long id) {
                final Object currentJsonObj = objects[0];
                if (jsonEvent != null && jsonEvent.onCellLongTouch(WuKongJSONViewer.this, keyPath, position, currentJsonObj)) {
                    return true;
                }

                String key = null;
                if (currentJsonObj instanceof JSONObject) {
                    ArrayList<String> names = WuKongJSONViewer.getSortedNames(((JSONObject) currentJsonObj));
                    key = names.get(position);
                }
                final String finalKey = key;

                String[] actions = new String[]{key != null ? "修改键值" : "", "", "删除", "", "添加"};
                showTransparentActionsSheet(null, true, actions, new WuKongAction.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which, WuKongAction.ActionSheetItem sheetItem) {
                        final String name = sheetItem.name;
                        if (name.equals("修改键值")) {
                            WuKongAlert.showEditTextDialog("请输入新KEY值", finalKey, "确定",
                                    (dialog, which1) -> {
                                        String newKey = WuKongAlert.getTextInShowedEditTextDialog(dialog);
                                        try {
                                            ((JSONObject) currentJsonObj).put(newKey, ((JSONObject) currentJsonObj).remove(finalKey));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        adapter.notifyDataSetChanged();
                                        if (jsonEvent != null) {
                                            jsonEvent.onKeyChanged(WuKongJSONViewer.this, keyPath, position, currentJsonObj, finalKey, newKey);
                                        }
                                    });
                        } else if (name.equals("删除")) {
                            WuKongAlert.show("警告", "此操作不可回退，确定要删除吗？",
                                    "确定", (dialog, which12) -> {

                                        Object currentJsonObj1 = objects[0];
                                        if (currentJsonObj1 instanceof JSONObject) {
                                            ArrayList<String> names = WuKongJSONViewer.getSortedNames(((JSONObject) currentJsonObj1));
                                            String key1 = names.get(position);
                                            ((JSONObject) currentJsonObj1).remove(key1);
                                        } else if (currentJsonObj1 instanceof JSONArray) {
                                            ((JSONArray) currentJsonObj1).remove(position);
                                        }

                                        adapter.notifyDataSetChanged();
                                        if (jsonEvent != null) {
                                            jsonEvent.onDeleted(WuKongJSONViewer.this, keyPath, position, currentJsonObj1);
                                        }
                                    }, "取消", null);

                        } else if (name.equals("添加")) {
                            final String[] keyTemps = new String[]{"anything"};
                            String[] actions = new String[]{"数组", "", "对象", "", "字符", "", "原始类型"};
                            WuKongAction dialog = showTransparentActionsSheet(null, true, actions,
                                    (which13, sheetItem1) -> {
                                        final String action = sheetItem1.name;
                                        String content = "";
                                        if (action.equals("数组")) {
                                            content = "[\n\n]";
                                        } else if (action.equals("对象")) {
                                            content = "{\n\n}";
                                        }
                                        Intent intent = new Intent(WuKongApi.getTopActivity(), WuKongTextActivity.class);
                                        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_TITLE, name + " - " + action);
                                        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_EDITABLE, true);
                                        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_CONTENTS, content);
                                        WuKongApi.getTopActivity().startActivity(intent);
                                        WuKongJSONUtil.retryInMainThread(1000, 3, new WuKongJSONUtil.Action() {
                                            @Override
                                            public boolean execute(int retryIndex) {
                                                if (WuKongApi.getTopActivity() instanceof WuKongTextActivity) {
                                                    final WuKongTextActivity act = (WuKongTextActivity) WuKongApi.getTopActivity();
                                                    act.saveEvent = new WuKongTextActivity.SaveEvent() {
                                                        @Override
                                                        public void onSave(String content) {
                                                            if (content == null) {
                                                                return;
                                                            }
                                                            content = content.trim();
                                                            Object obj = null;
                                                            if (content.startsWith("{")) {
                                                                obj = WuKongJSONObject.createJSONObject(content);
                                                            } else if (content.startsWith("[")) {
                                                                obj = WuKongJSONArray.createJSONArray(content);
                                                            } else if (action.equals("字符")) {
                                                                obj = content;
                                                            } else if (action.equals("原始类型")) {
                                                                if (content.equals("true") || content.equals("false")) {
                                                                    obj = Boolean.valueOf(content);
                                                                } else {
                                                                    if (content.contains(".")) {
                                                                        obj = Double.valueOf(content);
                                                                    } else {
                                                                        obj = Long.valueOf(content);
                                                                    }
                                                                }
                                                            }

                                                            Object currentJsonObj12 = objects[0];
                                                            if (currentJsonObj12 instanceof JSONObject) {
                                                                WuKongJSONObject.putJSONObject((JSONObject) currentJsonObj12, keyTemps[0], obj);
                                                            } else if (currentJsonObj12 instanceof JSONArray) {
                                                                WuKongJSONArray.insert((JSONArray) currentJsonObj12, obj, position);
                                                            }

                                                            adapter.notifyDataSetChanged();
                                                            if (jsonEvent != null) {
                                                                jsonEvent.onAdded(WuKongJSONViewer.this, keyPath, position, currentJsonObj12);
                                                            }
                                                        }
                                                    };
                                                    return true;
                                                }
                                                return false;
                                            }
                                        });
                                    });
                            dialog.setCancelable(false);

                            WuKongAlert.showEditTextDialog("首先输入新KEY值，然后选择VALUE值类型", "", "确定",
                                    (dialog1, which14) -> keyTemps[0] = WuKongAlert.getTextInShowedEditTextDialog(dialog1));

                        }
                    }
                });
                return true;
            }
        });

    }

    /**
     * Static Methods
     */
    public static ArrayList<String> getSortedNames(@NonNull JSONObject json) {
        ArrayList<String> results = (ArrayList<String>) WuKongJSONArray.toList(json.names());
        Collections.sort(results);
        return results;
    }

    public static String getLastKey(String keyPath) {
        if (keyPath == null) {
            return null;
        }
        String[] paths = keyPath.split("~");
        if (paths.length > 0) {
            return paths[paths.length - 1];
        }
        return null;
    }

    public static String getParentKeyPath(String keyPath) {
        if (keyPath == null) {
            return null;
        }
        if (!keyPath.contains("~")) {
            return null;
        }
        return keyPath.replace("~" + getLastKey(keyPath), "");
    }

    public static Object getCurrentJsonObjByKeyPath(Object currentJsonObj, String keyPath) {
        if (keyPath == null) {
            return currentJsonObj;
        }
        String[] paths = keyPath.split("~");
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            if (path.isEmpty()) {
                continue;
            }
            if (currentJsonObj instanceof JSONObject) {
                currentJsonObj = ((JSONObject) currentJsonObj).opt(path);
            } else if (currentJsonObj instanceof JSONArray) {
                currentJsonObj = ((JSONArray) currentJsonObj).opt(Integer.parseInt(path));
            }
        }
        return currentJsonObj;
    }

    public static JSONArray createJsonArrayThatContains(JSONArray array, String contains) {
        JSONArray result = new JSONArray();
        for (int i = 0; array != null && i < array.length(); i++) {
            Object obj = array.opt(i);
            if (obj.toString().contains(contains)) {
                result.put(obj);
            }
        }
        return result;
    }

    public static JSONObject createJsonObjectThatContains(JSONObject json, String contains) {
        JSONObject result = new JSONObject();
        JSONArray names = json != null ? json.names() : null;
        for (int i = 0; names != null && i < names.length(); i++) {
            String name = names.optString(i);
            if (name.contains(contains)) {
                try {
                    result.put(name, json.opt(name));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static JSONObject createParentJsonRecursiveIfNull(JSONObject configJson, String keyPath) {
        if (configJson == null || keyPath == null) {
            return null;
        }
        Object object = WuKongJSONViewer.getCurrentJsonObjByKeyPath(configJson, keyPath);
        if (object == null) {
            object = new JSONObject();
            String lastKey = WuKongJSONViewer.getLastKey(keyPath);
            String parentKeyPath = WuKongJSONViewer.getParentKeyPath(keyPath);
            JSONObject parent = createParentJsonRecursiveIfNull(configJson, parentKeyPath);
            if (parent == null) {
                parent = configJson;
            }
            WuKongJSONObject.putJSONObject(parent, lastKey, object);
        }
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    /**
     * Util Methods
     */
    public static void showTransparentActionsSheet(String title, String[] actions, WuKongAction.OnSheetItemClickListener listener) {
        showTransparentActionsSheet(title, false, actions, listener);
    }

    public static WuKongAction showTransparentActionsSheet(String title, boolean showCancelItem, String[] actions, WuKongAction.OnSheetItemClickListener listener) {
        int[] titleColors = new int[]{Color.WHITE};
        Drawable[] stateDrawables = new Drawable[actions.length];
        for (int i = 0; i < actions.length; i++) {
            stateDrawables[i] = createSelector(WuKongApi.getAppContext(), Color.GRAY, Color.RED);
        }
        WuKongAction dialog = WuKongAction.showActions(title, actions, titleColors, stateDrawables, listener);
        if (showCancelItem) {
            StateListDrawable cancelDrawable = createSelector(WuKongApi.getAppContext(), Color.GRAY, Color.RED);
            dialog.textViewCancel.setBackground(cancelDrawable);
        } else {
            dialog.textViewCancel.setVisibility(View.GONE);
        }
        dialog.dialog.getWindow().setDimAmount(0f);
        return dialog;
    }

    public static StateListDrawable createSelector(Context context, int normalColor, int pressColor) {
        // selecor
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.setEnterFadeDuration(100);
        stateListDrawable.setExitFadeDuration(100);
        // stateListDrawable.addState(new int[]{}, new ColorDrawable(getAppColorResId(context)));
        // stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.RED));
        // stateListDrawable.addState(new int[]{}, new ColorDrawable(Color.GRAY));  // 必须放在最后
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressColor));
        stateListDrawable.addState(new int[]{}, new ColorDrawable(normalColor));
        return stateListDrawable;
    }
}

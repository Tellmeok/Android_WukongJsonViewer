package com.android.wukong.viewer.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.wukong.viewer.R;
import com.android.wukong.viewer.WuKongApi;
import com.android.wukong.viewer.util.WuKongAssetser;
import com.android.wukong.viewer.util.WuKongPreferences;
import com.android.wukong.viewer.util.WuKongReflect;
import com.android.wukong.viewer.util.WuKongHelper;
import com.android.wukong.viewer.util.json.WuKongJSONObject;
import com.android.wukong.viewer.util.json.WuKongJSONTranslator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WuKongListJsonAdapter extends WuKongListAdapter {

    private static final String TAG = "WuKongListJsonAdapter";

    public static int kViewTagKeyIsFirstInit = 1314520;

    private JSONObject listRenderJson = null;

    private List<String> types = null;

    public WuKongListJsonAdapter(Context mContext) {
        try {
            if (mContext == null) {
                return;
            }
            inflater = LayoutInflater.from(mContext);

            // get file name
            String listRenderJsonFileName = getListRenderJsonFileName(mContext);
            JSONObject json = WuKongAssetser.getAssetsAsJsonObject("render/" + listRenderJsonFileName);
            if (json != null) {
                initializeRenderJson(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getListRenderJsonFileName(Context mContext) {
        String fileName = null;
        if (mContext instanceof Activity) {
            fileName = ((Activity) mContext).getIntent().getStringExtra("JsonNameOfListRender");
        }
        if (fileName == null) {
            fileName = this.getClass().getSimpleName() + ".json";   // default is class name
        }
        return fileName;
    }

    public void initializeRenderJson(JSONObject renderJson) {
        if (renderJson == null) {
            return;
        }
        try {
            listRenderJson = renderJson;
            types = new ArrayList<>();

            // parse types
            JSONArray rows = listRenderJson.optJSONArray("rows");
            for (int position = 0; rows != null && position < rows.length(); position++) {
                JSONObject json = rows.optJSONObject(position);
                String type = json.optString("TYPE");
                if (!types.contains(type)) {
                    types.add(type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (listRenderJson.has("config_of_list_view")) {
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject config = listRenderJson.optJSONObject("config_of_list_view");
                        ListView listView = getOwnerListView();
                        // 根据Json设置Views的属性、值、样式等等
                        setViewsValuesAccordingJson(config, listView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    // -------------------------------- Item 重用 --------------------------------
    @Override
    public int getViewTypeCount() {
        if (listRenderJson != null) {
            return types.size();
        } else {
            return super.getViewTypeCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (listRenderJson != null) {
            JSONObject json = listRenderJson.optJSONArray("rows").optJSONObject(position);
            String type = json.optString("TYPE");
            return types.indexOf(type);
        } else {
            return super.getItemViewType(position);
        }
    }
    // -------------------------------- Item 重用 --------------------------------


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listRenderJson != null) {
            JSONObject eventsJson = listRenderJson.optJSONObject("events");
            if (eventsJson != null) {
                String title = WuKongHelper.getClickItemTitle(parent, view, position, id);
                WuKongHelper.dispatchEvent(this, title, eventsJson.optString(title));
                return;
            }
        }
        super.onItemClick(parent, view, position, id);
    }

    @Override
    public int getCount() {
        if (listRenderJson != null) {
            return listRenderJson.optJSONArray("rows").length();
        } else {
            return super.getCount();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (listRenderJson != null) {
            JSONObject json = listRenderJson.optJSONArray("rows").optJSONObject(position);

            // create convertView
            if (convertView == null) {
                try {
                    String type = json.optString("TYPE");
                    int resource_id = R.layout.class.getDeclaredField(type).getInt(R.layout.class);
                    convertView = inflater.inflate(resource_id, null);
                    convertView.setTag(kViewTagKeyIsFirstInit, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                convertView.setTag(kViewTagKeyIsFirstInit, false);
            }

            // 根据Json设置Views的属性、值、样式等等
            setViewsValuesAccordingJson(json, convertView);
        }


        // 填一些通用的数据
        if (convertView != null) {
            // 拿出 views
            TextView keyTextView = convertView.findViewById(R.id.wukongItemLeftTextView);
            String tag = keyTextView != null ? (String) keyTextView.getTag() : null;
            TextView valueTextView = convertView.findViewById(R.id.wukongItemRightTextView);
            Switch valueSwitchView = convertView.findViewById(R.id.wukongItemRightSwitch);

            // Switch 事件, 里面会判断多次调用，只设置一次
            setupSharedPreferenceEventToSwitcher(this, valueSwitchView);

            // 根据 Preference 写 Switch
            setupSharedPreferenceValueToSwitcher(valueSwitchView, tag);

            // 根据 Preference 写 TextView
            setupSharedPreferenceValueToTextView(valueTextView, tag);
        }

        return super.getView(position, convertView, parent);
    }

    protected void onSwitchValueChanged(@NonNull WuKongListAdapter adapter, @NonNull Switch onSwitchView, @NonNull TextView onTextView) {
        // Override by subclass
        Event event = getEvent();
        if (event != null) {
            Method m = WuKongReflect.getMethodWithName(event.getClass(), "onSwitchValueChanged");
            if (m != null) {
                WuKongReflect.invokeMethod(event, "onSwitchValueChanged",
                        new Class[]{WuKongListAdapter.class, Switch.class, TextView.class},
                        new Object[]{adapter, onSwitchView, onTextView});
            }
        }
    }

    /**
     * Public & Private Methods
     */

    public static void setViewsValuesAccordingJson(JSONObject json, View containerView) {
        if (json == null || containerView == null) {
            return;
        }

        JSONObject jsonWith = WuKongJSONObject.getJSONWithKeyPrefixs(json, new String[]{"method_", "field_"});
        setFieldMethodValue(containerView, jsonWith);

        JSONObject SUB_VIEWS = json.optJSONObject("SUB_VIEWS");
        if (SUB_VIEWS == null) {
            return;
        }

        // 通过反射调用方法来设置数据及其样式
        Iterator<String> idsIterator = SUB_VIEWS.keys();
        while (idsIterator.hasNext()) {
            try {
                // 获取子View Json
                String subViewIdString = idsIterator.next();
                JSONObject subViewJson = SUB_VIEWS.optJSONObject(subViewIdString);

                // 获取子View
                int subViewIdResId = R.id.class.getDeclaredField(subViewIdString).getInt(R.id.class);
                View subView = containerView.findViewById(subViewIdResId);

                Iterator<String> methodsIterator = subViewJson.keys();
                while (methodsIterator.hasNext()) {

                    try {
                        String methodFieldName = methodsIterator.next();
                        Object methodFieldValue = subViewJson.opt(methodFieldName);
                        setFieldMethodValue(subView, methodFieldName, methodFieldValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void setFieldMethodValue(Object target, JSONObject json) {
        if (target == null || json == null) {
            return;
        }
        Iterator<?> iterator = json.keys();
        while (iterator.hasNext()) {
            try {
                String name = (String) iterator.next();
                setFieldMethodValue(target, name, json.opt(name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void setFieldMethodValue(Object target, String name, Object value) throws Exception {
        if (target == null || name == null || value == null) {
            return;
        }

        // 获取值
        if (value instanceof String) {
            value = translateTheValue(target, name, value);
        } else if (value instanceof JSONObject) {
            setTheValueCauseIsJson(target, name, value);
            return;
        }

        if (name.startsWith("method_")) {
            // 1. ----------------- 通过方法设置值 -----------------

            // 查找方法
            String methodName = getMethodNameFromName(name);
            Method method = searchTheMethodAccordingName(target, methodName, value);

            // 找到了?
            if (method != null) {
                invokeTheMethodAccordingValue(target, method, value);
            } else {
                Log.d(TAG, "set cannot find method: " + methodName);
            }

        } else if (name.startsWith("field_")) {
            // 2. ----------------- 通过属性设置值 -----------------

            // 查找属性
            String fieldName = getFieldNameFromName(name);
            Field field = WuKongReflect.searchField(target, fieldName);

            // 找到了?
            if (field != null) {
                field.set(target, value);
            } else {
                Log.d(TAG, "set cannot find field: " + fieldName);
            }
        }
    }

    private static Object translateTheValue(Object target, String name, Object value) {
        if (!(value instanceof String)) {
            return value;
        }

        // ----------------- 如果是自定义的值，那么获取值 -----------------
        String string = (String) value;
        if (string.startsWith("defined:")) {
            String sp = string.replaceFirst("defined:", "");
            String[] strings = sp.split(",");

            String clazzName = null;
            String filedName = null;
            String methodName = null;
            String methodArgs = null;
            for (String s : strings) {
                if (s.startsWith("class=")) {
                    clazzName = s.replace("class=", "").trim();
                    clazzName = clazzName.replace("[PKG_NAME]", WuKongApi.getPackageName());
                } else if (s.startsWith("field=")) {
                    filedName = s.replace("field=", "").trim();
                } else if (s.startsWith("method=")) {
                    methodName = s.replace("method=", "").trim();
                } else if (s.startsWith("args=")) {
                    methodArgs = s.replace("args=", "").trim();
                }
            }

            // [__package_name__].R.mipmap.info_black  ->  defined:class=[PKG_NAME].R$mipmap,field=info_black
            Class clazz = null;
            if (clazzName != null) {
                Exception e1 = null;
                Exception e2 = null;
                Exception e3 = null;
                if (clazz == null) {
                    try {
                        clazz = WuKongListJsonAdapter.class.getClassLoader().loadClass(clazzName);
                    } catch (Exception e) {
                        e1 = e;
                    }
                }
                if (clazz == null) {
                    try {
                        clazz = target.getClass().getClassLoader().loadClass(clazzName);
                    } catch (Exception e) {
                        e2 = e;
                    }
                }
                if (clazz == null) {
                    try {
                        clazz = Class.forName(clazzName);
                    } catch (Exception e) {
                        e3 = e;
                    }
                }
                if (clazz == null) {
                    if (e1 != null) {
                        e1.printStackTrace();
                    }
                    if (e2 != null) {
                        e2.printStackTrace();
                    }
                    if (e3 != null) {
                        e3.printStackTrace();
                    }
                }
            }
            if (clazz != null && filedName != null) {
                // 获取类的Filed值
                value = WuKongReflect.getFieldValue(clazz, filedName);
            } else if (clazz != null && methodName != null) {
                // 获取类的Method值
                if (methodArgs == null) {
                    value = WuKongReflect.invokeMethod(clazz, methodName, null, null);
                } else {
                    value = WuKongReflect.invokeMethod(clazz, methodName, new Class[]{String.class}, new Object[]{methodArgs});
                }
            }

        }

        return value;
    }

    private static void setTheValueCauseIsJson(Object target, String name, Object value) {
        if (!(value instanceof JSONObject)) {
            return;
        }

        JSONObject jsonObject = (JSONObject) value;

        if (name.startsWith("method_")) {
            // 查找方法
            String setMethodName = getMethodNameFromName(name);
            String getMethodName = setMethodName.replaceFirst("set", "get");
            Method getMethod = null;
            try {
                getMethod = WuKongReflect.searchMethod(target, getMethodName, new Class[]{});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            // 找到了?
            if (getMethod != null) {
                try {
                    // 获取值
                    Object vo = getMethod.invoke(target);

                    JSONObject jsonWith = WuKongJSONObject.getJSONWithKeyPrefixs(jsonObject, new String[]{"method_", "field_"});
                    setFieldMethodValue(vo, jsonWith);

                    JSONObject jsonWithout = WuKongJSONObject.getJSONWithoutKeyPrefixs(jsonObject, new String[]{"method_", "field_"});
                    WuKongJSONTranslator.getInstance().setJsonValuesToObjectFields(vo, jsonWithout);

                    // 设置值
                    Method setMethod = searchTheMethodAccordingName(target, setMethodName, vo);
                    if (setMethod != null) {
                        setMethod.invoke(target, vo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "get cannot find method: " + getMethodName);
            }

        } else if (name.startsWith("field_")) {
            // 查找属性
            String fieldName = getFieldNameFromName(name);
            Field field = WuKongReflect.searchField(target, fieldName);

            // 找到了?
            if (field != null) {
                try {

                    Object vo = field.get(target);
                    WuKongJSONTranslator.getInstance().setJsonValuesToObjectFields(vo, jsonObject);

                    // 将值设置回去
                    field.set(target, vo);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "get cannot find field: " + fieldName);
            }

        }
    }

    private static Method searchTheMethodAccordingName(Object target, String methodName, Object value) {
        // 查找方法
        Method method = null;
        Class parameterClazz = value.getClass();
        try {
            method = WuKongReflect.searchMethod(target, methodName, new Class[]{parameterClazz});
        } catch (NoSuchMethodException e) {
            // 找不到? 试试 Object class
            // if (Object.class.isAssignableFrom(parameterClazz)){} // 不判断了，多数都是继承Object.class
            try {
                method = WuKongReflect.searchMethod(target, methodName, new Class[]{Object.class});
            } catch (NoSuchMethodException ex) {
                // nothing ...
            }
        }

        // 找不到? 试试uperClass
        if (method == null) {
            Class superClass = parameterClazz;
            while (method == null && superClass != null && superClass != Object.class) {
                superClass = superClass.getSuperclass();
                try {
                    method = WuKongReflect.searchMethod(target, methodName, new Class[]{superClass});
                } catch (NoSuchMethodException e) {
                    // nothing ...
                }
            }
        }

        // 还查找不到? 用参数类型的接口Class试试
        if (method == null) {
            Class<?>[] interfacesClazz = parameterClazz.getInterfaces();
            for (int i = 0; i < interfacesClazz.length; i++) {
                Class t = interfacesClazz[i];
                try {
                    method = WuKongReflect.searchMethod(target, methodName, new Class[]{t});
                } catch (NoSuchMethodException e) {
                    // nothing ...
                }
                if (method != null) {
                    break;
                }
            }
        }

        // 还查找不到? 看看是不是原始类型的对应的类 (java.lang.Class.java)
        Class[] primitivePossibleClasses = null;
        if (parameterClazz == Boolean.class) {
            primitivePossibleClasses = new Class[]{boolean.class};
        } else if (parameterClazz == Character.class) {
            primitivePossibleClasses = new Class[]{char.class};
        } else if (parameterClazz == Byte.class) {
            primitivePossibleClasses = new Class[]{byte.class};
        } else if (parameterClazz == Short.class) {
            primitivePossibleClasses = new Class[]{short.class};
        } else if (parameterClazz == Integer.class) {
            primitivePossibleClasses = new Class[]{int.class, long.class};
        } else if (parameterClazz == Long.class) {
            primitivePossibleClasses = new Class[]{long.class, int.class};
        } else if (parameterClazz == Float.class) {
            primitivePossibleClasses = new Class[]{float.class, double.class};
        } else if (parameterClazz == Double.class) {
            primitivePossibleClasses = new Class[]{double.class, float.class};
        } else if (parameterClazz == Void.class) {
            primitivePossibleClasses = new Class[]{void.class};
        }
        for (int i = 0; primitivePossibleClasses != null && i < primitivePossibleClasses.length; i++) {
            try {
                Class clazz = primitivePossibleClasses[i];
                method = WuKongReflect.searchMethod(target, methodName, new Class[]{clazz});
            } catch (NoSuchMethodException e) {
                // nothing ...
            }
            if (method != null) {
                break;
            }
        }

        return method;
    }

    private static void invokeTheMethodAccordingValue(Object target, Method method, Object value) {
        if (method == null) {
            return;
        }

        try {
            Class[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
                Class methodParameterType = parameterTypes[0];
                if (methodParameterType.isPrimitive()) {
                    if (Number.class.isAssignableFrom(value.getClass())) {
                        if (methodParameterType == long.class) {
                            value = ((Number) value).longValue();
                        } else if (methodParameterType == int.class) {
                            value = ((Number) value).intValue();
                        } else if (methodParameterType == double.class) {
                            value = ((Number) value).doubleValue();
                        } else if (methodParameterType == float.class) {
                            value = ((Number) value).floatValue();
                        }
                    }
                }
            }

            method.invoke(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMethodNameFromName(String name) {
        String methodName = name.replace("method_", "");
        if (methodName.contains("|")) {
            methodName = methodName.split("\\|")[0];
        }
        return methodName;
    }

    private static String getFieldNameFromName(String name) {
        return name.replace("field_", "");
    }

    /**
     * Public Static Methods
     */
    // switcher event
    public static void setupSharedPreferenceEventToSwitcher(final WuKongListJsonAdapter adapter, final Switch valueSwitchView) {
        if (valueSwitchView == null) {
            return;
        }
        // https://stackoverflow.com/a/9093699
        // int position = getListView().getPositionForView(v);
        if (WuKongReflect.getFieldValue(valueSwitchView, "mOnCheckedChangeListener") == null) {
            valueSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ViewParent parent = buttonView.getParent();
                    if (parent instanceof ViewGroup) {
                        ViewGroup viewGroup = (ViewGroup) parent;
                        int childCount = viewGroup.getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            View v = viewGroup.getChildAt(i);
                            if (!(v instanceof TextView)) {
                                continue;
                            }

                            TextView textView = (TextView) v;
                            adapter.onSwitchValueChanged(adapter, valueSwitchView, textView);

                            Object tagObj = textView.getTag();
                            if (!(tagObj instanceof String)) {
                                continue;
                            }
                            String tag = (String) tagObj;
                            if (tag.startsWith("SP")) {

                                String key = getSharedPreferenceKeyFromTag(tag);
                                String file = WuKongListJsonAdapter.getSharedPreferenceFileFromTag(tag);

                                Object object = WuKongPreferences.getWithFile(file, key, null);
                                if (object == null || (Boolean) object != isChecked) {
                                    WuKongPreferences.setWithFile(file, key, isChecked);
                                }

                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    // Switch value
    public static void setupSharedPreferenceValueToSwitcher(Switch valueSwitchView, String tag) {
        if (valueSwitchView == null || tag == null) {
            return;
        }
        String key = getSharedPreferenceKeyFromTag(tag);
        String file = WuKongListJsonAdapter.getSharedPreferenceFileFromTag(tag);
        Boolean value = (Boolean) WuKongPreferences.getWithFile(file, key, null);
        valueSwitchView.setChecked(value == null ? false : value);
    }

    // TextView value
    public static void setupSharedPreferenceValueToTextView(TextView valueTextView, String tag) {
        if (valueTextView == null || tag == null) {
            return;
        }

        String key = getSharedPreferenceKeyFromTag(tag);
        String file = WuKongListJsonAdapter.getSharedPreferenceFileFromTag(tag);
        Object value = WuKongPreferences.getWithFile(file, key, null);
        valueTextView.setText(value != null ? value.toString() : "");
    }

    // shared preference key
    public static String getSharedPreferenceKeyFromTag(String tag) {
        // like "SP-keep_active_time_float"
        if (tag == null) {
            return "";
        }
        String[] strings = tag.split("-");
        if (strings.length >= 2) {
            return strings[strings.length - 1];
        }
        return "";
    }

    // shared preference file
    public static String getSharedPreferenceFileFromTag(String tag) {
        // like "SP[sp_of_active]-keep_active_time_float"
        String result = WuKongApi.getPackageName();
        String[] strings = tag != null ? tag.split("-") : null;
        if (strings != null && strings.length >= 2) {
            String string = strings[0];   // SP[xml_file_name]
            result = string.replace("SP", "").replace("[", "").replace("]", "");
            if (result.equals("Global")) {
                result = WuKongPreferences.Global.getXmlFileName();
            } else if (result.equals("User")) {
                result = WuKongPreferences.User.getXmlFileName();
            } else if (result.equals("Temp")) {
                result = WuKongPreferences.Temp.getXmlFileName();
            }
        }
        return result;
    }

}

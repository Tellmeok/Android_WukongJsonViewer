package com.android.wukong.viewer.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WuKongJSONArray {

    /**
     * Iterate Element
     */
    public interface Iterator {
        boolean handler(Object element);
    }

    public static void iterateJSONArray(JSONArray jsonArray, Iterator iterator) {
        if (jsonArray == null) {
            return;
        }

        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            try {
                Object element = jsonArray.opt(i);
                if (iterator != null && iterator.handler(element)) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Iterate JSONObject Element
     */
    public static interface IterateHandler {
        public boolean iterate(JSONObject json, int index) throws Exception;
    }

    public static void iterateJSONArrayElement(JSONArray jsonArray, IterateHandler handler) {
        if (jsonArray == null) {
            return;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                if (handler != null && handler.iterate(element, i)) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void insert(JSONArray array, Object value, int atIndex) {
        try {
            int length = array.length();
            if (atIndex < length) {
                // move the tails after atIndex
                for (int i = length - 1; i >= atIndex; i--) {
                    array.put(i + 1, array.opt(i));
                }
                array.put(atIndex, value);
            } else {
                // just add to tail
                array.put(value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void put(JSONArray array, Object value) {
        if (array == null) {
            return;
        }
        array.put(value);
    }

    public static void put(JSONArray array, int atIndex, Object value) {
        try {
            array.put(atIndex, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * Create methods
     */
    public static JSONArray createJSONArrayFromFile(String jsonFile) {
        String string = WuKongJSONUtil.readFileToText(jsonFile);
        JSONArray json = null;
        if (string != null) {
            json = createJSONArray(string);
        }
        return json;
    }

    public static JSONArray create(Object... values) {
        JSONArray json = new JSONArray();
        for (int i = 0; values != null && i < values.length; i++) {
            json.put(values[i]);
        }
        return json;
    }

    public static JSONArray createJSONArray(String json) {
        return createJSONArray(json, null);
    }

    public static JSONArray createJSONArray(String json, JSONArray def) {
        if (json == null || json.isEmpty()) {
            return def;
        }
        JSONArray jsonArray = def;
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray createJSONArrayFromString(String string, String separator) {
        JSONArray results = new JSONArray();

        if (string != null) {
            String[] strings = string.split(separator);
            for (String str : strings) {
                results.put(str);
            }
        }

        return results;
    }

    /*
     * Reverse and Merge
     */
    public static JSONArray reverseJSONArray(JSONArray array) {
        JSONArray jsonArray = new JSONArray();

        if (array != null) {
            for (int i = array.length() - 1; i >= 0; i--) {
                Object obj = array.opt(i);
                jsonArray.put(obj);
            }
        }

        return jsonArray;
    }

    public static void mergeTwoJSONArray(JSONArray destination, JSONArray source) {
        if (destination == null || source == null) {
            return;
        }
        for (int i = 0; i < source.length(); i++) {
            Object obj = source.opt(i);
            destination.put(obj);
        }
    }

    // element is JSONObject
    public static Object getJSONArrayElementValue(JSONArray jsonArray, int index, String key) {
        if (jsonArray == null || index >= jsonArray.length()) {
            return null;
        }
        JSONObject element = jsonArray.optJSONObject(index);
        Object v = element.opt(key);
        return v;
    }

    // element is JSONObject
    public static JSONObject getJSONArrayElement(JSONArray jsonArray, String key, Object value) {
        if (jsonArray == null) {
            return null;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                Object v = element.opt(key);
                if (value.equals(v)) {
                    return element;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static JSONObject getJSONArrayElementHasKey(JSONArray jsonArray, String hasKey) {
        if (jsonArray == null) {
            return null;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                if (element.has(hasKey)) {
                    return element;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static JSONObject getJSONArrayElementHasKey(JSONArray jsonArray, String superKey, String hasSubKey) {
        if (jsonArray == null) {
            return null;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                JSONObject json = element.optJSONObject(superKey);
                if (json != null && json.has(hasSubKey)) {
                    return json;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // element is JSONObject
    public static List getJSONArrayElementsOfKey(JSONArray jsonArray, String key) {
        List list = new ArrayList();
        for (int i = 0; jsonArray != null && i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                Object v = element.opt(key);
                if (v != null) {
                    list.add(v);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // element is JSONObject
    public static Object removeJSONArrayElement(JSONArray jsonArray, String key, Object value) {
        if (jsonArray == null) {
            return null;
        }

        Object removedObj = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                Object v = element.opt(key);
                if (value.equals(v)) {
                    removedObj = jsonArray.remove(i);
                    i--;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return removedObj;
    }

    // element is JSONObject
    public static void replaceJSONArrayElementKey(JSONArray jsonArray, String key, String newKey) {
        if (jsonArray == null) {
            return;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.optJSONObject(i);
                element.put(newKey, element.opt(key));
                element.remove(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // element is JSONObject
    public static JSONObject getValues(JSONArray array, String valueAsKey) {
        if (array == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject element = array.optJSONObject(i);
                json.put(element.optString(valueAsKey), element);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    // element is JSONObject
    public static JSONArray getKeysValues(JSONArray array, String... keys) {
        if (array == null) {
            return null;
        }
        JSONArray results = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject element = array.optJSONObject(i);

                JSONObject json = new JSONObject();
                for (String k : keys) {
                    json.put(k, element.opt(k));
                }
                results.put(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    /**
     * To Methods
     */
    public static ArrayList jsonToList(JSONArray json) {
        ArrayList result = new ArrayList();
        for (int i = 0; json != null && i < json.length(); i++) {
            Object value = json.opt(i);
            if (value instanceof JSONObject) {
                value = WuKongJSONObject.jsonToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
            }
            result.add(value);
        }
        return result;
    }

    public static ArrayList<HashMap<String, Object>> toListMap(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        ArrayList list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.opt(i);
            if (value instanceof JSONObject) {
                JSONObject element = (JSONObject) value;
                Map<String, Object> map = WuKongJSONObject.jsonToMap(element);
                list.add(map);
            }
        }
        return list;
    }

    public static ArrayList toList(JSONArray array) {
        ArrayList list = new ArrayList();
        for (int i = 0; array != null && i < array.length(); i++) {
            Object o = array.opt(i);
            list.add(o);
        }
        return list;
    }

    public static Object[] toArray(JSONArray array) {
        Object[] results = new Object[array.length()];
        for (int i = 0; i < array.length(); i++) {
            results[i] = array.opt(i);
        }
        return results;
    }

    public static int[] toIntArray(JSONArray array) {
        int[] results = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            results[i] = array.optInt(i);
        }
        return results;
    }

    public static String[] toStringArray(JSONArray array) {
        String[] results = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            results[i] = array.optString(i);
        }
        return results;
    }

    public static int indexOfObject(JSONArray array, Object obj) {
        if (array == null || obj == null) {
            return -1;
        }
        for (int i = 0; i < array.length(); i++) {
            if (obj.equals(array.opt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static void removeJSONArrayBeforeIndex(JSONArray jsonArray, int index) {
        if (jsonArray == null) {
            return;
        }
        for (int i = 0; i < index; i++) {
            if (jsonArray.length() > 0) {
                jsonArray.remove(0);
            }
        }
    }

    public static void removeJSONArrayAfterIndex(JSONArray jsonArray, int index) {
        if (jsonArray == null) {
            return;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i > index) {
                jsonArray.remove(i);
                i--;
            }
        }
    }

    public static void replaceJsonElementsValues(JSONArray sourceJson, JSONArray replaceJson) {
        if (sourceJson == null || replaceJson == null) {
            return;
        }
        int min = Math.min(sourceJson.length(), replaceJson.length());
        for (int i = 0; i < min; i++) {
            Object val = sourceJson.opt(i);
            Object replaceVal = replaceJson.opt(i);

            if (val instanceof JSONObject && replaceVal instanceof JSONObject) {
                WuKongJSONObject.replaceJsonElementsValues((JSONObject) val, (JSONObject) replaceVal);
            } else if (val instanceof JSONArray && replaceVal instanceof JSONArray) {
                WuKongJSONArray.replaceJsonElementsValues((JSONArray) val, (JSONArray) replaceVal);
            } else {
                WuKongJSONArray.put(sourceJson, i, replaceVal);
            }
        }

        for (int i = min; i < replaceJson.length(); i++) {
            sourceJson.put(replaceJson.opt(i));
        }
    }

    public static interface JNHandler {
        public boolean handle(int index, Object value);
    }

    public static void iterate(JSONArray jsonArray, JNHandler handler) {
        if (jsonArray == null || handler == null) {
            return;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            if (handler.handle(i, jsonArray.opt(i))) {
                break;
            }
        }
    }

}

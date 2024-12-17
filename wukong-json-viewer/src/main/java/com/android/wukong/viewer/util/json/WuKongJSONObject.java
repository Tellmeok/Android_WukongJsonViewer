package com.android.wukong.viewer.util.json;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;


public class WuKongJSONObject {

    /*
     * Format JSON String
     */

    public static String formatJsonString(String string) {
        int level = 0;
        StringBuffer jsonFormatString = new StringBuffer();
        for (int index = 0; index < string.length(); index++) {
            char c = string.charAt(index);
            if (level > 0 && '\n' == jsonFormatString.charAt(jsonFormatString.length() - 1)) {
                jsonFormatString.append(getJsonElementLevelString(level));
            }
            // 遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonFormatString.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonFormatString.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonFormatString.append("\n");
                    level--;
                    jsonFormatString.append(getJsonElementLevelString(level));
                    jsonFormatString.append(c);
                    break;
                default:
                    jsonFormatString.append(c);
                    break;
            }
        }
        return jsonFormatString.toString();
    }

    private static String getJsonElementLevelString(int level) {
        StringBuffer levelString = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelString.append("\t");
        }
        return levelString.toString();
    }

    public static String toStringWithSeperator(JSONObject json, String seperator) {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<?> iterator = json != null ? json.keys() : null;
        while (json != null && iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = json.opt(key);

            String string = null;
            if (value instanceof JSONObject) {
                string = toStringWithSeperator((JSONObject) value, seperator);
            } else if (value != null) {
                string = value.toString();
            }

            if (string != null) {
                stringBuilder.append(string);
                stringBuilder.append(seperator);
            }
        }

        String result = stringBuilder.toString();
        if (result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /*
     * convenient methods
     */

    public static void clearNullValue(JSONObject json) {
        Iterator<?> iterator = json != null ? json.keys() : null;
        while (json != null && iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = json.opt(key);
            if (value == null || value == JSONObject.NULL) {
                iterator.remove();
            }
        }
    }

    public static byte[] toBytes(JSONObject json) {
        String jsonString = json.toString();
        byte[] postBodyBytes = null;
        try {
            postBodyBytes = jsonString.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            postBodyBytes = jsonString.getBytes();
        }
        return postBodyBytes;
    }

    public static JSONObject create(Object... keysValues) {
        JSONObject json = new JSONObject();
        for (int i = 0; keysValues != null && i < keysValues.length; i = i + 2) {
            String key = (String) keysValues[i];
            Object value = (i + 1) < keysValues.length ? keysValues[i + 1] : null;
            try {
                json.put(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public static JSONObject createJSONObjectFromFile(String jsonFile) {
        String string = WuKongJSONUtil.readFileToText(jsonFile);
        JSONObject json = null;
        if (string != null) {
            json = createJSONObject(string);
        }
        return json;
    }

    public static JSONObject createJSONObject(String json) {
        return createJSONObject(json, null);
    }

    public static JSONObject createJSONObject(String json, JSONObject def) {
        if (json == null || json.isEmpty()) return def;
        JSONObject jsonObject = def;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static HashMap<String, Object> jsonToMap(JSONObject json) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        Iterator<?> iterator = json != null ? json.keys() : null;
        while (json != null && iterator != null && iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = json.opt(key);

            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = WuKongJSONArray.jsonToList((JSONArray) value);
            }

            result.put(key, value);
        }
        return result;
    }

    public static void putAll(JSONObject fromJson, JSONObject toJson) {
        if (fromJson == null || toJson == null) {
            return;
        }
        Iterator<String> iterator = fromJson.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                toJson.put(key, fromJson.opt(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void putJSONObject(JSONObject json, String key, Object value) {
        if (json == null) {
            return;
        }
        try {
            json.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void put(JSONObject json, Object... keysValues) {
        if (json == null) {
            return;
        }
        for (int i = 0; keysValues != null && i < keysValues.length; i = i + 2) {
            String key = (String) keysValues[i];
            Object value = (i + 1) < keysValues.length ? keysValues[i + 1] : null;
            try {
                json.put(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // put replaceJson element to source with the same structure, like merge but with depth
    public static void replaceJsonElementsValues(JSONObject sourceJson, JSONObject replaceJson) {
        if (sourceJson == null || replaceJson == null) {
            return;
        }

        Iterator<?> keys = replaceJson.keys();

        while (keys.hasNext()) {

            try {

                String replaceKey = (String) keys.next();
                Object elementReplace = replaceJson.opt(replaceKey);
                Object elementSource = sourceJson.opt(replaceKey);

                if ((elementSource instanceof JSONObject) && (elementReplace instanceof JSONObject)) {
                    WuKongJSONObject.replaceJsonElementsValues((JSONObject) elementSource, (JSONObject) elementReplace);
                } else if ((elementSource instanceof JSONArray) && (elementReplace instanceof JSONArray)) {
                    WuKongJSONArray.replaceJsonElementsValues((JSONArray) elementSource, (JSONArray) elementReplace);
                } else {
                    if (replaceKey.startsWith("delete-")) {
                        String deleteKey = replaceKey.replaceFirst("delete-", "");
                        Log.d(">>>>>", ">>>>>>>>>>>>>>> deleteKey " + deleteKey);
                        if (sourceJson.has(deleteKey)) {
                            sourceJson.remove(deleteKey);
                        }
                        // add if remove before
                        if (replaceJson.has(deleteKey)) {
                            sourceJson.put(deleteKey, replaceJson.opt(deleteKey));
                        }
                    } else {
                        // replace or add
                        sourceJson.put(replaceKey, elementReplace);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void mergeJSONObject(JSONObject destination, JSONObject source) {
        if (destination == null || source == null) {
            return;
        }
        Iterator<?> iteratorSource = source.keys();
        while (iteratorSource.hasNext()) {
            try {
                String name = (String) iteratorSource.next();
                Object value = source.opt(name);
                destination.put(name, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONObject getJSONWithKeyContains(JSONObject source, String[] contains) {
        if (source == null || contains == null) {
            return null;
        }
        JSONObject result = null;
        try {
            Iterator<?> iterator = source.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                boolean isContains = false;
                for (String str : contains) {
                    if (key.contains(str)) {
                        isContains = true;
                        break;
                    }
                }

                if (isContains) {
                    if (result == null) {
                        result = new JSONObject();
                    }
                    result.put(key, source.opt(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getJSONWithKeyPrefix(JSONObject source, String prefix) {
        if (source == null) {
            return null;
        }
        JSONObject result = null;
        Iterator<?> iterator = source.keys();
        while (iterator.hasNext()) {
            try {
                String key = (String) iterator.next();
                if (key.startsWith(prefix)) {
                    if (result == null) {
                        result = new JSONObject();
                    }
                    result.put(key, source.opt(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static JSONObject getJSONWithKeyPrefixs(JSONObject source, String[] prefixs) {
        if (source == null) {
            return null;
        }
        JSONObject result = null;
        try {
            Iterator<?> iterator = source.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                boolean isStartWith = false;
                for (int i = 0; i < prefixs.length; i++) {
                    String prefix = prefixs[i];
                    if (key.startsWith(prefix)) {
                        isStartWith = true;
                        break;
                    }
                }

                if (isStartWith) {
                    if (result == null) {
                        result = new JSONObject();
                    }
                    result.put(key, source.opt(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getJSONWithoutKeyPrefixs(JSONObject source, String[] prefixs) {
        if (source == null) {
            return null;
        }
        JSONObject result = null;
        try {
            Iterator<?> iterator = source.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                boolean isStartWith = false;
                for (int i = 0; i < prefixs.length; i++) {
                    String prefix = prefixs[i];
                    if (key.startsWith(prefix)) {
                        isStartWith = true;
                        break;
                    }
                }

                if (!isStartWith) {
                    if (result == null) {
                        result = new JSONObject();
                    }
                    result.put(key, source.opt(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONArray getValues(JSONObject json) {
        if (json == null) {
            return null;
        }
        JSONArray array = new JSONArray();
        Iterator<?> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Object value = json.opt(key);
            array.put(value);
        }
        return array;
    }

    /**
     * Remove
     */
    public static JSONObject removeWithKeyPrefix(JSONObject source, String prefix) {
        if (source == null) {
            return null;
        }
        JSONObject removedResult = null;
        try {
            Iterator<?> iterator = source.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key.startsWith(prefix)) {
                    if (removedResult == null) {
                        removedResult = new JSONObject();
                    }
                    removedResult.put(key, source.opt(key));
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removedResult;
    }

    public static JSONObject removeWithoutKeyPrefix(JSONObject source, String prefix) {
        if (source == null) {
            return null;
        }
        JSONObject removedResult = null;
        try {
            Iterator<?> iterator = source.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (!key.startsWith(prefix)) {
                    if (removedResult == null) {
                        removedResult = new JSONObject();
                    }
                    removedResult.put(key, source.opt(key));
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removedResult;
    }

    public static JSONObject removeWithKeyContains(JSONObject source, String containsString) {
        if (source == null) {
            return null;
        }
        JSONObject removedResult = null;
        try {
            Iterator<?> iterator = source.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key.contains(containsString)) {
                    if (removedResult == null) {
                        removedResult = new JSONObject();
                    }
                    removedResult.put(key, source.opt(key));
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removedResult;
    }

    // element is JSONObject
    public static Object removeJSONElementKey(JSONObject jsonObject, String elementKey, String removeKey) {
        if (jsonObject == null) {
            return null;
        }
        JSONObject element = jsonObject.optJSONObject(elementKey);
        if (element != null && element.has(removeKey)) {
            return element.remove(removeKey);
        }
        return null;
    }

    public static void removeAll(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        JSONArray names = jsonObject.names();
        if (names == null || names.length() == 0) {
            return;
        }
        for (int i = 0; i < names.length(); i++) {
            jsonObject.remove(names.optString(i));
        }
    }

    public static interface JNHandler {
        public boolean handle(String key, Object value);
    }

    public static void iterate(JSONObject jsonObject, JNHandler handler) {
        if (jsonObject == null || handler == null) {
            return;
        }
        Iterator<?> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (handler.handle(key, jsonObject.opt(key))) {
                break;
            }
        }
    }

}


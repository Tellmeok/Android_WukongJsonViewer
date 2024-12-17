package com.android.wukong.viewer.util;

public class WuKongSp {

    public WuKongPreferences impl;

    public WuKongSp(String xml) {
        impl = new WuKongPreferences(xml);
    }

    public WuKongSp(WuKongPreferences inner) {
        impl = inner;
    }

    public boolean contains(String key) {
        return impl.contains(key);
    }

    public void removeKey(String key) {
        impl.remove(key);
    }

    public void clear() {
        impl.clear();
    }

    public String getString(String key, String defValue) {
        return (String) impl.getValue(key, defValue);
    }

    public int getInt(String key, int defValue) {
        Object value = impl.getValue(key, defValue);
        return value != null ? (int) value : defValue;
    }

    public long getLong(String key, long defValue) {
        Object value = impl.getValue(key, defValue);
        return value != null ? (long) value : defValue;
    }

    public float getFloat(String key, float defValue) {
        Object value = impl.getValue(key, defValue);
        return value != null ? (float) value : defValue;
    }

    public double getDouble(String key, double defValue) {
        Object value = impl.getValue(key, defValue);
        return value != null ? (double) value : defValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        Object value = impl.getValue(key, defValue);
        return value != null ? (boolean) value : defValue;
    }

    public void setString(String key, String value) {
        impl.setValue(key, value);
    }

    public void setInt(String key, int value) {
        impl.setValue(key, value);
    }

    public void setLong(String key, long value) {
        impl.setValue(key, value);
    }

    public void setFloat(String key, float value) {
        impl.setValue(key, value);
    }

    public void setDouble(String key, double value) {
        impl.setValue(key, value);
    }

    public void setBoolean(String key, boolean value) {
        impl.setValue(key, value);
    }

    /**
     *
     */
    public Object getValue(String key) {
        return impl.getValue(key);
    }

    public Object getValue(String key, Object defValue) {
        return impl.getValue(key, defValue);
    }

    public void setValue(String key, Object value) {
        impl.setValue(key, value, false);
    }

    public void setValue(String key, Object value, boolean isNotifyObserver) {
        impl.setValue(key, value, isNotifyObserver);
    }

}

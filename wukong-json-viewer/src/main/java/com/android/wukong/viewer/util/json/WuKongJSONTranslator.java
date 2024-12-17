package com.android.wukong.viewer.util.json;

import android.os.Parcel;
import android.os.Parcelable;


import com.android.wukong.viewer.util.WuKongReflect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WuKongJSONTranslator {

    private static WuKongJSONTranslator instance = null;

    public static WuKongJSONTranslator getInstance() {
        if (instance == null) {
            instance = new WuKongJSONTranslator();
        }
        return instance;
    }

    public void setMapValuesToObjectFields(Object object, Map<String, Object> map) {
        if (object == null || map == null) {
            return;
        }
        Class<?> clazz = (object instanceof Class) ? (Class<?>) object : object.getClass();
        for (String name : map.keySet()) {
            Field field = WuKongReflect.searchField(clazz, name, 0, null);
            if (field == null) {
                continue;
            }
            Object value = map.get(name);
            try {
                field.set(object, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setJsonValuesToObjectFields(Object object, JSONObject json) {
        if (object == null || json == null) {
            return;
        }

        Class<?> clazz = (object instanceof Class) ? (Class<?>) object : object.getClass();

        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String name = keys.next();
            Field field = WuKongReflect.searchField(clazz, name, 3, null);
            if (field == null) {
                continue;
            }

            // try catch here, fieldValue is optional
            Object fieldValue = null;
            try {
                fieldValue = field.get(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Class<?> fieldType = field.getType();

            Object value = json.opt(name);

            if (value instanceof JSONObject) {
                setJsonValuesToObjectFields(fieldValue, (JSONObject) value);

            } else if (value instanceof JSONArray) {
                if (fieldType.isArray()) {
                    setJsonValuesToArrayField(fieldValue, (JSONArray) value);
                } else if (List.class.isAssignableFrom(fieldType)) {
                    setJsonValuesToListField(object, field, fieldType, (List<Object>) fieldValue, (JSONArray) value);
                }

            } else {

                // if is Enum, transfer String to Enum
                if (value instanceof String && fieldType.isEnum() && fieldValue != null) {
                    Class<?> enumClazz = fieldValue.getClass();
                    // or just use static method: java.lang.Enum.valueOf(enumClazz, value.toString());
                    value = WuKongReflect.invokeMethod(enumClazz, "valueOf", new Class[]{String.class}, new Object[]{value.toString()});

                } else if (value instanceof String && (fieldType == char.class || fieldType == Character.class)) {
                    char[] chars = ((String) value).toCharArray();
                    value = chars.length >= 1 ? chars[0] : value;

                } else if (value instanceof Number && fieldType.isPrimitive()) {
                    Object valueObj = WuKongReflect.parseValueOfNumber(fieldType, value);
                    if (valueObj != null && valueObj != value) {
                        value = valueObj;
                    }

                } else if (value instanceof String) {
                    Object valueObj = WuKongReflect.parseValueOfString(fieldType, value);
                    if (valueObj != null && valueObj != value) {
                        value = valueObj;
                    }
                }

                // set the value
                try {
                    field.set(object, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setJsonValuesToArrayField(Object array, JSONArray values) {
        if (array == null || values == null) {
            return;
        }
        if (!array.getClass().isArray()) {
            return;
        }

        Class<?> element_clazz = WuKongReflect.getElementClassForArray(array);
        if (element_clazz == null) {
            return;
        }

        int len = Array.getLength(array);
        int values_len = values.length();
        for (int index = 0; index < len && values_len > index; index++) {

            Object value = values.opt(index);

            if (element_clazz == char.class && value instanceof String) {
                char[] chars = ((String) value).toCharArray();
                ((char[]) array)[index] = chars.length >= 1 ? chars[0] : '\0';

            } else if (element_clazz == boolean.class && value instanceof Boolean) {
                ((boolean[]) array)[index] = (Boolean) value;

            } else if (value instanceof JSONObject) {
                Object nextObj = Array.get(array, index);
                setJsonValuesToObjectFields(nextObj, (JSONObject) value);

            } else {
                WuKongReflect.setValueOfNumber(array, index, element_clazz, value);

            }
        }
    }

    public void setJsonValuesToListField(Object object, Field field, Class<?> fieldType, List<Object> fieldValues, JSONArray json) {
        if (object == null || field == null) {
            return;
        }
        try {
            if (fieldValues == null) {
                if (fieldType.isInterface() || Modifier.isAbstract(fieldType.getModifiers())) {
                    fieldValues = new ArrayList<>();
                } else {
                    fieldValues = (List<Object>) fieldType.newInstance();
                }
                field.set(object, fieldValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fieldValues == null || json == null) {
            return;
        }

        Class<?> element_class = null;
        for (int i = 0; i < json.length(); i++) {
            Object value = json.opt(i);
            Object element = fieldValues.size() > i ? fieldValues.get(i) : null;

            // add
            if (element == null) {
                if (element_class == null) {
                    element_class = WuKongReflect.getFieldGenericElementType(field);
                }
                if (element_class != null) {
                    element = createInstanceForElement(element_class, value, object, field);
                }
                if (element != null) {
                    fieldValues.add(element);
                }
            }

            // modify
            if (element != null && value instanceof JSONObject) {
                setJsonValuesToObjectFields(element, (JSONObject) value);
            }
        }
    }

    public Object createInstanceForElement(Class<?> element_class, Object element_value, Object onObject, Field onField) {
        Object instance = WuKongReflect.newInstanceOf(element_class);
        if (instance == null && Parcelable.class.isAssignableFrom(element_class)) {
            try {
                Parcelable.Creator<?> CREATOR = (Parcelable.Creator<?>) WuKongReflect.getFieldValue(element_class, "CREATOR");
                Parcel parcel = Parcel.obtain();
                Parcelable parcelableObj = (Parcelable) CREATOR.createFromParcel(parcel);
                parcel.recycle();
                instance = parcelableObj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

}

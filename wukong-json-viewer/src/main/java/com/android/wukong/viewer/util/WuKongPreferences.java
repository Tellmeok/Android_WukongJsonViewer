package com.android.wukong.viewer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.android.wukong.viewer.WuKongApi;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class WuKongPreferences {

    private String xmlFileName = null;

    private boolean isDefaultNotifyObserver = false;

    public interface PreferenceObserver {
        void updated(String key, Object oldValue, Object newValue);
    }

    private final ArrayList<PreferenceObserver> kUpdatedObservers = new ArrayList<>();

    public WuKongPreferences(String fileName) {
        xmlFileName = fileName;
        isDefaultNotifyObserver = false;
    }

    public WuKongPreferences(String fileName, boolean isNotifyObserver) {
        xmlFileName = fileName;
        isDefaultNotifyObserver = isNotifyObserver;
    }

    public String getXmlFileName() {
        return xmlFileName;
    }

    public void addUpdatedObserver(PreferenceObserver callback) {
        if (!isContainsObserver(callback)) {
            kUpdatedObservers.add(callback);
        }
    }

    public void removeUpdatedObserver(PreferenceObserver callback) {
        kUpdatedObservers.remove(callback);
    }

    public boolean isContainsObserver(PreferenceObserver callback) {
        return kUpdatedObservers.contains(callback);
    }

    public SharedPreferences getInnerSharedPreferences() {
        return WuKongApi.getApplication().getSharedPreferences(xmlFileName, Context.MODE_PRIVATE);
    }

    public void setValue(String key, Object value) {
        setValue(key, value, isDefaultNotifyObserver);
    }

    public void setValue(String key, Object value, boolean isNotifyObserver) {
        if (isNotifyObserver) {
            final String fKey = key;
            final Object fValue = value;
            final Object fOldValue = getValue(key, null);
            new android.os.Handler(Looper.getMainLooper()).post(() -> {
                for (PreferenceObserver callback : kUpdatedObservers) {
                    callback.updated(fKey, fOldValue, fValue);
                }
            });
        }

        SharedPreferences.Editor editor = getInnerSharedPreferences().edit();
        if (value == null) {
            editor.remove(key);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer || value.getClass() == int.class) {
            editor.putInt(key, (int) value);
        } else if (value instanceof Long || value.getClass() == long.class) {
            editor.putLong(key, (long) value);
        } else if (value instanceof Float || value.getClass() == float.class) {
            editor.putFloat(key, (float) value);
        } else if (value instanceof Boolean || value.getClass() == boolean.class) {
            editor.putBoolean(key, (boolean) value);
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        }
        editor.apply();
    }

    public Object getValue(String key) {
        return getInnerSharedPreferences().getAll().get(key);
    }

    public Object getValue(String key, Object defValue) {
        Object value = getInnerSharedPreferences().getAll().get(key);
        return value == null ? defValue : value;
    }

    public boolean contains(String key) {
        return getInnerSharedPreferences().contains(key);
    }

    public void remove(String key) {
        getInnerSharedPreferences().edit().remove(key).apply();
    }

    public void clear() {
        getInnerSharedPreferences().edit().clear().apply();
    }

    /**
     * Encrypt & Decrypt
     */

    public void encryptValue(String key, String value) {
        String md5 = getMd5(key);
        String aesKey = md5 != null ? md5.substring(0, 16) : defaultAESKey;
        encryptValue(key, value, aesKey);
    }

    public void encryptValue(String key, String value, String __aes_key__) {
        setValue(key, Encrypt(value, __aes_key__));
    }

    public String decryptValue(String key) {
        String md5 = getMd5(key);
        String aesKey = md5 != null ? md5.substring(0, 16) : defaultAESKey;
        return decryptValue(key, aesKey);
    }

    public String decryptValue(String key, String __aes_key__) {
        return Decrypt((String) getValue(key, ""), __aes_key__);
    }

    /**
     * Classes static members
     */

    public static WuKongPreferences Global = new WuKongPreferences(WuKongApi.getPackageName() + "_preferences"); // [package_name]_preferences.xml
    public static WuKongPreferences User = new WuKongPreferences("user_preferences");          // user_preferences.xml
    public static WuKongPreferences Temp = new WuKongPreferences("temp_preferences");          // temp_preferences.xml

    private static WuKongPreferences instance = null;     // [package_name].xml

    public static WuKongPreferences getInstance() {
        if (instance == null) {
            instance = new WuKongPreferences(WuKongApi.getPackageName(), true);
        }
        return instance;
    }

    /**
     * Observers
     */

    public static void addPreferenceUpdatedObserver(PreferenceObserver callback) {
        getInstance().addUpdatedObserver(callback);
    }

    public static void removePreferenceUpdatedObserver(PreferenceObserver callback) {
        getInstance().removeUpdatedObserver(callback);
    }

    /**
     * Get & Set
     */
    public static void set(String key, Object value) {
        getInstance().setValue(key, value, getInstance().isDefaultNotifyObserver);
    }

    public static void set(String key, Object value, boolean isNotifyObserver) {
        getInstance().setValue(key, value, isNotifyObserver);
    }

    public static Object get(String key) {
        return getInstance().getValue(key);
    }

    public static Object get(String key, Object defValue) {
        return getInstance().getValue(key, defValue);
    }

    /**
     * Get & Set with specified file name
     */
    public static void setWithFile(String fileName, String key, Object value) {
        WuKongPreferences preferences = (fileName != null && !fileName.isEmpty()) ? new WuKongPreferences(fileName) : getInstance();
        preferences.setValue(key, value, preferences.isDefaultNotifyObserver);
    }

    public static Object getWithFile(String fileName, String key, Object defValue) {
        WuKongPreferences preferences = (fileName != null && !fileName.isEmpty()) ? new WuKongPreferences(fileName) : getInstance();
        return preferences.getValue(key, defValue);
    }

    /**
     * Encrypt & Decrypt
     */
    public static void encrypt(String key, String value) {
        getInstance().encryptValue(key, value);
    }

    public static void encrypt(String key, String value, String __aes_key__) {
        getInstance().encryptValue(key, value, __aes_key__);
    }

    public static String decrypt(String key) {
        return getInstance().decryptValue(key);
    }

    public static String decrypt(String key, String __aes_key__) {
        return getInstance().decryptValue(key, __aes_key__);
    }

    /**
     * Encrypt & Decrypt
     */
    public static String getMd5(@NonNull String plainText) {
        return getMd5(plainText.getBytes());
    }

    public static String getMd5(@NonNull byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte b[] = md.digest();
            int i;
            StringBuffer buffer = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buffer.append("0");
                }
                buffer.append(Integer.toHexString(i));
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String defaultAESKey = "__AES_KEY__000__";

    private static String aesAlgorithm = "AES";

    private static String aesTransformation = "AES/ECB/PKCS5Padding"; //"算法/模式/补码方式"

    private static String filling = "00000000000000000000000000000000";

    // 加密
    public static String Encrypt(String sSrc, String sKey) {
        byte[] encrypted = Encrypt2Bytes(sSrc, sKey);
        if (encrypted == null) return null;
        return android.util.Base64.encodeToString(encrypted, 0);
    }

    public static byte[] Encrypt2Bytes(String sSrc, String sKey) {
        if (sSrc == null) return null;
        return Encrypt2Bytes(sSrc.getBytes(), sKey);
    }

    public static byte[] Encrypt2Bytes(byte[] bytes, String sKey) {
        if (sKey == null || sKey.isEmpty()) return null;
        if (sKey.length() != 16) {
            sKey = (sKey + filling).substring(0, 16);
        }
        return Encrypt2Bytes(bytes, sKey.getBytes());
    }

    public static byte[] Encrypt2Bytes(byte[] bytes, byte[] keys) {
        try {
            if (bytes == null || bytes.length == 0) return null;
            SecretKeySpec skeySpec = new SecretKeySpec(keys, aesAlgorithm);
            Cipher cipher = Cipher.getInstance(aesTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // 解密
    public static String Decrypt(String sSrc, String sKey) {
        byte[] original = Decrypt2Bytes(sSrc, sKey);
        if (original == null) return null;
        return new String(original);
    }

    public static byte[] Decrypt2Bytes(String sSrc, String sKey) {
        byte[] encrypted = android.util.Base64.decode(sSrc, 0);
        return Decrypt2Bytes(encrypted, sKey);
    }

    public static byte[] Decrypt2Bytes(byte[] encrypted, String sKey) {
        if (sKey == null || sKey.isEmpty()) return null;
        if (sKey.length() != 16) {
            sKey = (sKey + filling).substring(0, 16);
        }
        return Decrypt2Bytes(encrypted, sKey.getBytes());
    }

    public static byte[] Decrypt2Bytes(byte[] encrypted, byte[] keys) {
        try {
            if (encrypted == null || encrypted.length == 0) return null;
            SecretKeySpec skeySpec = new SecretKeySpec(keys, aesAlgorithm);
            Cipher cipher = Cipher.getInstance(aesTransformation);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return cipher.doFinal(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

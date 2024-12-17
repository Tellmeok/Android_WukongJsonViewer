package com.android.wukong.viewer.util;

import android.content.res.AssetManager;

import com.android.wukong.viewer.WuKongApi;
import com.android.wukong.viewer.util.json.WuKongJSONArray;
import com.android.wukong.viewer.util.json.WuKongJSONObject;
import com.android.wukong.viewer.util.json.WuKongJSONUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;


public class WuKongAssetser {

    public static Object getAssetsAsJson(String assetsItemPath) {
        Object json = getAssetsAsJsonObject(assetsItemPath);
        if (json == null) {
            json = getAssetsAsJsonArray(assetsItemPath);
        }
        return json;
    }

    public static JSONObject getAssetsAsJsonObject(String assetsItemPath) {
        try {
            return WuKongJSONObject.createJSONObject(getAssetsAsString(assetsItemPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getAssetsAsJsonArray(String assetsItemPath) {
        try {
            return WuKongJSONArray.createJSONArray(getAssetsAsString(assetsItemPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAssetsAsString(String assetsItemPath) {
        try {
            return WuKongJSONUtil.readStreamToText(getAssetsAsStream(assetsItemPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getAssetsAsStream(String assetsItemPath) {
        try {
            AssetManager assetManager = WuKongApi.getApplication().getAssets();
            File fileInAssets = new File(assetsItemPath);
            File parent = fileInAssets.getParentFile();
            if (parent == null) {
                return null;
            }
            String assetsDir = parent.getAbsolutePath().trim();
            if (assetsDir.startsWith("/")) {
                assetsDir = assetsDir.substring(1);
            }
            if (Arrays.asList(assetManager.list(assetsDir)).contains(fileInAssets.getName())) {
                return assetManager.open(assetsItemPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

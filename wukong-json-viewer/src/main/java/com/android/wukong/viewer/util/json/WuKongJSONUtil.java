package com.android.wukong.viewer.util.json;

import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WuKongJSONUtil {

    /**
     * READ
     */

    public static String readFileToText(String fileName) {
        if (fileName == null || !new File(fileName).exists()) {
            return null;
        }
        String result = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int length = -1;
            // do not use new File(fileName).length to init a buffer. /proc/PID/status file length is 0, but it has contents
            byte[] buffer = new byte[1024 * 100];
            while ((length = dataInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            dataInputStream.close();

            byte[] bytes = outputStream.toByteArray();
            outputStream.close();
            result = new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String readStreamToText(InputStream inputStream) {
        if (inputStream == null) return null;
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            int length = -1;
            byte[] buffer = new byte[8 * 1024];
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            byte[] bytes = outputStream.toByteArray();
            String result = new String(bytes, "UTF-8");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * WRITE
     */

    public static boolean writeTextToFile(String text, String fileName) {
        boolean result = false;
        FileWriter fileWriter = null;
        try {
            File fileText = new File(fileName);
            File fileParent = fileText.getParentFile();
            if (fileParent != null && !fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!fileText.exists()) {
                fileText.createNewFile();
            }
            fileWriter = new FileWriter(fileText, false);
            fileWriter.write(text);
            fileWriter.flush();
            fileWriter.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Others
     */

    public static interface Action {
        public boolean execute(int retryIndex);
    }

    public static void retryInMainThread(final long retrySleep, final int retryCount, final Action action) {
        final int[] doCounts = new int[]{0};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                boolean result = false;
                Exception exception = null;
                try {
                    result = action.execute(doCounts[0]);
                } catch (Exception e) {
                    exception = e;
                }
                if (result || exception != null) {
                    return;
                }

                doCounts[0] = doCounts[0] + 1;
                if (doCounts[0] >= retryCount) {
                    return;
                }
                new android.os.Handler(Looper.getMainLooper()).postDelayed(this, retrySleep);
            }
        };

        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (isMainThread) {
            runnable.run();
        } else {
            new android.os.Handler(Looper.getMainLooper()).post(runnable);
        }
    }
}

package com.sdk.dyq.mapmodule;

import android.app.Application;
import android.content.Context;

import java.io.File;

/**
 * Created by yuanqiang on 2017/5/15.
 */

public class MapApp extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        makeDirs(Config.PATH_SENSOR_GPS);
    }
    public static Context getContext() {
        return context;
    }

    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }
    public static String getFolderName(String filePath) {

        if (isEmpty(filePath)) {
            return filePath;
        }

        int filePos = filePath.lastIndexOf(File.separator);
        return (filePos == -1) ? "" : filePath.substring(0, filePos);
    }
    public static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }
}

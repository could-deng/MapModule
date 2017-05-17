package com.sdk.dyq.mapmodule;

import android.os.Environment;

import java.io.File;

/**
 * Created by yuanqiang on 2017/5/15.
 */

public class Config {

    /** 根文件夹名称*/
    public static final String PRODUCT_DIR = "MapModule";
    /** 根文件夹路径*/
    public static final String PATH_APP_STORAGE = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + PRODUCT_DIR + File.separator;
    /** GPS记录文件路径*/
    public static final String PATH_SENSOR_GPS = PATH_APP_STORAGE + "SensorGps" +File.separator;

    //region ================================ 地图定位相关 ================================
    /**定位类型,未知*/
    public static final int LOCATION_TYPE_NONE = 0;
    /**定位类型,GPS定位*/
    public static final int LOCATION_TYPE_GPS = 1;
    /**定位类型,网络定位*/
    public static final int LOCATION_TYPE_LBS = 2;
    /**GPS定位点可接受的最小精度,用于过滤GPS点*/
    public static final int GPS_AVAILABLE_ACCURACY = 80;
    /**地图显示时镜头放大倍数*/
    public static final int MAP_CAMERA_ZOOM = 16;
    /**地图显示时镜头最大放大倍数*/
    public static final int MAP_CAMERA_ZOOM_MAX = 16;
    /**地图显示时镜头最小放大倍数*/
    public static final int MAP_CAMERA_ZOOM_MIN = 12;
    /**地图画轨迹时,定位精度大于60米画虚线*/
    public static final int GPS_ACCURACY_DOT = 60;
    //endregion ================================ 地图定位相关 ================================
}

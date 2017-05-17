package com.sdk.dyq.mapmodule.common;

import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import com.sdk.dyq.mapmodule.common.bean.TrailInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuanqiang on 2017/5/15.
 */

public class JSonHelper {
    /**
     * 以流的方式解析跑步轨迹文件
     *
     * @param fileName 轨迹文件绝对路径名
     * @return 轨迹点集合
     */
    public static List<TrailInfo> readRunTrailFile(String fileName) {
        List<TrailInfo> trailInfoList = new ArrayList<>();
        if (TextUtils.isEmpty(fileName)) {
            return trailInfoList;
        }
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return trailInfoList;
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            JsonReader reader = new JsonReader(new InputStreamReader(fileInputStream, "UTF-8"));
            new JsonReader(new InputStreamReader())
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name != null && name.equals("array")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        TrailInfo trailInfo = new TrailInfo();
                        while (reader.hasNext()) {
                            String fieldName = reader.nextName();
                            if (fieldName != null) {
                                if (fieldName.equalsIgnoreCase("accurace") || fieldName.equalsIgnoreCase("accuracy")) {//精度
                                    trailInfo.setAccuracy(reader.nextDouble());
                                } else if (fieldName.equalsIgnoreCase("time")) {//时间戳
                                    trailInfo.setTime(reader.nextLong());
                                } else if (fieldName.equalsIgnoreCase("sportState")) {//运动类型,0:运动中,1:运动暂停
                                    try {
                                        trailInfo.setSportState(reader.nextInt());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        trailInfo.setSportState(reader.nextBoolean() ? 1 : 0);//V1.0老版本
                                    }
                                } else if (fieldName.equalsIgnoreCase("speed")) {//速度,单位米/秒
                                    try {
                                        trailInfo.setSpeed(Float.parseFloat(reader.nextString()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (fieldName.equalsIgnoreCase("altitude")) {//海拔,单位米
                                    trailInfo.setAltitude(reader.nextDouble());
                                } else if (fieldName.equalsIgnoreCase("bearing")) {//角度
                                    try {
                                        trailInfo.setBearing(Float.parseFloat(reader.nextString()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (fieldName.equalsIgnoreCase("lng")) {//经度
                                    trailInfo.setLng(reader.nextDouble());
                                } else if (fieldName.equalsIgnoreCase("type")) {//定位类型,0:GPS,1:LBS
                                    trailInfo.setType(reader.nextInt());
                                } else if (fieldName.equalsIgnoreCase("used")) {//是否使用(显示在地图上)V2.0.4版本开始废弃,true:是,false:否
                                    try {
                                        trailInfo.setUsed(reader.nextBoolean());
                                    } catch (Exception e) {
                                        trailInfo.setUsed(reader.nextInt() == 1);
                                    }
                                } else if (fieldName.equalsIgnoreCase("lat")) {//纬度
                                    trailInfo.setLat(reader.nextDouble());
                                }
                            }
                        }
                        reader.endObject();
                        trailInfoList.add(trailInfo);
                    }//array
                    reader.endArray();
                }
            }//object
            reader.endObject();
            reader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            String error = e.getMessage();
            if (!TextUtils.isEmpty(error)) {
                Log.i("TT", "readRunTrailFile error:" + error);
            }
        }
        return trailInfoList;
    }
}

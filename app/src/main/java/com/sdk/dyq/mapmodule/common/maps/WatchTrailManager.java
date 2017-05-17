package com.sdk.dyq.mapmodule.common.maps;


import com.sdk.dyq.mapmodule.common.bean.TrailInfo;

/**
 * 轨迹帮助类
 */
public class WatchTrailManager {

    private static WatchTrailManager instance;
    private double dDistance;//由定位轨迹计算出的总距离
    private int iPointUsed;//在地图上显示用的轨迹点数

//    	private TrailInfo mStartPoint;//起始点
    private TrailInfo mLastUsedPoint;//最后一个用于显示在地图上的点
    private TrailInfo mLastUsedSecondPoint;//倒数第二个用于显示在地图上的点

    public WatchTrailManager() {
        clear();
    }

    public static WatchTrailManager getInstance() {
        if (instance == null) {
            instance = new WatchTrailManager();
        }
        return instance;
    }

    private void clear() {
        dDistance = 0;
        iPointUsed = 0;
        mLastUsedSecondPoint = null;
        mLastUsedPoint = null;
    }

    /**
     * 获取可显示在地图上的轨迹点数
     */
    public int getPointUsed() {
        return iPointUsed;
    }

    /**
     * 获取由定位轨迹计算出的距离
     *
     * @return 由定位轨迹计算出的距离
     */
    public int getDistance() {
        return (int) dDistance;
    }

    /**
     * 根据经纬度计算两点距离
     *
     * @param lon1 第一点经度
     * @param lat1 第一点纬度
     * @param lon2 第二点经度
     * @param lat2 第二点纬度
     * @return 两点之间的距离, 单位米
     */
    public static double getShortDistance(double lon1, double lat1, double lon2,double lat2) {
        final double DEF_PI = 3.14159265359;
        final double DEF_2PI = 6.28318530712;
        final double DEF_PI180 = 0.01745329252;
        final double DEF_R = 6370693.5; // radius of earth

        double ew1, ns1, ew2, ns2;
        double dx, dy, dew;
        double distance;
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        dew = ew1 - ew2;
        if (dew > DEF_PI)
            dew = DEF_2PI - dew;
        else if (dew < -DEF_PI)
            dew = DEF_2PI + dew;
        dx = DEF_R * Math.cos(ns1) * dew;
        dy = DEF_R * (ns1 - ns2);
        distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    public void releaseResource() {
        clear();
    }

    /**
     * 分析轨迹点,并计算GPS运动距离
     *
     * @param info 轨迹点信息
     */
    public void addTrailInfoToArray(TrailInfo info) {
        // 1.判断轨迹点信息,不使用的点不处理
        if (info == null)
            return;

        // 2.更新
        iPointUsed++;
        mLastUsedSecondPoint = mLastUsedPoint;
        mLastUsedPoint = info;
        // 3.运动暂停时,不计算GPS距离
//        if (info.getSportState() != TrailInfo.SPORT_STATE_SPORT) {
//            mLastUsedSecondPoint = null;
//            mLastUsedPoint = null;
//            return;
//        }
        // 4.根据最近使用的两个轨迹点经纬度计算两点之间的距离,并将结果加到由定位轨迹计算出的总距离中
        if (getLastUsedSecondPoint() == null)
            return;
        dDistance += getShortDistance(getLastUsedPoint().getLng(),
                getLastUsedPoint().getLat(), getLastUsedSecondPoint().getLng(),
                getLastUsedSecondPoint().getLat());
//        Log.i("TT","addTrailInfoToArray calculator dDistance:"+dDistance);
    }

    /**
     * 获取最近定位路段的平均速度,单位为米/秒
     */
    public double getLastSpeed() {
        if ((getLastUsedPoint() == null) || (getLastUsedSecondPoint() == null)
                || (getLastUsedPoint() == getLastUsedSecondPoint()))
            return 0;

        double distance = getShortDistance(getLastUsedPoint().getLng(),
                getLastUsedPoint().getLat(), getLastUsedSecondPoint().getLng(),
                getLastUsedSecondPoint().getLat());
//        long time = getLastUsedPoint().getTime() - getLastUsedSecondPoint().getTime();
        long time =2000;//todo 暂时时间间隔写死，为2秒
        if (time <= 0)
            return 0;
        return distance * 1000 / time;
    }

    /**
     * 获取最近一次有效的定位点
     */
    public TrailInfo getLastUsedPoint() {
        return mLastUsedPoint;
    }

    /**
     * 获取上上一次有效的定位点
     */
    public TrailInfo getLastUsedSecondPoint() {
        return mLastUsedSecondPoint;
    }

}

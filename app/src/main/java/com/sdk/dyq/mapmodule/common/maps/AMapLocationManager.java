package com.sdk.dyq.mapmodule.common.maps;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.sdk.dyq.mapmodule.Config;
import com.sdk.dyq.mapmodule.MapApp;

/**
 * 高德定位管理,国内自动加了纠偏,国外未纠偏
 * */
public class AMapLocationManager {

	private AMapLocationClient mLocationClient;//高德定位客户端
    private AMapLocationClientOption mLocationOption;//高德定位参数配置
    private final long LOCATION_MIN_INTERVAL = 1000;//定位更新的最小时间间隔,单位为毫秒.

	private OnAMapLocationChangeListener locationChangeListener;//高德定位更新监听回调
	private AMapLocation mLastLocationInfo;//高德最后一次定位信息

    /**
     * 高德定位回调监听
     * */
    private AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation aLocation) {
            if (aLocation == null)
                return;
//			Log.i("TT", "AMapLocationManager-->AMapLocationListener aLocation:" + aLocation);
			//1.只接收GPS和网络定位结果
            switch (aLocation.getLocationType()){//定位类型
                case AMapLocation.LOCATION_TYPE_OFFLINE://离线定位结果
                case AMapLocation.LOCATION_TYPE_SAME_REQ://前次定位结果,网络定位请求低于1秒、或两次定位之间设备位置变化非常小时返回,设备位移通过传感器感知
                    // 直接返回不重复处理
                    return;

                case AMapLocation.LOCATION_TYPE_GPS://GPS定位类型,通过设备GPS定位模块返回的定位结果
                    break;

                case AMapLocation.LOCATION_TYPE_FIX_CACHE://缓存定位结果,返回一段时间前设备在相同的环境中缓存下来的网络定位结果,节省无必要的设备定位消耗
                case AMapLocation.LOCATION_TYPE_WIFI://Wifi定位结果,属于网络定位,定位精度相对基站定位会更好
                case AMapLocation.LOCATION_TYPE_CELL://基站定位结果,属于网络定位
                    break;

            }
			//2.过滤0,0点
            if(aLocation.getLongitude() == 0 &&
                    aLocation.getLatitude() == 0){
                return;
            }

			//3.通知定位回调监听（高德）
            if (locationChangeListener != null) {
				locationChangeListener.onLocationChange(aLocation);
			}
			//4.更新最后一次定位（高德）
            mLastLocationInfo = aLocation;
        }
    };


	/**
	 * 高德定位更新监听回调接口
	 * */
	public interface OnAMapLocationChangeListener {
		/**
		 * 定位更新监听
		 * @param aLocation 新的定位信息
		 * */
		void onLocationChange(AMapLocation aLocation);
	}

	/**
	 * 设置定位更新监听回调
	 * @param listener 定位更新监听回调
	 * */
	public void setOnAMapLocationChangeListener(
			OnAMapLocationChangeListener listener) {
		locationChangeListener = listener;
	}

	/**
	 * 获取定位更新监听回调
	 * @return 定位更新监听回调
	 * */
	public OnAMapLocationChangeListener getOnAMapLocationChangeListener() {
		return locationChangeListener;
	}

	public AMapLocationManager() {
		clear();
	}

	private void clear() {
		mLastLocationInfo = null;
	}

    /**
     * 获取最后一次定位信息
     * */
	public AMapLocation getLastLocationInfo() {
		return mLastLocationInfo;
	}

    /**
     * 获取GPS强度
     *
     * @return [0,4] 数值越大表示信号越好
     * */
	public int getGpsSignalLevel() {
		if (mLastLocationInfo == null)
			return 0;

		int accuracy = (int) mLastLocationInfo.getAccuracy();
		if (accuracy < 0)
			return 0;

        //根据定位精度计算GPS强度
        int level = accuracy / 10;
		if (mLastLocationInfo.getLatitude() == 0
				&& mLastLocationInfo.getLongitude() == 0 && accuracy == 0)
			level = 0;
		else if (level >= 10)
			level = 0;
		else if (level > 4)
			level = 1;
		else
			level = 5 - level;
		return level;
	}

    /** 是否已经有定位信息*/
	public boolean isLocated() {
		return (mLastLocationInfo != null);
	}

    /**
     * 开始定位
     *
     * @param context 上下文
     * */
	private void startLocate(Context context) {
		if (context == null)
			return;
        //1.初始化定位
        mLocationClient = new AMapLocationClient(context);

        //2.设置定位参数
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 设置定位模式为高精度模式,优先返回精度高的定位结果
//        mLocationOption.setOnceLocation(false);//持续定位模式,注意:该选项在stopLocation后还会不断定位
        mLocationOption.setInterval(LOCATION_MIN_INTERVAL);//定位周期频率
        mLocationOption.setNeedAddress(true);//需要显示地址信息

        //3.设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //4.启动定位
        mLocationClient.startLocation();

	}

    /** 开始定位*/
	public void startLocate() {
		startLocate(MapApp.getContext());
	}

    /** 停止定位*/
	public void stopLocate() {
        if (mLocationClient != null) {//停止定位
//            Log.i("TT","stopLocation-->stopLocation");
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
			mLocationListener = null;
        }
	}

    /**
     * 获取定位类型
     *
     * @return 1:GPS定位类型,2:LBS定位类型
     * */
	public int getGPSLocateType() {

		int iLocateType = Config.LOCATION_TYPE_NONE;
		if (mLastLocationInfo == null)
			return iLocateType;
		//根据语言包选择地图类型
		String provider = mLastLocationInfo.getProvider();
		if (provider == null || provider.isEmpty())
			return iLocateType;
		if (provider.equalsIgnoreCase("gps"))
			iLocateType = Config.LOCATION_TYPE_GPS;
		else if (provider.equalsIgnoreCase("lbs"))
			iLocateType = Config.LOCATION_TYPE_LBS;
		else
			iLocateType = Config.LOCATION_TYPE_LBS;

		return iLocateType;
	}

    /**
     * 获取指定点的定位类型
     *
     * @param location 指定定位点
     *
     * @return 1:GPS定位类型,2:LBS定位类型
     * */
    public int getGPSLocateType(AMapLocation location) {

        int iLocateType = Config.LOCATION_TYPE_NONE;
        if (location == null)
            return iLocateType;
        String provider = location.getProvider();
        if (provider == null || provider.isEmpty())
            return iLocateType;
        if (provider.equalsIgnoreCase("gps"))
            iLocateType = Config.LOCATION_TYPE_GPS;
        else if (provider.equalsIgnoreCase("lbs"))
            iLocateType = Config.LOCATION_TYPE_LBS;
        else
            iLocateType = Config.LOCATION_TYPE_LBS;

        return iLocateType;
    }

    //region ============================V2.0.4版本之前调试用的过滤方法=================================
//    /**
//     * 根据轨迹点used字段过滤
//     *
//     * @param info 要判断的轨迹点
//     *
//     * @return true:过滤,false:不过滤
//     * */
//	private boolean filterByUsedFlag(TrailInfo info){
//		if(info == null)return true;
//		return !info.getUsed();
//	}

//    /**
//     * 过滤轨迹点,在V2.0.4版本之前由于保存了所有的定位点,所以需要过滤,V2.0.4之后不再需要,为了程序兼容性仍调用
//     *
//     * @param list 轨迹点集合
//     * @param method 默认值用1,即根据轨迹点字段used=true来过滤,其它取值如(4,8,10,20,40)用于调试
//     *
//     * */
//    public void filterTrailList(List<TrailInfo> list, int method) {
//        if (list == null || (list.size() <= 0))
//            return;
//        if(list.get(0) == null)return;
//
//        boolean bFilterUsed = ((method & 0x1) != 0);
//        TrailInfo nowTrail;
//        for (int i = 0; i < list.size();i++) {
//            if(list.get(i) == null){
//                continue;
//            }
//            nowTrail = list.get(i);
//
//            boolean bFilter = false;
//            if(bFilterUsed)bFilter = filterByUsedFlag(nowTrail);
//            if (bFilter) {
//                list.remove(i);
//                i--;//移除一个元素后把i减1,使所有的元素都被遍历到
//            }
//        }
//    }

    //endregion ============================V2.0.4版本之前调试用的过滤方法=================================

}

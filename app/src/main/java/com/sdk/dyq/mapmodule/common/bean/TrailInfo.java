package com.sdk.dyq.mapmodule.common.bean;

/**
 * 轨迹点信息
 * */
public class TrailInfo {
    /**运动中*/
	public static final int SPORT_STATE_SPORT = 0;
    /**运动暂停状态*/
	public static final int SPORT_STATE_PAUSE = 1;

	private long time;//时间戳
	private double lat;//纬度
	private double lng;//经度
	private double accuracy;//精度,单位米
	private double altitude;//海拔,单位米
	private float bearing;//角度
	private float speed;//速度,单位米/秒
	private int type;//定位类型,0:GPS,1:LBS
	private int sportState;//运动类型,0:运动中,1:运动暂停
	private boolean used;//是否使用(显示在地图上),true:是,false:否
	private int color;//轨迹点颜色

	public TrailInfo() {
        time = 0;
        lat = 0;
        lng = 0;
        accuracy = 0;
        altitude = 0;
        bearing = 0;
        speed = 0;
        type = 0;
        sportState = SPORT_STATE_SPORT;
//        used = true;
	}

    /**
     * 创建轨迹点
     *
     * @param time 轨迹点时间戳
     * @param lat  纬度
     * @param lng  经度
     * @param sportState 运动类型,0:运动中TrailInfo.SPORT_STATE_SPORT ,1:运动暂停TrailInfo.SPORT_STATE_PAUSE
     * */
	public TrailInfo(long time, double lat, double lng, int sportState) {
		this.time = time;
		this.lat = lat;
		this.lng = lng;
		this.sportState = sportState;
	}

	/**
	 * 创建轨迹点
	 *
	 * @param time 轨迹点时间戳
	 * @param lat  纬度
	 * @param lng  经度
	 * @param sportState 运动类型,0:运动中TrailInfo.SPORT_STATE_SPORT ,1:运动暂停TrailInfo.SPORT_STATE_PAUSE
	 * @param used 是否使用(显示在地图上),true:是,false:否
	 * */
	public TrailInfo(long time, double lat, double lng, int sportState, boolean used) {
		this.time = time;
		this.lat = lat;
		this.lng = lng;
		this.sportState = sportState;
		this.used = used;
	}

	/**
	 *  复制创建轨迹点
	 *
	 * @param other 要复制的轨迹点
	 *
	 * */
	public TrailInfo(TrailInfo other){
		if(other == null)
			return;
		setTime(other.getTime());
		setAccuracy(other.getAccuracy());
		setUsed(other.getUsed());
		setLat(other.getLat());
		setLng(other.getLng());
		setType(other.getType());
		setSportState(other.getSportState());
		setSpeed(other.getSpeed());
		setBearing(other.getBearing());
		setAltitude(other.getAltitude());
	}

    /**
     * 获取轨迹点定位类型
     * @return  0:GPS,1:LBS
     * */
	public int getType() {
		return type;
	}

    /**
     * 设置轨迹点定位类型
     * @param type 0:GPS,1:LBS
     * */
	public void setType(int type) {
		this.type = type;
	}

    /**
     * 获取轨迹点运动类型
     *
     * @return  0:运动中,1:运动暂停
     * */
	public int getSportState() {
		return sportState;
	}

    /**
     * 设置轨迹点运动类型
     * @param sportState 运动类型,0:运动中TrailInfo.SPORT_STATE_SPORT ,1:运动暂停TrailInfo.SPORT_STATE_PAUSE
     * */
	public void setSportState(int sportState) {
		this.sportState = sportState;
	}

    /**
     * 设置轨迹点是否使用(显示在地图上)
     * @param used true:是,false:否
     * */
    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * 获取轨迹点是否使用(显示在地图上)
     * @return true:是,false:否
     * */
    public boolean getUsed() {
        return used;
    }

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}
	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getBearing() {
		return bearing;
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	/**
	 * 获取轨迹点颜色
	 * */
	public int getColor() {
		return color;
	}

	/**
	 * 设置轨迹点颜色,用于轨迹线着色
	 *
	 * @param color 颜色值
	 * */
	public void setColor(int color) {
		this.color = color;
	}

}

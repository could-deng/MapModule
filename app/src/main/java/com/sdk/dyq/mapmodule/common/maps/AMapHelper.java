package com.sdk.dyq.mapmodule.common.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.sdk.dyq.mapmodule.Config;
import com.sdk.dyq.mapmodule.MapApp;
import com.sdk.dyq.mapmodule.R;
import com.sdk.dyq.mapmodule.common.bean.PointWithColor;
import com.sdk.dyq.mapmodule.common.bean.TrailInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 高德地图帮助类,负责地图显示,画轨迹,画标志等
 */
public class AMapHelper {
    private final double SPEED_SLOW = 0;
    private final double SPEED_FAST = 6;
    private final int REFRESH_SPACE_TIME = 10 * 1000;


    private MapView mapView;
    private AMap aMap;
    private AMapLocationManager locationManager;
    private GroundOverlay darkLayout;//地图暗色遮罩
    private BitmapDescriptor mMarkFlag;//标识点图标

    private Circle mLocatePointCenter;//定位中心点圆圈
    private Circle mLocatePointRange;//定位点80米范围圆圈
    private boolean bEnableLocate;
    private boolean bLocateOnlyOnce;

    private long lastLocateTime;
    private int iLocateIndex;
    private int colorPause;
    private int colorCenter;//定位中心点圆圈填充色
    private int colorRadius;//定位点80米范围圆圈填充色
    private int iScreenWidth;//屏幕像素宽
    private int iScreenHeight;//屏幕像素高
    private int iTrailLineWidth;//轨迹线条宽度

    private List<Polyline> polyLines;//轨迹线集合
    private Marker startMarker;//开始标识
    private Marker endMarker;//结束标识

    private AMapLocationManager.OnAMapLocationChangeListener locationChangeListener = new AMapLocationManager.OnAMapLocationChangeListener() {

        @Override
        public void onLocationChange(AMapLocation aLocation) {
            if ((aLocation == null) || (aMap == null))
                return;
            if ((aLocation.getLatitude() == 0)
                    && (aLocation.getLongitude() == 0))
                return;
            LatLng pos = new LatLng(aLocation.getLatitude(),
                    aLocation.getLongitude());
            if (iLocateIndex <= 0) {
                if(getAMap() != null) {
                    getAMap().animateCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    pos, Config.MAP_CAMERA_ZOOM, 0, 0)), 1000, null);
                }
                forceShowLocationPoint(pos);
                if (bLocateOnlyOnce)
                    setEnableLocate(false);

                if (aLocation.getAccuracy() > Config.GPS_AVAILABLE_ACCURACY)
                    return;
                if (aLocation.getSpeed() == 0)
                    return;
            }

            long now = System.currentTimeMillis();//Calendar.getInstance().getTimeInMillis();
            if (now - lastLocateTime < REFRESH_SPACE_TIME)
                return;
            float zoom = Config.MAP_CAMERA_ZOOM;
            if(getAMap() != null) {
                 zoom = getAMap().getCameraPosition().zoom;
            }

            if (iLocateIndex <= 0)
                zoom = Config.MAP_CAMERA_ZOOM;
            iLocateIndex++;
            lastLocateTime = now;
            if(getAMap() != null) {
                getAMap().animateCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                pos, zoom, 0, 0)), 1000, null);
            }
            forceShowLocationPoint(pos);
        }
    };

    private void clear() {
        mapView = null;
        if(aMap != null){
            aMap.clear();
        }
        aMap = null;
        locationManager = null;
        if (darkLayout != null)
            getDarkLayer().remove();
        darkLayout = null;
        if (mMarkFlag != null)
            mMarkFlag.recycle();
        mMarkFlag = null;
        mLocatePointCenter = null;
        mLocatePointRange = null;
        bEnableLocate = false;
        bLocateOnlyOnce = false;

        colorPause = 0;
        colorCenter = 0;
        colorRadius = 0;
        iLocateIndex = 0;
        lastLocateTime = 0;

        handleMemoryLeak();
    }

    /**
     * 解决高德地图SDK有时出现的未注销广播引起的内存泄漏
     * */
    private void handleMemoryLeak(){
        /***利用java反射注销有时高德地图没有销毁的广播
         *  IntentFilter var8 = new IntentFilter();
         *  var8.addAction("android.intent.action.TIME_SET");
         *  var8.addAction("android.intent.action.DATE_CHANGED");
         *  LocalBroadcastManager.getInstance(this).unregisterReceiver(new AMapDelegateImpGLSurfaceView.TimeChangedReceiver());
         **/
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(MapApp.getContext());
        if(manager != null){
            try {
                Field field = manager.getClass().getDeclaredField("mReceivers");
                // 设置访问权限
                field.setAccessible(true);
                // 得到私有的变量值
                Object receivers = field.get(manager);
                if(receivers != null){
                    HashMap<BroadcastReceiver, ArrayList<IntentFilter>> listHashMap = (HashMap<BroadcastReceiver, ArrayList<IntentFilter>>) receivers;
                    if(listHashMap != null) {
                        Set<Map.Entry<BroadcastReceiver, ArrayList<IntentFilter>>> entries =  listHashMap.entrySet();
                        for(Map.Entry<BroadcastReceiver, ArrayList<IntentFilter>> entry: entries){
                            if(entry != null){
                                ArrayList<IntentFilter> intentFilters = entry.getValue();
                                if(intentFilters != null){
                                    for(IntentFilter intentFilter : intentFilters){
                                        Iterator<String> actionsIterator = intentFilter.actionsIterator();
                                        if(actionsIterator != null && actionsIterator.hasNext()){
                                            String action = actionsIterator.next();
                                            if("android.intent.action.TIME_SET".equals(action) ||
                                                    "android.intent.action.DATE_CHANGED".equals(action)){
                                                if(entry.getKey() != null){
                                                    manager.unregisterReceiver(entry.getKey());
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建高德地图帮助
     *
     * @param mapView 高德地图view
     * @param bEnableLocate 是否需要定位,true:是,false:否
     * */
    public AMapHelper(MapView mapView, boolean bEnableLocate) {
        clear();
        this.bEnableLocate = bEnableLocate;
        this.mapView = mapView;
        init();
    }

    private void init() {
        init(MapApp.getContext());
    }

    /**
     * 地图初始化配置
     *
     * @param context 上下文
     * */
    private void init(Context context) {
        if (context == null)
            return;

        mMarkFlag = BitmapDescriptorFactory.fromResource(R.drawable.run_flag);
        colorPause = 0xFF808080;

        iTrailLineWidth = (int) context.getResources().getDimension(
                R.dimen.fitmix_line_width);
        colorCenter = 0xFF99CC33;
        colorRadius = 0x20000000;

        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        iScreenWidth = dm.widthPixels;
        iScreenHeight = dm.heightPixels;
    }


    /** 获取高德地图实例*/
    public AMap getAMap() {
        if ((aMap == null) && (mapView != null))//
            aMap = mapView.getMap();
        return aMap;
    }

    /** 获取高德地图View*/
    public MapView getMapView() {
        return mapView;
    }

    //region =========================需要与Activity或Fragment生命周期绑定的方法======================
    /**
     * 地图创建,注意:一定要在Activity或Fragment onCreate方法中调用
     * */
    public void onCreate(Bundle savedInstanceState) {
        if (!isMapViewValid())
            return;
        if(mapView!=null) {
            mapView.onCreate(savedInstanceState);
            setupMap();
        }
    }

    /**
     * 暂停地图绘制,注意:一定要在Activity或Fragment onPause方法中调用
     * */
    public void onPause() {
        if(isMapViewValid()) {
            mapView.onPause();
        }
    }

    /**
     * 恢复地图绘制,注意:一定要在Activity或Fragment onResume方法中调用
     * */
    public void onResume() {
        if(isMapViewValid()) {
            mapView.onResume();
        }
    }

    /**
     * 地图销毁,注意:一定要在Activity或Fragment onDestroy方法中调用
     * */
    public void onDestroy() {
        if (getLocationManager() != null) {
            getLocationManager().setOnAMapLocationChangeListener(null);
            getLocationManager().stopLocate();
        }
        locationChangeListener = null;
        locationManager = null;
        if (isMapViewValid()){
            mapView.setDrawingCacheEnabled(false);
            mapView.onDestroy();
            mapView.removeAllViews();
        }

        clear();
    }

    /**
     * 地图状态保存,注意:如果需要保持地图状态则一定要在Activity或Fragment onSaveInstanceState方法中调用
     * */
    public void onSaveInstanceState(Bundle outState) {
        if (!isMapViewValid())
            return;
        mapView.onSaveInstanceState(outState);
    }

    //endregion =========================需要与Activity或Fragment生命周期绑定的方法======================

    private boolean isMapViewValid() {
        return (mapView != null);
    }

    private void setupMap() {
        if (!isMapViewValid())
            return;
        mapView.setDrawingCacheEnabled(true);
        if(getAMap() != null) {
            getAMap().getUiSettings().setZoomControlsEnabled(false);//隐藏地图缩放控件
        }
        setEnableLocate(bEnableLocate);
    }

    private AMapLocationManager getLocationManager() {
        if (locationManager == null)
            locationManager = new AMapLocationManager();
        return locationManager;
    }

    private GroundOverlay getDarkLayer() {
        return darkLayout;
    }

    private BitmapDescriptor getMarkFlag() {
        return mMarkFlag;
    }

    /**
     * 设置是否开启定位
     *
     * @param bEnableLocate true:是,false:否
     * */
    private void setEnableLocate(boolean bEnableLocate) {
        this.bEnableLocate = bEnableLocate;
        if (bEnableLocate) {
            getLocationManager().startLocate();
            getLocationManager().setOnAMapLocationChangeListener(
                    locationChangeListener);

        } else if (locationManager != null) {
            getLocationManager().stopLocate();
            getLocationManager().setOnAMapLocationChangeListener(
                    null);
        }
    }

    /**
     * 获取定位点圆圈
     *
     * @param point 原圆圈
     * @param pos 经纬度点
     * @param radius 圆圈半径
     * @param color 圆圈填充色
     * @param zIndex 圆圈在地图上的显示层次,数字越大显示在上面
     * */
    private Circle setLocatePoint(Circle point, LatLng pos, double radius,
                                  int color, int zIndex) {
        if (point == null) {
            CircleOptions op = new CircleOptions();
            op.center(pos);
            op.fillColor(color);
            op.radius(radius);
            op.strokeWidth(0);
            op.zIndex(zIndex);
            op.strokeColor(color);
            if (aMap == null)
                return null;
            return getAMap().addCircle(op);
        } else {
            point.setCenter(pos);
            return point;
        }
    }

    /**
     * 根据速度获取轨迹线条颜色
     *
     * @param dSpeed 速度,单位为米/秒
     * @param bPause 是否运动暂停状态
     * */
    private int getColorBySpeed(double dSpeed, boolean bPause) {
        if (bPause)
            return colorPause;
        if (dSpeed <= SPEED_SLOW)
            return 0xFF00FF00;//纯绿
        else if (dSpeed >= SPEED_FAST)
            return 0xFFFF0000;//纯红
        else if (dSpeed <= (SPEED_FAST + SPEED_SLOW) / 2) {
            int g = (int) (0xFF * (SPEED_FAST - dSpeed) / (SPEED_FAST - SPEED_SLOW));
            return 0xFFFF0000 | (g << 8);
        } else {
            int r = (int) (0xFF * (dSpeed - SPEED_SLOW) / (SPEED_FAST - SPEED_SLOW));
            return 0xFF00FF00 | (r << 16);
        }
    }


    /**
     * 添加暗色遮罩图层
     *
     * @param point 经纬度中心点
     * */
    private void addDarkOverLay(LatLng point) {
        if (aMap == null || point == null)
            return;

        if (darkLayout != null) {
            getDarkLayer().remove();
        }
        darkLayout = null;
        GroundOverlayOptions overlayOptions = new GroundOverlayOptions();

        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0x32000000);
        canvas.save();
        canvas.restore();
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
                .fromBitmap(bitmap);

        overlayOptions.anchor(0.5F, 0.5F);
        overlayOptions.zIndex(4.0F);
        overlayOptions.positionFromBounds(getWholeWordBounds(point));
        overlayOptions.image(bitmapDescriptor);
        if (bitmap != null)
            bitmap.recycle();
        if(getAMap() != null) {
            darkLayout = getAMap().addGroundOverlay(overlayOptions);
        }
    }

    /**
     * 在地图上添加标识点
     *
     * @param point 标识点位置
     * @param sText 标识点文字
     * */
    private void addMarkFlag(LatLng point, String sText) {
        if (aMap == null || sText == null || point == null
                || getMarkFlag() == null)
            return;
        double dRatioW = iScreenWidth * 2.0 / 3 / 720;
        double dRatioH = iScreenHeight * 2.0 / 3 / 1280;
        float dRatio = 1;
        if (iScreenWidth <= 1280) dRatio = (float) (dRatioW > dRatioH ? dRatioH : dRatioW);

        int width = (int) (getMarkFlag().getWidth() * dRatio);
        int height = (int) (getMarkFlag().getHeight() * dRatio);

        Bitmap bitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        Matrix matrix = new Matrix();
        matrix.postScale(dRatio, dRatio);
        canvas.drawBitmap(getMarkFlag().getBitmap(), matrix, paint);

        paint.setColor(0xFFFFFFFF);
        paint.setTextSize((int) (36 * dRatio));
        float textWidth = paint.measureText(sText);

        canvas.drawText(sText, (width - textWidth) / 2,
                height / 2 + 2, paint);
        canvas.save();
        canvas.restore();
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
                .fromBitmap(bitmap);

        MarkerOptions markFlag = new MarkerOptions();
        markFlag.icon(bitmapDescriptor).position(point);
        if (getAMap() != null) {
            getAMap().addMarker(markFlag);//out of memory
        }
    }

    /**
     * 根据中心点获取一个很大的矩形区域
     *
     * @param center 经纬度中心点
     * */
    private LatLngBounds getWholeWordBounds(LatLng center) {
        if (center == null)
            return null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng point1 = new LatLng(center.latitude + 10, center.longitude + 10);
        builder.include(point1);
        LatLng point2 = new LatLng(center.latitude - 10, center.longitude - 10);
        builder.include(point2);
        LatLng point3 = new LatLng(center.latitude - 10, center.longitude + 10);
        builder.include(point3);
        LatLng point4 = new LatLng(center.latitude + 10, center.longitude - 10);
        builder.include(point4);
        return builder.build();
    }

    /**
     * 获取轨迹的范围和轨迹中心点
     *
     * @param list 轨迹点集合
     * @param result 存储中心点信息的数组,index 0:lat,index 1:lng,index 2: zoom
     *
     * @return true:成功,false失败
     * */
    private boolean getTrailLineRectByZoom(List<TrailInfo> list,
                                     double[] result) {
        if (list == null || (list.size() == 0) || (result == null))
            return false;
        double left, top, right, bottom;
        left = right = list.get(0).getLng();
        top = bottom = list.get(0).getLat();
        int size = list.size();

        double lat, lng;
        for (int i = 1; i < size; i++) {
            lng = list.get(i).getLng();
            lat = list.get(i).getLat();
            if (lng < left)
                left = lng;
            else if (lng > right)
                right = lng;

            if (lat < top)
                top = lat;
            else if (lat > bottom)
                bottom = lat;
        }
        result[0] = (top + bottom) / 2;
        result[1] = (left + right) / 2;

        double distance = WatchTrailManager.getShortDistance(left, top, right, bottom);
        final int DISTANCE_MIN = 300;
        final int DISTANCE_MAX = 10000;
        double zoom;
        if (distance <= DISTANCE_MIN)
            zoom = Config.MAP_CAMERA_ZOOM_MAX;
        else if (distance >= DISTANCE_MAX)
            zoom = Config.MAP_CAMERA_ZOOM_MIN;
        else
            zoom = (Config.MAP_CAMERA_ZOOM_MAX + (Config.MAP_CAMERA_ZOOM_MIN - Config.MAP_CAMERA_ZOOM_MAX)
                    * (distance - DISTANCE_MIN) / (DISTANCE_MAX - DISTANCE_MIN));
        result[2] = zoom;
        return true;
    }

    /**
     * 获取轨迹的范围和轨迹中心点
     *
     * @param list   轨迹点集合
     * @param result 存储中心点信息的数组,index 0:中心点纬度,index 1:中心点经度,index 2: 最西边的经度 index 3: 最东边的经度
     *               index 4:最北边的纬度 index 5:最南边的纬度
     * @return true:成功,false失败
     */
    private boolean getTrailLineRect(List<TrailInfo> list,
                                     double[] result) {
        if (list == null || (list.size() == 0) || (result == null))
            return false;
        double left, top, right, bottom;
        left = right = list.get(0).getLng();
        top = bottom = list.get(0).getLat();
        int size = list.size();

        double lat, lng;
        for (int i = 1; i < size; i++) {
            lng = list.get(i).getLng();
            lat = list.get(i).getLat();
            if (lng < left)
                left = lng;
            else if (lng > right)
                right = lng;

            if (lat < bottom)
                bottom = lat;
            else if (lat > top)
                top = lat;
        }
        result[0] = (top + bottom) / 2;
        result[1] = (left + right) / 2;
        result[2] = left;
        result[3] = right;
        result[4] = top;
        result[5] = bottom;

        return true;
    }

    //region =============================== 对外公共方法 ======================================

    /** 清除地图所有标识*/
    public void clearTrail() {
        if (aMap != null)
            getAMap().clear();
        mLocatePointCenter = null;
        mLocatePointRange = null;
    }

    /**
     * 重新移动地图镜头
     *
     * @param pos 定位点
     * @param bAnimateCamera 动画移动地图镜头 true:是,false:否
     * @param bShowLocationPos 是否显示带圆圈效果定位点
     *
     * */
    public void relocatePosition(LatLng pos, boolean bAnimateCamera, boolean bShowLocationPos) {
        if (aMap == null)
            return;

        lastLocateTime = System.currentTimeMillis();

        float zoomIndex = Config.MAP_CAMERA_ZOOM;
        if(getAMap() != null){
            if(getAMap().getCameraPosition() != null) {
                zoomIndex = getAMap().getCameraPosition().zoom;
                if(zoomIndex < Config.MAP_CAMERA_ZOOM_MIN){
                    zoomIndex = Config.MAP_CAMERA_ZOOM;
                }
            }
        }
        if (bAnimateCamera) {
            if(getAMap() != null) {
                getAMap().animateCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition(pos,
                                zoomIndex, 0, 0)), 1000, null);
            }
        }else{
            if(getAMap() != null) {
                getAMap().moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(pos,zoomIndex, 0, 0)));
            }
        }

        if (bShowLocationPos) forceShowLocationPoint(pos);
    }

    /**
     * 在地图显示带圆圈效果定位点
     *
     * @param pos 定位点
     * */
    public void forceShowLocationPoint(LatLng pos) {
        mLocatePointCenter = setLocatePoint(mLocatePointCenter, pos, 10,
                colorCenter, 1);
        mLocatePointRange = setLocatePoint(mLocatePointRange, pos,
                Config.GPS_AVAILABLE_ACCURACY, colorRadius, 1);
    }


    /**
     * 显示轨迹
     *
     * @param list 轨迹点集合
     * @param mapWidth 地图宽度
     * @param mapHeight 地图高度
     * @param bAnimateCamera 是否镜头动画般移动
     */
    public void setTrailOfMap(List<TrailInfo> list, int mapWidth, int mapHeight, boolean bAnimateCamera) {
        if ((list == null) || (list.size() <= 1))
            return;
        if (aMap == null)
            return;
        //1.清除地图所有标识
        clearTrail();
        if (list.size() <= 0) return;
        //4.平滑处理
        list = bSpline(list);

        //7.添加轨迹点
        LatLng position = null;//当前处理的点
        PolylineOptions option = null;
        if(option == null){
            option = new PolylineOptions();
            option.zIndex(5.0f);
            option.width(iTrailLineWidth);
            option.setDottedLine(false);//画实线
        }
        int color = 0xFF00FF00;//纯绿;
        for (int i = 0; i < list.size(); i++) {
            position = new LatLng(list.get(i).getLat(), list.get(i).getLng());
            option.add(position);
            if(i == 0){
                continue;
            }
//                color = getColorBySpeed(dSpeed, bOldPause);
            option.color(color);
            if (getAMap() != null) {
                getAMap().addPolyline(option);
                if(option.getPoints() != null) {
                    option.getPoints().clear();
                }
            }
            option.add(position);
        } //end for

        // 9.添加起始点和终点标识
        LatLng positionStart = new LatLng(list.get(0).getLat(), list.get(0)
                .getLng());
        LatLng positionEnd = new LatLng(list.get(list.size() - 1).getLat(),
                list.get(list.size() - 1).getLng());

        BitmapDescriptor bitmapStart = BitmapDescriptorFactory
                .fromResource(R.drawable.run_start);
        MarkerOptions markStart = new MarkerOptions();
        markStart.icon(bitmapStart).position(positionStart);
        if(getAMap() != null) {
            getAMap().addMarker(markStart);
        }
        BitmapDescriptor bitmapEnd = BitmapDescriptorFactory
                .fromResource(R.drawable.run_end);
        MarkerOptions markEnd = new MarkerOptions();
        markEnd.icon(bitmapEnd).position(positionEnd);
        if(getAMap() != null) {
            getAMap().addMarker(markEnd);
        }

        // 11.根据轨迹形状决定轨迹边界适配地图缩放大小,并添加地图暗色遮罩图层
        double[] map = new double[6];
        boolean bSuccess = getTrailLineRect(list, map);
        if (bSuccess) {
            double lat = map[0];
            double lng = map[1];
            position = new LatLng(lat, lng);
            addDarkOverLay(position);
//         12.根据是否动画显示轨迹显示,即将镜头从地图初始化显示的位置（北京天安门）移动到轨迹的位置
            if (getAMap() != null && position != null) {
                LatLng northeast = new LatLng(map[4], map[3]);
                LatLng southwest = new LatLng(map[5], map[2]);
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(northeast);
                boundsBuilder.include(southwest);
                LatLngBounds bounds = boundsBuilder.build();
                int padding = (int) (mapHeight * 0.1f);
                // 11.根据是否动画移动摄像机
                if (bAnimateCamera) {
                    getAMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapWidth, mapHeight, padding), 1000, null);
                } else {
                    getAMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapWidth, mapHeight, padding));
                }
            }
        }
    }

    /**
     * 轨迹回放动画
     * @param list
     * @param mapWidth
     * @param mapHeight
     * @param callback
     */
    public void setAnimTrailOfMap(List<TrailInfo> list, int mapWidth, int mapHeight,AMap.CancelableCallback callback){
        if ((list == null) || (list.size() <= 1) || aMap == null) {
            if(callback!=null){
                callback.onCancel();
            }
            return;
        }
        //1.清除地图所有标识
        clearTrail();
        if (list.size() <= 0) return;

        // 2.根据轨迹形状决定轨迹边界适配地图缩放大小,并添加地图暗色遮罩图层
        LatLng position = null;//当前处理的点
        double[] map = new double[6];
        boolean bSuccess = getTrailLineRect(list, map);
        if (bSuccess) {
            double lat = map[0];
            double lng = map[1];
            position = new LatLng(lat, lng);
            addDarkOverLay(position);

            if (getAMap() != null && position != null) {
                LatLng northeast = new LatLng(map[4], map[3]);
                LatLng southwest = new LatLng(map[5], map[2]);
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(northeast);
                boundsBuilder.include(southwest);
                LatLngBounds bounds = boundsBuilder.build();
                int padding = (int) (mapHeight * 0.1f);
                //移动摄像头
                getAMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapWidth, mapHeight, padding), 1000, callback);
            }
        }

        // 5.添加起始点和终点标识
        LatLng positionStart = new LatLng(list.get(0).getLat(), list.get(0)
                .getLng());
        LatLng positionEnd = new LatLng(list.get(list.size() - 1).getLat(),
                list.get(list.size() - 1).getLng());

        BitmapDescriptor bitmapStart = BitmapDescriptorFactory
                .fromResource(R.drawable.run_start);
        MarkerOptions markStart = new MarkerOptions();
        markStart.icon(bitmapStart).position(positionStart);
        if(getAMap() != null) {
            startMarker = getAMap().addMarker(markStart);
        }
        BitmapDescriptor bitmapEnd = BitmapDescriptorFactory
                .fromResource(R.drawable.run_end);
        MarkerOptions markEnd = new MarkerOptions();
        markEnd.icon(bitmapEnd).position(positionEnd);
        if(getAMap() != null) {
            endMarker = getAMap().addMarker(markEnd);
        }


        //3.平滑处理
        list = bSpline(list);

        //4.添加轨迹点
        PolylineOptions option = null;
        if(option == null){
            option = new PolylineOptions();
            option.zIndex(5.0f);
            option.width(iTrailLineWidth);
            option.setDottedLine(false);//画实线
            polyLines = new ArrayList<>(list.size());
        }
        int color = 0xFF00FF00;//纯绿;
        for (int i = 0; i < list.size(); i++) {
            position = new LatLng(list.get(i).getLat(), list.get(i).getLng());
            option.add(position);
            if(i == 0){
                continue;
            }
//                color = getColorBySpeed(dSpeed, bOldPause);
            option.color(color);
            addPolyLine(option);
            option.add(position);
        } //end for
        showTrail(false);


        //6.获取地图显示区域失败回调通知
        if (!bSuccess) {
            if (callback != null) {
                callback.onCancel();
            }
        }
    }

    /**
     * 在地图上添加轨迹线条
     *
     * @param option 线条设置参数
     */
    private void addPolyLine(PolylineOptions option) {
        if (getAMap() != null) {
            if (polyLines != null) {
                polyLines.add(getAMap().addPolyline(option));
            }
            if (option.getPoints() != null) {
                option.getPoints().clear();
            }
        }
    }


    /**
     * 设置是否显示轨迹
     *
     * @param show 是否显示轨迹,true:显示,false:不显示
     */
    public void showTrail(boolean show) {
        if (aMap != null) {
            if (show) {
                if (startMarker != null) {
                    startMarker.setVisible(true);
                    Log.i("TT","startMarker.setVisible(true)");
                }
                if (endMarker != null) {
                    endMarker.setVisible(true);
                    Log.i("TT","endMarker.setVisible(true)");
                }
                if (polyLines != null) {
                    for (Polyline polyline : polyLines) {
                        polyline.setVisible(true);
                    }
                }
            } else {
                if (startMarker != null) {
                    startMarker.setVisible(false);
                }
                if (endMarker != null) {
                    endMarker.setVisible(false);
                }

                if (polyLines != null) {
                    for (Polyline polyline : polyLines) {
                        polyline.setVisible(false);
                    }
                }
            }
        }
    }



    /**
     * b-spline 平滑轨迹线 http://www.maissan.net/articles/simulating-vines/2
     *
     * @param trailInfoList 要平滑处理的轨迹点集合
     *
     * @return 平滑处理的结果集合
     * */
    private List<TrailInfo> bSpline(List<TrailInfo> trailInfoList) {
        if(trailInfoList == null || trailInfoList.size() < 2)
            return trailInfoList;

        //int iLen = trailInfoList.size();
        double t;
        double ax,bx,cx,lat;
        double ay,by,cy,lon;
        double dx,dy;
        // For every point
        List<TrailInfo> result = new ArrayList<>();//重新处理
        result.add(trailInfoList.get(0));
        result.add(trailInfoList.get(1));
        /** java.lang.NullPointerException: Attempt to invoke virtual method
         * 'void com.fitmix.sdk.bean.TrailInfo.setLat(double)' on a null object reference*/
        /** java.lang.IndexOutOfBoundsException: Invalid index 1073, size is 1608*/
        /** java.lang.NullPointerException
         *  at com.fitmix.sdk.common.maps.AMapHelper.bSpline(AMapHelper.java:1077)*/
        long time;
        int sportState;
        boolean used;

        for (int i = 2; i < trailInfoList.size() - 2; i++) {
            ax = (-1*trailInfoList.get(i-2).getLat() + 3*trailInfoList.get(i-1).getLat()
                    - 3*trailInfoList.get(i).getLat() + trailInfoList.get(i+1).getLat()) / 6;
            ay = (-1*trailInfoList.get(i-2).getLng() + 3*trailInfoList.get(i-1).getLng()
                    -3*trailInfoList.get(i).getLng() + trailInfoList.get(i+1).getLng()) / 6;
            bx = (trailInfoList.get(i-2).getLat() - 2*trailInfoList.get(i-1).getLat() +trailInfoList.get(i).getLat()) / 2;
            by = (trailInfoList.get(i-2).getLng() - 2*trailInfoList.get(i-1).getLng() + trailInfoList.get(i).getLng()) / 2;
            cx = (-1*trailInfoList.get(i-2).getLat() + trailInfoList.get(i).getLat()) / 2;
            cy = (-1*trailInfoList.get(i-2).getLng() + trailInfoList.get(i).getLng()) / 2;
            dx = (trailInfoList.get(i-2).getLat() + 4*trailInfoList.get(i-1).getLat() + trailInfoList.get(i).getLat()) / 6;
            dy = (trailInfoList.get(i-2).getLng() + 4*trailInfoList.get(i-1).getLng() + trailInfoList.get(i).getLng()) / 6;

            time = trailInfoList.get(i).getTime();
            sportState = trailInfoList.get(i).getSportState();
            used = trailInfoList.get(i).getUsed();

            for (t = 0; t < 1; t += 0.2) {
                lat = ax * Math.pow(t + 0.1, 3) + bx * Math.pow(t + 0.1, 2) + cx * (t + 0.1) + dx;
                lon = ay * Math.pow(t + 0.1, 3) + by * Math.pow(t + 0.1, 2) + cy * (t + 0.1) + dy;
                result.add(new TrailInfo(time,lat,lon,sportState,used));
            }
        }
        result.add(trailInfoList.get(trailInfoList.size() - 2));
        result.add(trailInfoList.get(trailInfoList.size() - 1));
        Log.i("TT","bSpline list:" + trailInfoList.size() + ",result size:"+result.size());
        return result;
//        return trailInfoList;
    }









    /**
     * 在地图显示的轨迹点集合转换为对应在屏幕有颜色的坐标点集合
     *
     * @param list 已平滑处理过的轨迹点集合
     */
    public List<Point> convertTrailPoint(List<TrailInfo> list) {
        if (list == null || getAMap() == null || (list.size() <= 1))
            return null;
        //7.获取轨迹点对应在屏幕上的坐标点
        List<Point> pointWithColors = new ArrayList<>(list.size());
        LatLng latLng;
        TrailInfo trailInfo;
        Point point;
        for (int i = 0; i < list.size(); i++) {
            trailInfo = list.get(i);
            latLng = new LatLng(trailInfo.getLat(), trailInfo.getLng());
            point = getAMap().getProjection().toScreenLocation(latLng);
            pointWithColors.add(point);
        }
        Log.i("TT", "convertTrailPoint done!");
        return pointWithColors;
    }

    //endregion ===============================对外公共方法======================================

}

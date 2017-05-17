package com.sdk.dyq.mapmodule.view.activity;

import android.os.PersistableBundle;
import android.os.Bundle;
import com.amap.api.maps.MapView;
import com.sdk.dyq.mapmodule.Config;
import com.sdk.dyq.mapmodule.R;
import com.sdk.dyq.mapmodule.common.JSonHelper;
import com.sdk.dyq.mapmodule.common.ThreadManager;
import com.sdk.dyq.mapmodule.common.bean.TrailInfo;
import com.sdk.dyq.mapmodule.common.maps.AMapHelper;
import com.sdk.dyq.mapmodule.common.utils.ViewUtils;

import java.util.List;


public class MapTrailActivity extends MapBaseActivity {

    MapView mapView;
    List<TrailInfo> mTrailInfoList;

    //region ================================== 高德地图轨迹及gps数据点 ==================================
//    private SensorInformation GPSSensorInformation;
    private AMapHelper mapManager;//高德地图控制器
    //endregion ================================== 高德地图轨迹 ==================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMaps(savedInstanceState);
        getTrailData();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (getMapManager() != null) {
            getMapManager().onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getMapManager() != null) {
            getMapManager().onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getMapManager() != null) {
            getMapManager().onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (getMapManager() != null) {
            getMapManager().onSaveInstanceState(outState);
        }
    }

    //region ================================== 高德地图初始化 ==================================
    private void initMaps(Bundle savedInstanceState) {
        mapView = (com.amap.api.maps.MapView) findViewById(R.id.map);
        mapManager = new AMapHelper(mapView, false);
        if (getMapManager() != null) {
            //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
            getMapManager().onCreate(savedInstanceState);
        }

    }
    private AMapHelper getMapManager() {
        return mapManager;
    }
    //endregion ================================== 高德地图初始化 ==================================

    private void getTrailData(){
        final int mapWidth = ViewUtils.getScreenWidth(this)-ViewUtils.dp2px(this,30);
        final int mapHeight = ViewUtils.getScreenHeight(this)-ViewUtils.dp2px(this,30);
        ThreadManager.executeOnSubThread1(new Runnable() {
            @Override
            public void run() {
//                mTrailInfoList = JSonHelper.readRunTrailFile(Config.PATH_SENSOR_GPS+"163468_1458696465000.json");
                mTrailInfoList = JSonHelper.readRunTrailFile(Config.PATH_SENSOR_GPS+"163468_1477564918000.json");
                if (mTrailInfoList == null || mTrailInfoList.size() == 0)
                    return;
                if (mapManager == null)
                    return;

                getMapManager().setTrailOfMap(mTrailInfoList,mapWidth,mapHeight,false);
            }
        });
    }
}

package com.sdk.dyq.mapmodule.view.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.sdk.dyq.mapmodule.Config;
import com.sdk.dyq.mapmodule.R;
import com.sdk.dyq.mapmodule.common.JSonHelper;
import com.sdk.dyq.mapmodule.common.ThreadManager;
import com.sdk.dyq.mapmodule.common.bean.PointWithColor;
import com.sdk.dyq.mapmodule.common.bean.TrailInfo;
import com.sdk.dyq.mapmodule.common.maps.AMapHelper;
import com.sdk.dyq.mapmodule.common.utils.ViewUtils;
import com.sdk.dyq.mapmodule.view.widget.TrailAnimView;

import java.util.List;

/**
 * 地图动画显示轨迹
 */

public class MapAnimActivity extends MapBaseActivity {

    private MapView mapView;
    private AMapHelper mapManager;//高德地图控制器
    private List<TrailInfo> mTrailInfoList;


    private LinearLayout progress_trail;//加载进度框
    private TrailAnimView trail_anim;//绘制轨迹的动画控件
    private List<Point> pointWithColors;//将TrailInfo轨迹点集合转化为具备颜色的集合点


    private boolean cameraLocated;//地图摄像机是否已完成初次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_anim);
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
        progress_trail = (LinearLayout) findViewById(R.id.progress_trail);
        trail_anim = (TrailAnimView) findViewById(R.id.trail_anim);
        trail_anim.setAnimListener(onTrailAnimListener);
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


    private TrailAnimView.OnTrailAnimListener onTrailAnimListener = new TrailAnimView.OnTrailAnimListener() {
        @Override
        public void OnTrailAnimEnd() {
            if(mapManager != null) {
                getMapManager().showTrail(true);
            }
            if(trail_anim != null){
                trail_anim.stopAnimation();
                trail_anim = null;
            }
            if (progress_trail != null) {
                progress_trail.setVisibility(View.GONE);
            }
        }
    };

    private void getTrailData(){
        if (progress_trail != null) {
            progress_trail.setVisibility(View.VISIBLE);
        }

        final int mapWidth = ViewUtils.getScreenWidth(this)-ViewUtils.dp2px(this,30);
        final int mapHeight = ViewUtils.getScreenHeight(this)-ViewUtils.dp2px(this,30);
        ThreadManager.executeOnSubThread1(new Runnable() {
            @Override
            public void run() {
                mTrailInfoList = JSonHelper.readRunTrailFile(Config.PATH_SENSOR_GPS+"163468_1477564918000.json");
                if (mTrailInfoList == null || mTrailInfoList.size() == 0)
                    return;
                if (mapManager == null)
                    return;

                getMapManager().setAnimTrailOfMap(mTrailInfoList, mapWidth, mapHeight, new AMapHelper.MapCameraChangeFinish() {
                    @Override
                    public void onFinish() {
                        if (cameraLocated)
                            return;
                        cameraLocated = true;
                        Log.i("TT", "AMapHelper.MapCameraChangeFinish onFinish..");
                        if (trail_anim != null)
                            progress_trail.setVisibility(View.GONE);
                        pointWithColors = mapManager.convertTrailPoint(mTrailInfoList);
                        if(trail_anim!=null && pointWithColors!=null){
                            trail_anim.setColorPoints(pointWithColors);
                            trail_anim.startAnimation();
                        }
                    }

//                    @Override
//                    public void onCancel() {
//                        if(progress_trail!=null){
//                            progress_trail.setVisibility(View.GONE);
//                        }
//                    }
                });
            }
        });
    }


}

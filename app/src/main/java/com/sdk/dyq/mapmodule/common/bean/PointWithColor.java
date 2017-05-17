package com.sdk.dyq.mapmodule.common.bean;

import android.graphics.Point;

/**
 * 带颜色的点,用于在屏幕上绘制动态轨迹
 */

public class PointWithColor {

    public Point point;
    public int color;

    public PointWithColor(Point point, int color){
        this.point = point;
        this.color = color;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;

    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


}


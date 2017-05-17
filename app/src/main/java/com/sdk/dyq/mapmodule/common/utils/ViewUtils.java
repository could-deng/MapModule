package com.sdk.dyq.mapmodule.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by yuanqiang on 2017/5/16.
 */

public class ViewUtils {
    /**
     * dp转px
     *
     * @param
     */
    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static int dp2px(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static float px2sp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    /**
     * 获取屏幕的密度
     *
     * @return 屏幕的像素密度
     */
    public static float getScreenDensity(Context context) {
        if (context == null)
            return 2;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null)
            return 720;
        return dm.density;
    }
    /**
     * 获取屏幕的宽度
     *
     * @return 屏幕的像素宽度
     */
    public static int getScreenWidth(Context context) {
        if (context == null)
            return 720;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null)
            return 720;
        return dm.widthPixels;
    }
    public static int getScreenHeight(Context context){
        if (context == null)
            return 1280;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null)
            return 1280;
        return dm.heightPixels;
    }
}

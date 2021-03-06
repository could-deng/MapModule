package com.sdk.dyq.mapmodule.view.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.sdk.dyq.mapmodule.R;
import com.sdk.dyq.mapmodule.common.bean.PointWithColor;
import com.sdk.dyq.mapmodule.common.bean.TrailInfo;
import com.sdk.dyq.mapmodule.common.utils.ViewUtils;

import java.util.List;


/**
 * 动态展示运动轨迹控件
 */
public class TrailAnimView extends View {

    /**
     * 动画阶段标识,3:开始标识动画,2:轨迹动画,1:结束标识动画
     */
    private int mStage = 4;
    private ValueAnimator mIconAnim;//开始标识、结束标识动画
    private ValueAnimator mLineAnim;//轨迹动画

    private Bitmap bitmapEnd;
    private Bitmap bitmapLight;
    private Bitmap bitmapStart;
    private Matrix mIconMatrix;//图标形变矩阵

    private Paint paint;


    private OnTrailAnimListener animListener;
    private List<Point> mColorPoints;

    private Point mStartPoint;
    private Point mEndPoint;

    private Canvas mCacheCanvas;
    private Bitmap mCacheBitmap;//缓冲位图,性能优化

//    private int mProgress;//当前进度



    private float mLength;//轨迹线长度
    private Path mPath;//轨迹路径
    private PathMeasure mPathMeasure;
    private Matrix mPathMatrix;
    private float mPhase;//轨迹动画进度


    /**
     * 动画状态监听回调
     */
    public interface OnTrailAnimListener {
        /**
         * 轨迹动画结束回调
         */
        void OnTrailAnimEnd();
    }

    public TrailAnimView(Context context) {
        this(context, null);
    }

    public TrailAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrailAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (getResources() == null)
            return;
        mIconMatrix = new Matrix();
        mIconMatrix.postScale(0.71f,0.71f);//0.71f*0.71f=0.5
        final Bitmap start = BitmapFactory.decodeResource(getResources(), R.drawable.run_start);
        if(start!=null){
            bitmapStart = Bitmap.createBitmap(start,0,0,start.getWidth(),start.getHeight(),mIconMatrix,false);
            start.recycle();
        }
        Bitmap end = BitmapFactory.decodeResource(getResources(), R.drawable.run_end);
        if(end!=null){
            bitmapEnd = Bitmap.createBitmap(end,0,0,end.getWidth(),end.getHeight(),mIconMatrix,false);
            end.recycle();
        }

        bitmapLight = BitmapFactory.decodeResource(getResources(),R.drawable.ic_dot);

        paint = new Paint();
        int lineWidth = (int) getResources().getDimension(R.dimen.line_width);
        paint.setStrokeWidth(lineWidth/2);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xFF38FC2F);

        mIconAnim = ValueAnimator.ofFloat(0, 1.0f);//形变大小
        mIconAnim.setDuration(500);
        mIconAnim.setInterpolator(new OvershootInterpolator());
        mIconAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = (float) valueAnimator.getAnimatedValue();
                if(mStage == 3){//开始标识
                    if(mIconMatrix!=null){
                        mIconMatrix.reset();
                        if(bitmapStart!=null){
                            mIconMatrix.postScale(scale,scale,bitmapStart.getWidth() / 2,bitmapStart.getHeight());
                        }else{
                            mIconMatrix.postScale(scale,scale);
                        }
                    }
                    if(scale == 1.0f){
                        mStage = 2;
                        if(mLineAnim!=null) {
                            mLineAnim.start();
                        }else{
                            mStage = 1;
                            if(mIconAnim!=null) {
                                mIconAnim.setStartDelay(0);
                                mIconAnim.start();
                            }
                        }
                    }
                }
                else if(mStage == 1){//结束标识
                    if(mIconMatrix!=null){
                        mIconMatrix.reset();
                        if(bitmapEnd!=null){
                            mIconMatrix.postScale(scale,scale,bitmapEnd.getWidth() / 2,bitmapEnd.getHeight());
                        }else{
                            mIconMatrix.postScale(scale,scale);
                        }
                    }
                    if(scale == 1.0f){
                        if (bitmapEnd != null && mEndPoint != null) {
                            mCacheCanvas.drawBitmap(bitmapEnd, mEndPoint.x, mEndPoint.y, paint);
                        }
                        mStage = 0;
                    }
                }
                invalidate();
            }
        });


        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mStage = 0;
                if(animListener!=null){
                    animListener.OnTrailAnimEnd();
                }
                stopAnimation();
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mStage){
            case 3://开始画起始图标
                if(mStartPoint!=null && mIconMatrix!=null) {
                    mIconMatrix.postTranslate(mStartPoint.x,mStartPoint.y);
                    canvas.drawBitmap(bitmapStart, mIconMatrix, null);
                }
                break;
            case 2://画轨迹
                if(mCacheBitmap!=null){
                    canvas.drawBitmap(mCacheBitmap,0,0,paint);
                }
                if(bitmapLight!=null){
                    mPathMatrix.postTranslate(-bitmapLight.getWidth()/2,-bitmapLight.getHeight()/2);
                    canvas.drawBitmap(bitmapLight,mPathMatrix,paint);
//                    canvas.drawBitmap(bitmapLight,mColorPoints.get(mProgress).x-bitmapLight.getWidth()/2,
//                            mColorPoints.get(mProgress).y-bitmapLight.getHeight()/2,paint);
                }
                break;
            case 1://画终止点图标
                if(mCacheBitmap!=null){
                    canvas.drawBitmap(mCacheBitmap,0,0,paint);
                }
                if(bitmapEnd!=null && mIconMatrix!=null){
                    mIconMatrix.postTranslate(mEndPoint.x,mEndPoint.y);
                    canvas.drawBitmap(bitmapEnd,mIconMatrix,null);
                }
                break;
            case 0:
                if (mCacheBitmap != null) {
                    canvas.drawBitmap(mCacheBitmap, 0, 0, paint);
                }
                startAlphaOut();
                break;
        }
        invalidate();
    }

    /**
     * 隐藏动画
     */
    private void startAlphaOut() {
        ValueAnimator alphaOut = ValueAnimator.ofFloat(1.0f, 0.0f);
        alphaOut.setDuration(500);
        alphaOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                setAlpha(alpha);
                if (alpha == 0) {
                    if (animListener != null) {
                        animListener.OnTrailAnimEnd();
                    }
                    stopAnimation();
                }
            }
        });
        alphaOut.start();
    }

    private void createIconAnim(int paramInt) {

    }

    public void setAnimListener(OnTrailAnimListener animListener) {
        this.animListener = animListener;
    }

    /**
     * 开始轨迹回放动画
     */
    public void startAnimation() {
        if (mIconAnim != null) {
            mStage = 3;
            mIconAnim.setStartDelay(200);
            mIconAnim.start();
            Log.i("TT", "TrainAnimView setColorPoints mIconAnim start!");
        }
    }

    /**
     * 销毁动画
     */
    public void stopAnimation(){
        recycleAnim();
        setVisibility(View.GONE);
        recycleBitmap();
    }
    /**
     * 回收ValueAnim
     */
    private void recycleAnim(){
        if(mIconAnim!=null){
            mIconAnim.cancel();
            mIconAnim = null;
        }
        if(mLineAnim !=null){
            mLineAnim.cancel();
            mLineAnim = null;
        }
    }

    /**
     * 回收位图
     */
    private void recycleBitmap() {
        boolean recycled;
        if(bitmapStart != null){
            recycled = bitmapStart.isRecycled();
            if(!recycled){
                bitmapStart.recycle();
            }
        }

        if(bitmapEnd != null){
            recycled = bitmapEnd.isRecycled();
            if(!recycled){
                bitmapEnd.recycle();
            }
        }

        if(bitmapLight != null){
            recycled = bitmapLight.isRecycled();
            if(!recycled){
                bitmapLight.recycle();
            }
        }
        if(mCacheBitmap !=null){
            recycled = mCacheBitmap.isRecycled();
            if(!recycled){
                mCacheBitmap.recycle();
            }
        }
    }


    //region =========================== 开放的接口 ===========================

    /**
     * 计算轨迹动画时长
     */
    private int getLineAnimDuration() {
        int duration = 6000;
        if (mColorPoints != null && mColorPoints.size() > 1) {
            Point point1;
            Point point2;
            int x;
            int y;
            int length = mColorPoints.size();
            int total = 0;
            for (int i = 1; i < length; i++) {
                point1 = mColorPoints.get(i - 1);
                point2 = mColorPoints.get(i);
                x = Math.abs(point2.x - point1.x);
                y = Math.abs(point2.y - point1.y);
                total += x > y ? x : y;
            }
            if (total < 4320) {//6*60*12
                return duration;
            } else {
                duration = total * 100 / 72;//* 1000 / 720
            }
        }
        return duration;
    }

    /**
     * 设置要绘制的轨迹点
     */
    public void setColorPoints(List<Point> colorPoints) {
        this.mColorPoints = colorPoints;
        if(mColorPoints!=null && mColorPoints.size()>0){
            mStartPoint = new Point(mColorPoints.get(0).x,mColorPoints.get(0).y);
            mEndPoint = new Point(mColorPoints.get(mColorPoints.size()-1).x,mColorPoints.get(mColorPoints.size()-1).y);

            if(bitmapStart!=null && mStartPoint!=null){
                mStartPoint.x -= bitmapStart.getWidth()/2;
                mStartPoint.y -= bitmapStart.getHeight();
            }
            if(bitmapEnd!=null && mEndPoint!=null){
                mEndPoint.x -= bitmapEnd.getWidth()/2;
                mEndPoint.y -= bitmapEnd.getHeight();
            }
        }
//        mProgress = 0;

        if(colorPoints.size()>0){
            //创建缓冲位图
            if(mCacheBitmap == null) {
                mCacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
                mCacheCanvas = new Canvas(mCacheBitmap);
            }

            //创建轨迹动画参数

//            mLineAnim = ValueAnimator.ofInt(0,colorPoints.size());
//            int duration = colorPoints.size();
//            if(duration<3000){
//                duration = 3000;
//            }else if(duration>6000){
//                duration = 6000;
//            }
            int duration = getLineAnimDuration();
            setPath();
            mLineAnim = ObjectAnimator.ofFloat(this,"phase",1.0f,0.0f);
            mLineAnim.setDuration(duration);
            mLineAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = (float) animation.getAnimatedValue();
                    if(mColorPoints == null || progress == 0.0f){
                        mStage = 1;
                        if(mIconAnim!=null){
                            mIconAnim.setStartDelay(0);
                            mIconAnim.start();
                        }
                    }
                    invalidate();
                }
            });
        }

        Log.i("TT","TrainAnimView setColorPoints:"+colorPoints.size());

    }
    public void setPath(){
        mPath = new Path();
        if(mColorPoints!=null && mColorPoints.size()>0){
            Point point = mColorPoints.get(0);
            mPath.moveTo(point.x,point.y);
            for(int i = 1;i<mColorPoints.size();i++){
                point = mColorPoints.get(i);
                mPath.lineTo(point.x,point.y);
            }
        }
        mPathMeasure = new PathMeasure(mPath,false);
        mLength = mPathMeasure.getLength();
        mPathMatrix = new Matrix();
        mPhase = 0.0f;
    }

    /**
     * 执行ObjectAnimator.ofFloat(this,"phase",1.0f,0.0f);时会进入该方法
     * @param phase
     */
    public void setPhase(float phase){//动画属性
        paint.setPathEffect(new DashPathEffect(new float[]{mLength,mLength},
                Math.max(phase * mLength,mPhase)));
        if(mPathMatrix !=null){
            mPathMeasure.getMatrix((1.0f - phase) * mLength ,mPathMatrix,PathMeasure.POSITION_MATRIX_FLAG);
        }
        if(mCacheCanvas!=null){
            mCacheCanvas.drawPath(mPath,paint);
            if(bitmapStart!=null && mStartPoint!=null){
                mCacheCanvas.drawBitmap(bitmapStart,mStartPoint.x,mStartPoint.y,paint);
            }
        }
        mPhase = phase;
        invalidate();
    }


    //endregion =========================== 开放的接口 ===========================
}


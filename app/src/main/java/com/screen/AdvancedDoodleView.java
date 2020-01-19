package com.screen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.forward.androids.ScaleGestureDetectorApi27;
import cn.forward.androids.TouchGestureDetector;

/**
 * 支持对图片涂改, 可移动缩放图片
 */
public class AdvancedDoodleView extends View {

    private final static String TAG = "AdvancedDoodleView";

    private TouchGestureDetector mTouchGestureDetector; // 触摸手势监听
    private float mLastX, mLastY;

    private Bitmap mBitmap;
    private float mBitmapTransX, mBitmapTransY, mBitmapScale = 1;

    public AdvancedDoodleView(Context context) {
        this(context, null);
    }

    public AdvancedDoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 设置画笔
//        mPaint.setColor(Color.RED);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(3);
//        mPaint.setAntiAlias(true);
//        mPaint.setStrokeCap(Paint.Cap.ROUND);

        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

            RectF mRectF = new RectF();

            // 缩放手势操作相关
            Float mLastFocusX;
            Float mLastFocusY;
            float mTouchCentreX, mTouchCentreY;

            @Override
            public boolean onScaleBegin(ScaleGestureDetectorApi27 detector) {
                Log.d(TAG, "onScaleBegin: ");
                mLastFocusX = null;
                mLastFocusY = null;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetectorApi27 detector) {
                Log.d(TAG, "onScaleEnd: ");
            }

            @Override
            public boolean onScale(ScaleGestureDetectorApi27 detector) { // 双指缩放中
                Log.d(TAG, "onScale: ");
                // 屏幕上的焦点
                mTouchCentreX = detector.getFocusX();
                mTouchCentreY = detector.getFocusY();

                if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                    float dx = mTouchCentreX - mLastFocusX;
                    float dy = mTouchCentreY - mLastFocusY;
                    // 移动图片
                    mBitmapTransX = mBitmapTransX + dx;
                    mBitmapTransY = mBitmapTransY + dy;
                }

                // 缩放图片
                mBitmapScale = mBitmapScale * detector.getScaleFactor();
                if (mBitmapScale < 1f) {
                    mBitmapScale = 1f;
                }
                if (mBitmapScale > 3.0f) {
                    mBitmapScale = 3.0f;
                }
                invalidate();

                mLastFocusX = mTouchCentreX;
                mLastFocusY = mTouchCentreY;

                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) { // 单击选中
                float x = toX(e.getX()), y = toY(e.getY());
                invalidate();
                return true;
            }

            @Override
            public void onScrollBegin(MotionEvent e) { // 滑动开始
                Log.d(TAG, "onScrollBegin: ");
                float x = toX(e.getX()), y = toY(e.getY());
                mLastX = x;
                mLastY = y;
                invalidate(); // 刷新
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // 滑动中
                Log.d(TAG, "onScroll: " + e2.getX() + " " + e2.getY());
                float x = toX(e2.getX()), y = toY(e2.getY());
                mLastX = x;
                mLastY = y;
                invalidate(); // 刷新
                return true;
            }

            @Override
            public void onScrollEnd(MotionEvent e) { // 滑动结束
                Log.d(TAG, "onScrollEnd: ");
                float x = toX(e.getX()), y = toY(e.getY());
                invalidate(); // 刷新
            }

        });

        // 针对涂鸦的手势参数设置
        // 下面两行绘画场景下应该设置间距为大于等于1，否则设为0双指缩放后抬起其中一个手指仍然可以移动
        mTouchGestureDetector.setScaleSpanSlop(1); // 手势前识别为缩放手势的双指滑动最小距离值
        mTouchGestureDetector.setScaleMinSpan(1); // 缩放过程中识别为缩放手势的双指最小距离值
        mTouchGestureDetector.setIsLongpressEnabled(false);
        mTouchGestureDetector.setIsScrollAfterScaled(false);
    }

//    public void setBitmap(Bitmap bitmap) {
//        mBitmap = bitmap;
//    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) { //view绘制完成时 大小确定
        super.onSizeChanged(width, height, oldw, oldh);
        if (mBitmap != null) {
            setBackground(mBitmap);
        }
        invalidate();
    }

    public void setBackground(Bitmap mBitmap) {
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        float centerWidth, centerHeight;
        // 1.计算使图片居中的缩放值
        if (nw > nh) {
            mBitmapScale = 1 / nw;
            centerWidth = getWidth();
            centerHeight = (int) (h * mBitmapScale);
        } else {
            mBitmapScale = 1 / nh;
            centerWidth = (int) (w * mBitmapScale);
            centerHeight = getHeight();
        }
        Log.e(TAG, "nw : " + nw + " nh : " + nh + " centerWidth : " + centerWidth + " centerHeight : " + centerHeight);
        // 2.计算使图片居中的偏移值
        mBitmapTransX = (getWidth() - centerWidth) / 2f;
        mBitmapTransY = (getHeight() - centerHeight) / 2f;
        this.mBitmap = mBitmap;
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - mBitmapTransX) / mBitmapScale;
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - mBitmapTransY) / mBitmapScale;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean consumed = mTouchGestureDetector.onTouchEvent(event); // 由手势识别器处理手势
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系(toX toY)
        canvas.translate(mBitmapTransX, mBitmapTransY);
        canvas.scale(mBitmapScale, mBitmapScale);


        if (mBitmap != null)
            // 绘制图片
            canvas.drawBitmap(mBitmap, 0, 0, null);
        invalidate();
    }

    /**
     * 封装轨迹对象
     */
    private static class PathItem {
        Path mPath = new Path(); // 涂鸦轨迹
        float mX, mY; // 轨迹偏移值
    }
}

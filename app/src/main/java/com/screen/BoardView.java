package com.screen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.bright.course.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;

public class BoardView extends SurfaceView implements SurfaceHolder.Callback, OnScaleGestureListener,
        OnTouchListener {
    private static final String TAG = BoardView.class.getSimpleName();
    private Context mContext;

    private float mCenterScale; // 图片适应屏幕时的缩放倍数
    private int mCenterHeight, mCenterWidth;// 图片适应屏幕时的大小（View窗口坐标系上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在适应屏幕时，位于居中位置的偏移（View窗口坐标系上的偏移）
    private Bitmap mBitmap;
    // 缩放手势操作相关
    private Float mLastFocusX;
    private Float mLastFocusY;
    private float mTouchCentreX, mTouchCentreY;

    public enum ZoomFitType {
        FIT_SCREEN, FIT_WIDTH, FIT_HEIGHT
    }

    public enum MotionType {
        NOMALPEN, MARKPEN, FINGER, ERASER
    }

    private MotionType mMotionType = MotionType.NOMALPEN;

    private ZoomFitType mZoomFitType = ZoomFitType.FIT_WIDTH;
    private EraserListener mEraserListener;
    private FingerListener mFingerListener;
    private ControllerListener mControllerListener;
    private boolean isShow = false; //控制栏是否显示

    public static final float SCALE_MAX = 3.0f;
    private static final float SCALE_MID = 2.0f;

    private SampleThread mSampleThread;

    private Bitmap background = null;
    private Canvas bufferCanvas;
    private Bitmap bufferBitmap;


    private float paper_scale = 11, offsetX = 0, offsetY = 0;

    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float initScale = 1.0f;
    private boolean once = true;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private Matrix mMatrix = new Matrix();
    /**
     * 用于双击检测
     */
    private GestureDetector mGestureDetector;

    private int mTouchSlop;


    private boolean isCanDrag;
    private int lastPointerCount;

    private boolean isCheckTopAndBottom = true;
    private boolean isCheckLeftAndRight = true;
    private float mLastX, mLastY;

    private int screenWidth, screenHeight;


    public BoardView(Context context) {
        this(context, null);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
        mGestureDetector = new GestureDetector(context,
                new SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        mMatrix = new Matrix();
                        invalidate();
                        return true;
                    }
                });
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    private void init() {
        getHolder().addCallback(this);
        mSampleThread = new SampleThread(this.getHolder(), this);
        bufferCanvas = new Canvas();

        this.setOnTouchListener(this);
    }

    public void setBackground(Bitmap mBitmap) {
        if (mBitmap != null) {
            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            float nw = w * 1f / getWidth();
            float nh = h * 1f / getHeight();
            if (nw > nh) {
                mCenterScale = 1 / nw;
                mCenterWidth = getWidth();
                mCenterHeight = (int) (h * mCenterScale);
            } else {
                mCenterScale = 1 / nh;
                mCenterWidth = (int) (w * mCenterScale);
                mCenterHeight = getHeight();
            }
            mCentreTranX = (getWidth() - mCenterWidth) / 2f;
            mCentreTranY = (getHeight() - mCenterHeight) / 2f;
        }
        this.mBitmap = mBitmap;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mLastFocusX = null;
        mLastFocusY = null;
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        mTouchCentreX = detector.getFocusX();
        mTouchCentreY = detector.getFocusY();
        if (mLastFocusX != null && mLastFocusY != null) {
            float dx = mTouchCentreX - mLastFocusX;
            float dy = mTouchCentreY - mLastFocusY;
            if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {

            }
        }
        if (getDrawable() == null)
            return true;

        /**
         * 缩放的范围控制
         */
        if ((scale < SCALE_MAX && scaleFactor > 1.0f)
                || (scale > initScale && scaleFactor < 1.0f)) {
            /**
             * 最大值最小值判断
             */
            if (scaleFactor * scale < initScale) {
                scaleFactor = initScale / scale;
            }
            if (scaleFactor * scale > SCALE_MAX) {
                scaleFactor = SCALE_MAX / scale;
            }

            /**
             * 设置缩放比例
             */
            mMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(), detector.getFocusY());
        }
        return true;

    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }


    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }


    public void setEraserListener(EraserListener listener) {
        this.mEraserListener = listener;
    }

    public void setFingerListener(FingerListener listener) {
        this.mFingerListener = listener;
    }

    public void setControllerListener(ControllerListener listener) {
        this.mControllerListener = listener;
    }

    public void setMotionType(MotionType motionType) {
        mMotionType = motionType;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mMotionType == MotionType.NOMALPEN || mMotionType == MotionType.MARKPEN) {
            nomalEvent(event);
        } else if (mMotionType == MotionType.ERASER) {
            eraserEvent(event);
        } else if (mMotionType == MotionType.FINGER) {
//            fingerEvent(event);
            nomalEvent(event);
        }
        return true;
    }


    private void nomalEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        lastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                }
                if (isShow) {
                    isShow = false;
                } else {
                    isShow = true;
                }
//                mControllerListener.controller(isShow);
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                }
                Log.e(TAG, "ACTION_MOVE");
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }
                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        // 如果宽度小于屏幕宽度，则禁止左右移动
                        if (rectF.width() < getWidth()) {
                            dx = 0;
                            isCheckLeftAndRight = false;
                        }
                        // 如果高度小雨屏幕高度，则禁止上下移动
                        if (rectF.height() < getHeight()) {
                            dy = 0;
                            isCheckTopAndBottom = false;
                        }

                        mMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                    }
                }
                mLastX = x;
                mLastY = y;
                Log.e("Rectf", rectF.toString());
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "ACTION_UP");
                lastPointerCount = 0;
                break;
        }
    }

    private void eraserEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;

                if (isShow) {
                    isShow = false;
                } else {
                    isShow = true;
                }
//                mControllerListener.controller(isShow);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                float dx = Math.abs(x - mLastX);
                float dy = Math.abs(y - mLastY);
                if (dx >= 4 || dy >= 4) {
                    float x1 = Math.min(x, mLastX);
                    float y1 = Math.min(y, mLastY);
                    float x2 = Math.max(x, mLastX);
                    float y2 = Math.max(y, mLastY);
                    mEraserListener.eraserPos(exchangeDots(x1, y1)[0], exchangeDots(x1, y1)[1], exchangeDots(x2, y2)[0], exchangeDots(x2, y2)[1]);
                }
                break;

        }
    }

    private void fingerEvent(MotionEvent event) {
        int fingerId = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float mX1 = event.getX();
                float mY1 = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float mX2 = event.getX();
                float mY2 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float mX3 = event.getX();
                float mY3 = event.getY();
                break;
        }
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        mMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }


    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();
        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();

        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > 0 && isCheckTopAndBottom) {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom) {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight) {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && rect.right > 0 && isCheckLeftAndRight) {
            deltaX = viewWidth - rect.right;
        }
        mMatrix.postTranslate(deltaX, deltaY);
        Log.e("DXDY", "deax:" + deltaX + " deay:" + deltaY);

    }

    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }




    private Drawable getDrawable() {
        return new BitmapDrawable(getResources(), background);
    }


    public Bitmap readBitMap(int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


//    public void setPageSize(float width, float height, Bitmap buffer) {
//        if (getWidth() <= 0 || getHeight() <= 0 || width <= 0 || height <= 0) {
//            return;
//        }
//        float width_ratio = getWidth() / width;
//        float height_ratio = getHeight() / height;
//
//        if (mZoomFitType == ZoomFitType.FIT_SCREEN)
//            paper_scale = Math.min(width_ratio, height_ratio);
//        else if (mZoomFitType == ZoomFitType.FIT_WIDTH)
//            paper_scale = width_ratio;
//        else
//            paper_scale = height_ratio;
//
//        final int docWidth = (int) (width * paper_scale);
//        final int docHeight = (int) (height * paper_scale);
//
//        int mw = getWidth() - docWidth;
//        final int mh = getHeight() - docHeight;
//
//        if (mZoomFitType == ZoomFitType.FIT_SCREEN) {
//            offsetX = mw / 2;
//            offsetY = mh / 2;
//        } else {
//            offsetX = 0;
//            offsetY = 0;
//        }
//        Bitmap old = readBitMap(R.drawable.ic_action_dark_aspect_ratio);
//        if (buffer == null) {
//            background = Bitmap.createScaledBitmap(old, 1, 1, true);
//            background = old;
//            if (null != old) {
//                old.recycle();
//}
//        } else {
//            background = Bitmap.createScaledBitmap(buffer, docWidth, docHeight, true);
//            background = buffer;
//        }

//        bufferBitmap = background;
//        background = buffer;

//    }

    /**
     * turn the screen position into paper original position
     *
     * @param potX 屏幕坐标x
     * @param potY 屏幕坐标y
     *             by invert original matrix
     */
    public float[] exchangeDots(float potX, float potY) {
        float d[] = new float[2];
        float[] ffs = new float[]{potX, potY};
        Matrix matrix = new Matrix();
        mMatrix.invert(matrix);
        matrix.mapPoints(ffs);
        d[0] = ffs[0] / paper_scale;
        d[1] = ffs[1] / paper_scale;
        return d;
    }


    /**
     * 根据获取到到url进行图片加载
     */
    public void changePage(File urlPath, final float width, final float height) {
        Uri uri = Uri.fromFile(urlPath);
        Glide.with(getContext().getApplicationContext())
                .asBitmap()
                .load(uri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        setBackground(mBitmap);
//                        setPageSize(width, height, resource);
                    }
                });

    }


    public interface EraserListener {
        void eraserPos(float left, float top, float right, float bottom);
    }

    public interface FingerListener {
//        void fingerPos(Dot dot);
    }

    public interface ControllerListener {
        void controller(boolean isShow);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
//        setPageSize(88.5f, 125f, background);
        setBackground(mBitmap);
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        mSampleThread = new SampleThread(getHolder(), this);
        mSampleThread.setRunning(true);
//        setPageSize(88.5f, 125f, null);
        if (mBitmap != null)
            setBackground(mBitmap);
        mSampleThread.start();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mSampleThread.setRunning(false);
        boolean retry = true;

        while (retry) {
            try {
                mSampleThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }
    }

    public class SampleThread extends Thread {
        private SurfaceHolder surfaceholder;
        private BoardView mSampleiView;
        private boolean running = false;

        public SampleThread(SurfaceHolder surfaceholder, BoardView mView) {
            this.surfaceholder = surfaceholder;
            this.mSampleiView = mView;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            setName("SampleThread");

            Canvas mCanvas;

            while (running) {
                mCanvas = null;

                try {
                    mCanvas = surfaceholder.lockCanvas(); // lock canvas

                    synchronized (surfaceholder) {
                        if (mCanvas != null) {
                            mSampleiView.drawstrokes(mCanvas);
                        }
                    }
                } finally {
                    if (mCanvas != null) {
                        surfaceholder.unlockCanvasAndPost(mCanvas); // unlock
                        // canvas
                    }
                }
            }
        }

    }

    protected void drawstrokes(Canvas canvas) {
        canvas.drawColor(Color.LTGRAY);
        canvas.concat(mMatrix);
        canvas.drawBitmap(background, offsetX, offsetY, null);

    }

}

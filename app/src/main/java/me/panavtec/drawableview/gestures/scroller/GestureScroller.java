package me.panavtec.drawableview.gestures.scroller;

import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;

public class GestureScroller implements GestureScrollListener.OnGestureScrollListener {

    private final ScrollerListener listener;
    private boolean justBitmapModel;
    private float canvasWidth;
    private float canvasHeight;

    private RectF viewRect = new RectF();
    private RectF canvasRect = new RectF();

    public GestureScroller(final ScrollerListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        if (hasTwoFingers(e2) || justBitmapModel) {
//            Log.e("onScroll"," viewRect.left : " +viewRect.left+ " viewRect.top : " +viewRect.top + " viewRect.right :" +viewRect.right+"viewRect.bottm : " +viewRect.bottom + " x : " + distanceX + " y: " + distanceY);
            float y = viewRect.bottom + distanceY;
            float x = viewRect.left + distanceX;
            setViewportBottomLeft(x, y);
        }
        return true;
    }

    public void setCanvasBounds(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        canvasRect.right = canvasWidth;
        this.canvasHeight = canvasHeight;
        canvasRect.bottom = canvasHeight;
//        Log.e("scoller", " canvasbounds :  canvasRectwidth : " + canvasRect.width() + " canvasheight : " + canvasHeight + " canvasRect : " + canvasRect);
        listener.onCanvasChanged(canvasRect);
    }

    public void setViewBounds(int viewWidth, int viewHeight) {
        viewRect.right = viewWidth;
        viewRect.bottom = viewHeight;
//        Log.e("scoller", " ViewBounds :  viewRectwidth : " + viewRect.width() + " viewRectheight : " + viewRect.height() + " viewRect : " + viewRect);
        listener.onViewPortChange(viewRect);
    }

    public void onScaleChange(float scaleFactor) {
        canvasRect.right = canvasWidth * scaleFactor;
        canvasRect.bottom = canvasHeight * scaleFactor;
//        Log.e("scoller", " ScaleChange :  canvasRect : " + canvasRect + " viewRect : " + viewRect + " scaleFactor : " + scaleFactor);
        listener.onCanvasChanged(canvasRect);
    }


    public void setJustBitmapModel(boolean justBitmapModel) {
        this.justBitmapModel = justBitmapModel;
    }

    private void setViewportBottomLeft(float x, float y) {
        float viewWidth = viewRect.width();
        float viewHeight = viewRect.height();
        float left = Math.max(0, Math.min(x, canvasRect.width() - viewWidth));
        float bottom = Math.max(0 + viewHeight, Math.min(y, canvasRect.height()));
        float top = bottom - viewHeight;
        float right = left + viewWidth;
        viewRect.set(left, top, right, bottom);
//        Log.e("scoller", " setViewportBottomLeft :  viewRect : " + viewRect);
        listener.onViewPortChange(viewRect);
    }

    private boolean hasTwoFingers(MotionEvent e) {
        Log.d("hasTwoFingers", "count:" + e.getPointerCount());
        return e.getPointerCount() == 2;
    }
}

package com.bright.course.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by jinbangzhu on 8/4/15.
 */
public class ViewHelper {

    public static Bitmap getViewBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);

        return view.getDrawingCache();
    }

    public static void captureViewWithDrawingCache(View view, String savePath) {
        view.setDrawingCacheEnabled(true);

        // this is the important code :)
        // Without it the view will have a dimension of 0,0 and the bitmap will be null
//        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.buildDrawingCache(true);

        Bitmap b = view.getDrawingCache();

        try {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(savePath));
//                b = resizeImage(b, width, height);
                b.compress(Bitmap.CompressFormat.PNG, 80, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.flush();
                    out.close();
                    b.recycle();
                } catch (Throwable ignore) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.setDrawingCacheEnabled(false); // clear drawing cache
    }


    public static void saveBitmap(Bitmap bitmap, String path) {

        try {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(path));
//                b = resizeImage(b, width, height);
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.flush();
                    out.close();
                    bitmap.recycle();
                } catch (Throwable ignore) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //使用Bitmap加Matrix来缩放
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }

    public static void clear(View v) {
        ViewCompat.setAlpha(v, 1);
        ViewCompat.setScaleY(v, 1);
        ViewCompat.setScaleX(v, 1);
        ViewCompat.setTranslationY(v, 0);
        ViewCompat.setTranslationX(v, 0);
        ViewCompat.setRotation(v, 0);
        ViewCompat.setRotationY(v, 0);
        ViewCompat.setRotationX(v, 0);
        // @TODO https://code.google.com/p/android/issues/detail?id=80863
//        ViewCompat.setPivotY(v, v.getMeasuredHeight() / 2);
        if (Build.VERSION.SDK_INT >= 11)
            v.setPivotY(v.getMeasuredHeight() / 2);

        ViewCompat.setPivotX(v, v.getMeasuredWidth() / 2);
        ViewCompat.animate(v).setInterpolator(null);
    }
}

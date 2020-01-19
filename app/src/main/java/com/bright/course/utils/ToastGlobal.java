package com.bright.course.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bright.course.App;
import com.bright.course.R;

import java.lang.ref.WeakReference;

/**
 * Created by jinbangzhu on 12/05/2017.
 */

public class ToastGlobal {
    private static WeakReference<Toast> toastWeakReference;
    private static Toast toastShort;

    public static void showToast(String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        Toast toast = null;
        if (toastWeakReference != null) {
            toast = toastWeakReference.get();
        }
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(App.Companion.getInstance(), msg, Toast.LENGTH_LONG);
        toast.show();
        toastWeakReference = new WeakReference<>(toast);
    }

    public static void showToast(String msg, int duration) {
        if (TextUtils.isEmpty(msg))
            return;

        Toast toast = null;
        if (toastWeakReference != null) {
            toast = toastWeakReference.get();
        }
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(App.Companion.getInstance(), msg, duration);
        toast.show();
        toastWeakReference = new WeakReference<>(toast);
    }

    public static void customMsgToastShort(Context context, String message) {
        if (context == null) return;
        if (null == toastShort) {

            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.toast_layout, null);
            toastShort = new Toast(context);
//            toastShort.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            // 设置土司显示在屏幕的位置
            toastShort.setGravity(Gravity.FILL_HORIZONTAL|Gravity.BOTTOM,0,50);
            toastShort.setDuration(Toast.LENGTH_SHORT);
            toastShort.setView(layout);
            toastShort.getView().setAlpha(0.7f);
        }
        toastShort.setText(message);
        toastShort.show();
    }
}

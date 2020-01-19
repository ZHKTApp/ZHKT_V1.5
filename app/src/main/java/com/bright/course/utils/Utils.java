package com.bright.course.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.bright.course.App;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by helloworld on 2019/8/14.
 */

public class Utils {
    public static int calcStatusBarHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static List<Uri> convertList(List<String> data) {
        List<Uri> list = new ArrayList<>();
        for (String d : data) list.add(Uri.parse(d));
        return list;
    }

    public static void updateOptions(RequestOptions options, String url, long modified, ImageView imageView,Context mContext) {
        try {
            String tail = url.substring(url.lastIndexOf(".") + 1).toLowerCase();
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(tail);
            options.signature(new MediaStoreSignature(type, modified, 0));
            Glide.with(mContext.getApplicationContext()).load(url).apply(options).into(imageView);
        } catch (Exception e) {
        }
    }
}

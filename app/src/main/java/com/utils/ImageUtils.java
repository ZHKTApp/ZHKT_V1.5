package com.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jinbangzhu on 9/11/15.
 */
public class ImageUtils {

    public static void saveByteToFile(byte[] bytes, String savePath) {


        int quality = 100;
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
//        bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片

        try {
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(new File(savePath));
                out.write(bytes);
//                bitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (Throwable ignore) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmapToFile(Bitmap bitmap, String savePath, int maxSize) {
        if (null == bitmap) return;
        if (TextUtils.isEmpty(savePath)) return;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int quality = 100;
        while (baos.toByteArray().length / 1024 > maxSize) {    //循环判断如果压缩后图片是否大于 maxSize kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//这里压缩options%，把压缩后的数据存放到baos中

            if (quality >= 5) {
                quality -= 5;//每次都减少5
            } else {
                break;
            }
        }
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
//        bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片

        try {
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(new File(savePath));
                baos.writeTo(out);
//                bitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.flush();
                    out.close();
                    bitmap.recycle();
                } catch (Throwable ignore) {
                    bitmap.recycle();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            bitmap.recycle();
        }
    }


    public static int getBitmapHeight(String file) {
        int outWidth;
        int outHeight;
        float scae = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeFile(file, options); // 此时返回bm为空
        options.inJustDecodeBounds = false;
        // 计算缩放比
        return options.outHeight;
    }

    /**
     * 压缩图片，取到内存，在缩放指定大小，
     *
     * @param file
     * @param width
     * @return
     */
    public static Bitmap decodeFile(String file, final int width, final int height) {
        int outWidth;
        int outHeight;
        float scae = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeFile(file, options); // 此时返回bm为空
        options.inJustDecodeBounds = false;
        // 计算缩放比
        outWidth = options.outWidth;
        outHeight = options.outHeight;

//        int be = (int) (options.outHeight > options.outWidth ? options.outHeight / (float) height : options.outWidth / (float) width);
//        if (be <= 0)
//            be = 1;

        int be = 1;//be=1表示不缩放
        if (outWidth > outHeight && outWidth > width) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (options.outWidth / width);
        } else if (outWidth < outHeight && outHeight > height) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (options.outHeight / height);
        }
        if (be <= 0) be = 1;

        options.inSampleSize = be;

        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        // 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
        bitmap = BitmapFactory.decodeFile(file, options);

//        if (outWidth > outHeight && outHeight > height) {
//            scae = height / outHeight;
//        } else if (outWidth > height) {
//            scae = height / outWidth;
//        }
//        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (outWidth * scae), (int) (outHeight * scae), true); // 按指定大小转换

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != exif) {

            if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                bitmap = rotate(bitmap, 90);
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                bitmap = rotate(bitmap, 270);
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                bitmap = rotate(bitmap, 180);
            }
        }

        return bitmap;
    }


    private static long getDateTime(ExifInterface exifInterface) {
        String dateTimeString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        if (dateTimeString == null) return -1;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            // The exif field is in local time. Parsing it as if it is UTC will yield time
            // since 1/1/1970 local time
            Date datetime = simpleDateFormat.parse(dateTimeString);
            if (datetime == null) return -1;
            long msecs = datetime.getTime();

            String subSecs = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME);
            if (subSecs != null) {
                try {
                    long sub = Long.parseLong(subSecs);
                    while (sub > 1000) {
                        sub /= 10;
                    }
                    msecs += sub;
                } catch (NumberFormatException e) {
                    // Ignored
                }
            }
            return msecs;
        } catch (IllegalArgumentException e) {
            return -1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    public static Bitmap changeBitmapColor(Bitmap sourceBitmap, int color) {

        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, color);
        p.setColorFilter(filter);
        p.setColor(color);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        return resultBitmap;
    }

}

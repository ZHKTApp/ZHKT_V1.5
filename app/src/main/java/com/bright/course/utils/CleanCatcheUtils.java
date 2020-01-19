package com.bright.course.utils;
/*
 *  缓存清理工具类
 *  /storage/emulated/0   sd卡目录
 *
 *   App缓存主要保存在两个地方：
 *  1> /data/data/应用包名/com.cytmxk.test/cache
 *  2> /storage/emulated/0/Android/data/应用包名/cache
 *

 */

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

import java.io.File;

public class CleanCatcheUtils {

    private CleanCatcheUtils() {

    }

    // 清理缓存
    public static boolean clear(Context context) {
        return delFile(context.getCacheDir()) && delFile(context.getExternalCacheDir());
    }

    //递归删除文件
    private static boolean delFile(File file) {
        if (file.isDirectory() && file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    delFile(f);
                }
                f.delete();
            }
        }
        return file.delete(); // 删除最外层
    }

    /**
     * 获取App的缓存大小(单位 byte)
     */
    public static String getCacheSize(Context context) {
        long result = 0;
        File dataDataCache = context.getCacheDir();
        if (dataDataCache.exists()) {
            result += getFolderSize(dataDataCache);
        }

        // sd 卡挂载
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File SDCache = context.getExternalCacheDir();
            if (null != SDCache && SDCache.exists()) {
                result += getFolderSize(new File(SDCache.getAbsolutePath()));
            }
        }

        return formatSize(context, result);
    }

    // 获取某个文件夹下面所有文件大小
    private static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File f : fileList) {
                if (f.isDirectory()) {
                    size += getFolderSize(f);
                } else {
                    size += f.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    //格式化
    private static String formatSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }
}
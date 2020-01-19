package com.bright.course.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    //    public static String PROJECT_PATH=Environment.getExternalStorageDirectory().getPath() + "/downOfYs";
    public static String PROJECT_PATH = Environment.getExternalStorageDirectory().getPath() + "/DCIM";
    public static String PATH = Environment.getExternalStorageDirectory().getPath() + "/";

    public static boolean createDirs(String filePath) {
        return createDirs(new File(filePath));
    }

    public static boolean createDirs(File f) {
        if (f == null)
            return false;
        return !f.exists() ? f.mkdirs() : false;
    }

    public static boolean createDirs(String path, String var0) {
        File var1;
        return !(var1 = new File(path + File.separator + var0)).exists() ? var1.mkdirs() : false;
    }

    public static boolean exists(String filePath) {
        return exists(new File(filePath));
    }

    public static File getFilePath(String pathName, String fileName) {
        File rootFile = new File(PROJECT_PATH);
        if (!rootFile.exists())
            rootFile.mkdirs();
        File dirFiles = new File(rootFile, pathName);
        if (rootFile == null) {
            Log.e("FileUtils : ","未授予读写权限");
            return null;
        }
        if (!dirFiles.exists())
            dirFiles.mkdirs();
        File filePath = new File(dirFiles,fileName);
        return filePath;
    }
    public static boolean isFilesCreate(String pathName, String fileName) {
        File file = new File(PROJECT_PATH);
        if (!file.exists()) file.mkdirs();
        File dirFile = new File(file, pathName);
        if (!dirFile.exists()) dirFile.mkdirs();
        File[] files = dirFile.listFiles();
        if (dirFile == null) {
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            String absolutePath = files[i].getAbsolutePath();
            String[] splits = absolutePath.split("/");
            String name = splits[splits.length - 1];
            if (name.equals(fileName)) {
                return true;
            }
        }
        return false;
    }
    //获取文件夹下的文件
    public static List<String> getFilesAllName() {
        File file = new File(PROJECT_PATH);
        if (!FileUtils.exists(file)) {
            FileUtils.createDirs(file);
        }
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String absolutePath = files[i].getAbsolutePath();
            String[] splits = absolutePath.split("/");
            String name = splits[splits.length - 1];
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    //获取文件夹下的文件
    public static List<String> getFilesAllName(String path) {
        File file = new File(PATH + path);
        if (!FileUtils.exists(file)) {
            FileUtils.createDirs(file);
        }
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String absolutePath = files[i].getAbsolutePath();
            String[] splits = absolutePath.split("/");
            String name = splits[splits.length - 1];
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    public static void creatDownFile() {
        File file = new File(PROJECT_PATH);
        if (!FileUtils.exists(file)) {
            FileUtils.createDirs(file);
        }
    }

    ;

    //查看是否有该文件
    public static boolean getFilesbolen(String filename) {
        File file = new File(PROJECT_PATH);
        if (!FileUtils.exists(file)) {
            FileUtils.createDirs(file);
        }
        File[] files = file.listFiles();
        if (files == null) {
            Log.e("error", "空目录");
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            String absolutePath = files[i].getAbsolutePath();
            String[] splits = absolutePath.split("/");
            String name = splits[splits.length - 1];
            if (name.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(File f) {
        if (f != null)
            return f.exists();
        return false;
    }

    public static boolean isDirectory(String filePath) {
        return isDirectory(new File(filePath));
    }

    public static boolean isDirectory(File f) {
        return f.isDirectory();
    }

    public static void openFile(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
        try {
            context.startActivity(intent); //这里最好try一下，有可能会报错。 //比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
    public static String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
//    private static String getMIMEType(File file) {
//
//        String type = "*/*";
//        String fName = file.getName();
//        //获取后缀名前的分隔符"."在fName中的位置。
//        int dotIndex = fName.lastIndexOf(".");
//        if(dotIndex < 0) {
//            return type;
//        }
//        /* 获取文件的后缀名*/
//        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
//        if(TextUtils.isEmpty(end))
//            return type;
//        //在MIME和文件类型的匹配表中找到对应的MIME类型。
//        type = MIME_MapTable.get(end);
//        return type;
//    }
}

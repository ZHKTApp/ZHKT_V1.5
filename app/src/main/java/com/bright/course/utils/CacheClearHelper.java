package com.bright.course.utils;

import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

public class CacheClearHelper {
    public static void clearCache(Context context) {

        try {
            CachePackageDataObserver cachePackageDataObserver = new CachePackageDataObserver();
            PackageManager packageManager = context.getPackageManager();
            Method localMethod = packageManager.getClass().getMethod("freeStorageAndNotify", Long.TYPE,
                    IPackageDataObserver.class);
            Long localLong = Long.valueOf(getEnvironmentSize() - 1L);
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = localLong;
            arrayOfObject[1] = cachePackageDataObserver;
            localMethod.invoke(packageManager,arrayOfObject[0],arrayOfObject[1]);
//            localMethod.invoke(packageManager, localLong, new IPackageDataObserver.Stub() {
//
//                @Override
//                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class CachePackageDataObserver extends IPackageDataObserver.Stub{

        @Override
        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
            Log.e("size", "packageName : " + packageName + " succeeded : " + succeeded);
        }
    }
    private static long getEnvironmentSize() {
        File localFile = Environment.getDataDirectory();
        long l1;
        if (localFile == null)
            l1 = 0L;
        while (true) {
            String str = localFile.getPath();
            StatFs localStatFs = new StatFs(str);
            long l2 = localStatFs.getBlockSize();
            l1 = localStatFs.getBlockCount() * l2;
            return l1;
        }
    }
}

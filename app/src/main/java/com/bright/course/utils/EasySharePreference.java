package com.bright.course.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kim on 3/3/15.
 */
public class EasySharePreference {
    public static final String PREFERENCE_SIXFOOT = "smartClass";

    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor sp_editor = null;

    public static SharedPreferences getPrefInstance(final Context c) {
        if (sharedPreferences == null) {
            sharedPreferences = c.getSharedPreferences(PREFERENCE_SIXFOOT, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getEditorInstance(final Context c) {
        if (sp_editor == null) {
            sp_editor = getPrefInstance(c).edit();
        }
        return sp_editor;
    }

    /**
     * str
     */
    public static void saveStr(Context context, String strkey, String strvalue) {
        getEditorInstance(context).putString(strkey, strvalue).commit();
    }

    public static String getStr(Context context, String strkey) {
        return getPrefInstance(context).getString(strkey, "");
    }
}

package com.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.bright.course.http.response.ResponseErrorCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ErrorCodeUtils {
    public static String getAssetJson(Context context,String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(fileName)));
            String jsonLine;
            while ((jsonLine = reader.readLine()) != null) {
                stringBuilder.append(jsonLine);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String getErrorMassage(List<ResponseErrorCode> list,String code){
        if (list.size()!=0&&list!=null){
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(code)) {
                    return list.get(i).getName();
                }
            }
        }
        return "未知错误";
    };

    public static String discernErrorCode(Context mContext,String fileName,String code){
        String codeJson = getAssetJson(mContext,fileName);
        Type type = new TypeToken<ArrayList<ResponseErrorCode>>(){}.getType();
        List<ResponseErrorCode> list = new Gson().fromJson(codeJson,type);
        String msg = getErrorMassage(list,code);
        return msg;
    }
}

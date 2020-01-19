package com.bright.course.http;

import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bright.course.App;
import com.bright.course.http.response.ResponseLogin;
import com.bright.course.utils.EasySharePreference;
import com.google.gson.Gson;

/**
 * Created by kim on 09/02/2018.
 */

public class UserInfoInstance extends LiveData<ResponseLogin> {
    public static final UserInfoInstance instance = new UserInfoInstance();
    public static final String KEY_FOR_USER_INFO = "userInfo";

    private UserInfoInstance() {
    }

    /**
     * 更新用户信息
     *
     * @param info 登录用户信息
     */
    public void updateUserInfo(ResponseLogin info) {
        if (null != info) {
            String jsonVal = new Gson().toJson(info);
            SharedPreferences.Editor editor = EasySharePreference.getEditorInstance(App.Companion.getInstance());
            editor.putString(KEY_FOR_USER_INFO, jsonVal);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = EasySharePreference.getEditorInstance(App.Companion.getInstance());
            editor.putString(KEY_FOR_USER_INFO, null);
            editor.apply();
        }

        setValue(info);
    }

    /**
     * 获取用户信息
     *
     * @return 登录用户信息
     */
    public ResponseLogin getUserInfo() {
        ResponseLogin info = getValue();

        if (info == null) {
            String userInfoStr = EasySharePreference.getPrefInstance(App.Companion.getInstance()).getString(KEY_FOR_USER_INFO, null);
            if (TextUtils.isEmpty(userInfoStr)) {
                info = null;
            } else {
                info = new Gson().fromJson(userInfoStr, ResponseLogin.class);
                setValue(info);
            }
        }

        return info;
    }

    public String getToken() {
        ResponseLogin user = getUserInfo();
        return user != null ? user.getToken() : null;
    }

    /**
     * 判断是否登录
     *
     * @return bool
     */
    public boolean isGuestUser() {
        return getUserInfo() == null || TextUtils.isEmpty(getUserInfo().getToken());
    }

    @Override
    protected void onActive() {
        super.onActive();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }
}

package com.bright.course;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by Administrator on 2018/1/12.
 */
public class BaseDialogFragment extends DialogFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            onCurrentAttach(context);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            onCurrentAttach(activity);
        }
    }

    /**
     * 重写此方法替代onAttach()高低版本不兼容
     * @param mContext
     */
    protected void onCurrentAttach(Context mContext){

    }

    /**
     * 判断弹窗是否显示
     * @return
     */
    public boolean isShowing(){
        return getDialog() != null && getDialog().isShowing();
    }

    /**
     * 显示DialogFragment(注此方法会衍生出状态值问题(本人在正常使用时并未出现过))
     * @param manager
     * @param tag
     * @param isResume 在Fragment中使用可直接传入isResumed()
     *                 在FragmentActivity中使用可自定义全局变量 boolean isResumed 在onResume()和onPause()中分别传人判断为true和false
     */
    public void show(FragmentManager manager, String tag, boolean isResume){
        if(!isShowing()){
            if(isResume){
                //正常显示
                if(!isAdded()){
                    show(manager, tag);
                }else{
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.show(this);
                    ft.commit();
                }
            }else{
                //注 此方法会衍生出一些状态问题,慎用（在原代码中 需要设置  mDismissed = false 和 mShownByMe = true 并未在此引用到,如果需要用到相关判断值,此方法不可用）
                FragmentTransaction ft = manager.beginTransaction();
                if(!isAdded()){
                    ft.add(this, tag);
                }else{
                    ft.show(this);
                }
                ft.commitAllowingStateLoss();
            }
        }
    }

    /**
     * 关闭DialogFragment
     * @param isResume 在Fragment中使用可直接传入isResumed()
     *                 在FragmentActivity中使用可自定义全局变量 boolean isResumed 在onResume()和onPause()中分别传人判断为true和false
     */
    public void dismiss(boolean isResume){
        if(isResume){
            dismiss();
        }else{
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void dismiss() {
        if(isShowing()){
            super.dismiss();
        }
    }

    @Override
    public void dismissAllowingStateLoss() {
        if(isShowing()){
            super.dismissAllowingStateLoss();
        }
    }
}
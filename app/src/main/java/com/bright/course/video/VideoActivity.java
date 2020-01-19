/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bright.course.video;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.bright.course.BaseEventBusActivity;
import com.bright.course.R;
import com.holoview.smcplaysdk.application.HoloPlayer;

public class VideoActivity extends BaseEventBusActivity implements HoloPlayer.OnPlayerStateChange {
    private static final String TAG = "VideoActivity";
    private BroadcastReceiver mReceiver = null;
    private HoloPlayer mHoloPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideSystemUI();
        setContentView(R.layout.activity_player);

        ActionBar actionBar = getSupportActionBar();

        mHoloPlayer = HoloPlayer.GetHoloPlayer();
        if (mHoloPlayer != null) {
            mHoloPlayer.InitPlayer(this, actionBar, R.id.video_view, this);
//            mHoloPlayer.StartPlay("rtmp://live.hkstv.hk.lxdns.com/live/hks");
            mHoloPlayer.StartPlay("udp://225.0.0.1:1234");
        }
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }


    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // only when screen turns on
        if (!ScreenReceiver.wasScreenOn) {
            Log.e("MYAPP", "SCREEN TURNED ON");
        }
        else{
            mHoloPlayer.StartPlay("udp://225.0.0.1:1234");
        }
    }
    @Override
    protected void onPause() {
        if (ScreenReceiver.wasScreenOn) {
            Log.e("MYAPP", "SCREEN TURNED OFF");
        } else {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mHoloPlayer != null) {
            mHoloPlayer.StopPlay();
        }
    }

    public void OnStateChange(HoloPlayer.PlayerState state) {
        Log.e(TAG, "OnStateChange\n");
    }

}

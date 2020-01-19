package com.bright.course

import android.app.Application
import com.bright.course.http.UserInfoInstance

/**
 * Created by kim on 2018/7/26.
 *
 */
class App : Application() {

    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        UserInfoInstance.instance.isGuestUser
    }
}
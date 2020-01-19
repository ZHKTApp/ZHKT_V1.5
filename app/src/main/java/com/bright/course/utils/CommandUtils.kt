package com.bright.course.utils

import android.util.Log

/**
 * Created by kim on 2018/11/5.
 *
 */
class CommandUtils {
    companion object {
        fun exec(code: Int) {
            var proc: Process? = null
            try {
                proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent $code"))
                proc!!.waitFor()
            } catch (ex: Exception) {
                Log.i("TAG", "Could not reboot", ex)
            }
        }
    }
}
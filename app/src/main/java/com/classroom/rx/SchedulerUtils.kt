package com.rztop.classroom.rx

import com.rztop.classroom.rx.scheduler.IoMainScheduler


/**
 * Created by wyq on 2018/9/25.
 */
object SchedulerUtils {

    fun <T> ioToMain(): IoMainScheduler<T> {
        return IoMainScheduler()
    }

}
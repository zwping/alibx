package com.zwping.alibx

import android.app.Application

/**
 * alibx功能清单
 * zwping @ 2021/11/24
 */
object Map {

    fun init(app: Application,
             toastOpt: (ToastUtilOption)->Unit={}) {
        ToastUtil.init(app, toastOpt)
    }

}
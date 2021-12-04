package com.zwping.alibx

import android.app.Application
import com.zwping.alibx.Requests.isAppDebug

/**
 * alibx功能清单
 * zwping @ 2021/11/24
 */
object Map {

    fun init(app: Application,
             toastOpt: (ToastUtilOption)->Unit={}) {

        Util.DEBUG = app.isAppDebug()

        ToastUtil.init(app, toastOpt)
    }

}
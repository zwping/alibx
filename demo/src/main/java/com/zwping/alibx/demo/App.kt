package com.zwping.alibx.demo

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.zwping.alibx.BaseApp
import com.zwping.alibx.Map
import com.zwping.alibx.logd

/**
 *
 * zwping @ 2022/1/6
 */
class App: BaseApp() {

    override fun onCreate() {
        super.onCreate()
        Map.globalInit(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        logd("ac创建: ${activity.javaClass.simpleName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        super.onActivityDestroyed(activity)
        logd("ac销毁: ${activity.javaClass.simpleName}")
    }

}
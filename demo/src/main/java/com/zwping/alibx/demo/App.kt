package com.zwping.alibx.demo

import android.app.Application
import com.zwping.alibx.Map

/**
 *
 * zwping @ 2022/1/6
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Map.globalInit(this)
    }

}
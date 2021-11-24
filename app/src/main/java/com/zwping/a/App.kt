package com.zwping.a

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 *
 * zwping @ 2021/11/24
 */
class App : Application() {
    companion object {
        val acs = mutableListOf<Activity>()
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object: ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                acs.add(activity)
                println("created: ${activity.javaClass.simpleName} - ${acs.map { it.javaClass.simpleName }}")
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                acs.remove(activity)
                println("destroy: ${activity.javaClass.simpleName} - ${acs.map { it.javaClass.simpleName }}")
            }

        })
    }

}
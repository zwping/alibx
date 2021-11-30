package com.zwping.a

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.zwping.alibx.*

/**
 *
 * zwping @ 2021/11/24
 */
class App : Application() {
    companion object {
        val acs = mutableListOf<Activity>()
    }

    class SchemeList: SchemeListInterface {
        override val data: HashMap<SchemeStandard, Class<out Activity>> = hashMapOf(
            "alibx://ac/second".scheme() to AcSecond::class.java,
            "alibx://ac/main".scheme() to AcMain::class.java,
            )
        override val dataFunc: HashMap<SchemeStandard, (ctx: Context, extra: Bundle?) -> Unit> = hashMapOf(
            "alibx://ac/second".scheme() to { ctx, extra ->
                if (ctx is BaseAc<*>) ctx.showLoading()
                println(extra)
            }
        )
        override val webBrowser: Class<out Activity>? = null
    }

    override fun onCreate() {
        super.onCreate()

        Scheme.init(this, SchemeList())

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
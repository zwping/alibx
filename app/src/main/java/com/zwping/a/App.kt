package com.zwping.a

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.zwping.alibx.*

/**
 *
 * zwping @ 2021/11/24
 */
class App : BaseApp() {
    companion object {
        val acs = mutableListOf<Activity>()
    }

    override fun onCreate() {
        super.onCreate()

        com.zwping.alibx.Map.globalInit(this,
            schemeList = SchemeList(), schemeToast = {_, msg ->  showToast(msg)},
            placeholder = R.mipmap.ic_launcher, error = R.mipmap.ic_launcher,
        )
        Scheme.addInterceptor(LoginInterceptor())
    }

    class SchemeList: ISchemeList {
        override val data: HashMap<SchemeStandard, Class<out Activity>> = hashMapOf(
            "alibx://ac/second".scheme() to AcSecond::class.java,
            "yikao://test/scanning".scheme() to AcSecond::class.java,
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

    class LoginInterceptor: ISchemeInterceptor{
        override val weight: Int = 10
        override fun process(ctx: Context, scheme: SchemeStandard): Boolean {
            // logd(scheme)
            return false
        }
    }

}
package com.zwping.alibx

import android.app.Application
import android.content.Context
import com.zwping.alibx.Requests.KTX.isAppDebug
import okhttp3.OkHttpClient

/**
 * alibx功能清单
 * zwping @ 2021/11/24
 */
object Map {

    fun globalInit(app: Application,
                   schemeList: ISchemeList? = null,
                   schemeToast: ((ctx: Context, msg: String) -> Unit)? = {_,_ ->},
                   dataStoreName: String = "DataStore",
                   placeholder: Int? = null,
                   error: Int? = null,
                   requests: OkHttpClient.Builder.() -> Unit = {},
                   toastOpt: (ToastUtilOption)->Unit={},
                   stateLayoutCfg: StateLayout.Companion.Cfg = StateLayout.Companion.Cfg()) {

        Util.DEBUG = app.isAppDebug()

        ToastUtil.init(app, toastOpt)

        schemeList?.also { Scheme.init(it, schemeToast) }

        Requests.init(requests)

        DataStoreUtil.NAME = dataStoreName

        ImageLoader.globalPlaceholder = placeholder
        ImageLoader.globalError = error

        StateLayout.init(stateLayoutCfg)
    }

}
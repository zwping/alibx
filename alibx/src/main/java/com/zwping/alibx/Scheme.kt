package com.zwping.alibx

import android.app.Activity
import android.app.ActivityOptions
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Toast
import com.zwping.alibx.LauncherMode.SingleTask
import com.zwping.alibx.LauncherMode.SingleTop
import com.zwping.alibx.OPTAnim.*
import com.zwping.alibx.Scheme.open
import java.util.*

/**
 * schemeUri简单路由 [open]
 * zwping @ 2021/11/18
 */
interface SchemeInterface {

    /*** scheme配置列表 ***/
    var schemeList: SchemeListInterface?
    /*** scheme跳转错误toast [可选/默认系统Toast] ***/
    var warningToast: ((ctx: Context, msg: String)->Unit)?

    fun init(schemeList: SchemeListInterface,
             warningToast: ((ctx: Context, msg: String)->Unit)? = null)


    fun open(ctx: Context?, schemeURL: String?, option: SchemeIntentOption.() -> Unit = {})
    fun open(ctx: Context?, clazz: Class<out Activity>, option: SchemeIntentOption.() -> Unit = {})
}
object Scheme: SchemeInterface {

    var ErrMsg1 = "scheme url is empty!!"
    var ErrMsg2 = "scheme url error!"
    var ErrMsg3 = "scheme list not init!"

    override var schemeList: SchemeListInterface? = null
    override var warningToast: ((ctx: Context, msg: String) -> Unit)? = null

    override fun init(schemeList: SchemeListInterface,
                      warningToast: ((ctx: Context, msg: String) -> Unit)?) {
        this.schemeList = schemeList
        this.warningToast = warningToast
    }

    override fun open(ctx: Context?, schemeURL: String?, option: SchemeIntentOption.() -> Unit) {
        ctx ?: return
        if (schemeURL.isNullOrBlank()) { showToast(ctx, ErrMsg1); return }
        try { open(ctx, scheme = schemeURL.scheme(), null, SchemeIntentOption(ctx).also(option)) }
        catch (e: Exception) { e.printStackTrace(); showToast(ctx, "$ErrMsg2 $schemeURL"); return }
    }
    override fun open(ctx: Context?, clazz: Class<out Activity>, option: SchemeIntentOption.() -> Unit) {
        ctx ?: return
        open(ctx, null, clazz = clazz, SchemeIntentOption(ctx).also(option))
    }

    /* ---------------------- */


    private fun open(ctx: Context, scheme: SchemeStandard?, clazz: Class<out Activity>?, opt: SchemeIntentOption){
        if (scheme == null && clazz == null) return
        var cls = clazz
        if (scheme != null) { // scheme优先级高于clazz
            if (schemeList == null) {
                showToast(ctx, ErrMsg3); return
            }
            if (scheme.uri?.scheme?.startsWith("http")==true || scheme.uri?.scheme?.startsWith("https")==true) {
                if (schemeList?.webBrowser == null) { // 未定制内部WebView则使用系统浏览器打开
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, scheme.uri)); return
                }
                cls = schemeList?.webBrowser
            }
            schemeList?.dataFunc?.filter { it.key.equals(scheme) }?.forEach {
                it.value.invoke(ctx, scheme.extra)
                return  // dataFunc优先级高于data
            }
            schemeList?.data?.filter { it.key.equals(scheme) }?.forEach {
                cls = it.value
            }
        }
        ctx.startActivity(Intent(ctx, cls).also { i ->
            if (scheme != null) i.data = scheme.uri // 携带过去
            scheme?.extra?.also { i.putExtras(it) }
            opt.extra?.also { i.putExtras(it) }
            when(opt.launcherMode) {
                SingleTop -> i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                SingleTask -> i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }, opt.options)

        if (opt.transitionAnimEnterResId != null && opt.transitionAnimExitResId != null) {
            if (ctx is Activity) ctx.overridePendingTransition(opt.transitionAnimEnterResId!!, opt.transitionAnimExitResId!!)
        } else {
            opt.transitionAnim?.also { if (ctx is Activity) ctx.overridePendingTransition(it) }
        }
    }

    private fun showToast(ctx: Context, msg: String) {
        warningToast?.also { it(ctx, msg); return }
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }
}

fun String.scheme() = SchemeStandard(this)
/**
 * 标准的SchemeUri
 */
class SchemeStandard {
    var uri: Uri? = null
    constructor(uri: Uri) { this.uri = uri }
    constructor(schemeUrl: String?) { uri = Uri.parse(schemeUrl) }
    constructor(scheme: String?, host: String?, path: String?, query: String?=null) {
        uri = Uri.parse("$scheme://$host/$path${if(query.isNullOrBlank()) "" else "?$query"}")
    }
    override fun toString(): String {
        return "$uri"
    }
    fun equals(other: SchemeStandard): Boolean {
        return uri?.scheme == other.uri?.scheme && uri?.host == other.uri?.host && uri?.path == other.uri?.path
    }
    val params: HashMap<String, String?>?
        get() {
            uri ?: return null
            uri?.query ?: return null
            return hashMapOf<String, String?>().also {
                uri?.queryParameterNames?.forEach { k -> it[k] = uri?.getQueryParameter(k) }
            }
        }
    val extra: Bundle?
        get() {
            val params = params ?: return null
            return Bundle().also { params.forEach { p -> it.putString(p.key, p.value) } }
        }
}

/**
 * scheme list 配置接口
 */
interface SchemeListInterface{
    val data: HashMap<SchemeStandard, Class<out Activity>>
    val dataFunc: HashMap<SchemeStandard, (ctx: Context, extra: Bundle?)->Unit>
    val webBrowser: Class<out Activity>? // 如果未设置内部webViewActivity, 默认跳转系统浏览器
}

interface SchemeIntentOptionInterface {
    // extra
    var extra: Bundle?
    fun extra(putBlock: Bundle.()->Unit)
    // launcherMode
    var launcherMode: LauncherMode?
    // 转场/过渡动画 overridePendingTransition(enter, exit)
    var transitionAnim: OPTAnim?
    var transitionAnimEnterResId: Int?
    var transitionAnimExitResId: Int?
    // 转场动画MD
    /**
     * 设置共享元素转场动画
     *  - 对应view预先赋值transitionName
     *  - 目标activity主动退出使用 [Activity.finishAfterTransition]
     *  - 目标ActivityView为异步图片时, 可使用[Activity.postponeEnterTransition]暂停动画,
     *   等待图片准备完成后[Activity.startPostponedEnterTransition]恢复动画
     */
    fun setSharedTransitionAnim(vararg views: View?)
    var options: Bundle?
}

/**
 * scheme跳转意图配置项
 */
class SchemeIntentOption(private val ctx: Context?): SchemeIntentOptionInterface {

    override var launcherMode: LauncherMode? = null

    override var transitionAnim: OPTAnim? = null
    override var transitionAnimEnterResId: Int? = null
    override var transitionAnimExitResId: Int? = null
    override fun setSharedTransitionAnim(vararg views: View?) {
        if (ctx !is Activity) return
        val sharedElements = ArrayList<Pair<View, String>>()
        views.filter { it!=null && !it.transitionName.isNullOrBlank() }.forEach { sharedElements.add(Pair(it, it!!.transitionName)) }
        if (sharedElements.isEmpty()) return
        options = ActivityOptions.makeSceneTransitionAnimation(ctx, *sharedElements.toTypedArray()).toBundle()
    }

    override var extra: Bundle? = null
    override fun extra(putBlock: Bundle.() -> Unit) {
        extra?.also { it.putBlock(); return }
        Bundle().also { extra = it }.putBlock()
    }

    override var options: Bundle? = null

    override fun toString(): String {
        return "SchemeIntentOption(ctx=$ctx, launcherMode=$launcherMode, transitionAnim=$transitionAnim, transitionAnimEnterResId=$transitionAnimEnterResId, transitionAnimExitResId=$transitionAnimExitResId, extra=$extra, options=$options)"
    }
}
enum class LauncherMode {
    // Standard,
    SingleTop,      // 栈顶复用模式
    SingleTask,     // 栈内复用模式, 其上activity移除
    // SingleInstance, // 单实例模式, 任意进程调用均复用其单例
}
enum class OPTAnim {
    // Default,
    None,
    FadeInOut,              // 淡入淡出
    FadeIn,                 // 淡入
    FadeOut,                // 淡出
    SlideLeftRight,         // 滑动 左推向右
    SlideRightLeft,         // 滑动 右推向左
    SlideToTop,             // 滑动 底部向上滑动弹出
    SlideToBottom,          // 滑动 弹出后向下滑动关闭
}
/* ----------KTX----------- */
fun Activity?.overridePendingTransition(enum: OPTAnim) {
    this ?: return
    when(enum) {
        None -> overridePendingTransition(0, 0)
        FadeInOut -> overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        FadeIn -> overridePendingTransition(android.R.anim.fade_in, com.zwping.alibx.R.anim.opt_hold)
        FadeOut -> overridePendingTransition(com.zwping.alibx.R.anim.opt_hold, android.R.anim.fade_out)
        SlideLeftRight -> overridePendingTransition(com.zwping.alibx.R.anim.slide_left_enter, com.zwping.alibx.R.anim.slide_right_exit)
        SlideRightLeft -> overridePendingTransition(com.zwping.alibx.R.anim.slide_right_enter, com.zwping.alibx.R.anim.slide_left_exit)
        SlideToTop -> overridePendingTransition(com.zwping.alibx.R.anim.slide_bottom_enter, com.zwping.alibx.R.anim.opt_hold)
        SlideToBottom -> overridePendingTransition(com.zwping.alibx.R.anim.opt_hold, com.zwping.alibx.R.anim.slide_bottom_exit)
    }
}
fun Context?.open(schemeURL: String?, option: SchemeIntentOption.() -> Unit = {}) { Scheme.open(this, schemeURL, option) }
fun Context?.open(clazz: Class<out Activity>, option: SchemeIntentOption.() -> Unit = {}) { Scheme.open(this, clazz, option) }
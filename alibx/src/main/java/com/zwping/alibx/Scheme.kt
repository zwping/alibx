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
import com.zwping.alibx.LauncherMode.*
import com.zwping.alibx.OPTAnim.*
import java.util.ArrayList

/**
 * schemeUri路由
 * zwping @ 2021/11/18
 */
interface SchemeInterface {

    fun init(app: Application,
             schemeConfigList: SchemeConfigListInterface,
             warningToast: ((ctx: Context, msg: String)->Unit)? = null)

    var schemeConfigList: SchemeConfigListInterface?
    var warningToast: ((ctx: Context, msg: String)->Unit)?

    @Deprecated("例子 scheme://host/path?query or http://xxx.com/path?query")
    fun Context?.open(scheme: String, host: String, path: String, query: String?)
    fun Context?.open(schemeURL: String?, option: SchemeOption.() -> Unit = {})
    fun Context?.open(clazz: Class<out Activity>, option: SchemeOption.() -> Unit = {})
}
interface SchemeOptionInterface {
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
interface SchemeConfigListInterface{
    val data: HashMap<String, Class<out Activity>>
    val dataFunc: HashMap<String, (ctx: Context, extra: Bundle?)->Unit>
    val webBrowser: Class<out Activity>? // 如果未设置内部webViewActivity, 默认跳转系统浏览器
}

object Scheme: SchemeInterface {
    override fun init(app: Application,
                      schemeConfigList: SchemeConfigListInterface,
                      warningToast: ((ctx: Context, msg: String) -> Unit)?) {
        this.schemeConfigList = schemeConfigList
        this.warningToast = warningToast
    }

    override var schemeConfigList: SchemeConfigListInterface? = null
    override var warningToast: ((ctx: Context, msg: String) -> Unit)? = null

    override fun Context?.open(scheme: String, host: String, path: String, query: String?) {}
    override fun Context?.open(clazz: Class<out Activity>, option: SchemeOption.() -> Unit) {
        this ?: return; open(null, clazz = clazz, SchemeOption(this).also(option))
    }
    override fun Context?.open(schemeURL: String?, option: SchemeOption.() -> Unit) {
        this ?: return
        if (schemeURL.isNullOrBlank()) { showToast(this, ErrMsg1); return }
        try { open(Uri.parse(schemeURL), null, SchemeOption(this).also(option)) }
        catch (e: Exception) { e.printStackTrace(); showToast(this, "$ErrMsg2 $schemeURL"); return }
    }

    private fun Context.open(uri: Uri?, clazz: Class<out Activity>?, opt: SchemeOption){
        if (uri == null && clazz == null) return
        var cls = clazz
        uri?.also uri@{
            if (it.scheme?.startsWith("http")==true || it.scheme?.startsWith("https")==true) {
                schemeConfigList?.webBrowser?.also { web ->
                    cls = web; return@uri
                }
                startActivity(Intent(Intent.ACTION_VIEW, it)); return // 没有则使用系统浏览器打开
            }
            schemeConfigList?.also { config ->

            }
        }
        startActivity(Intent(this, cls).also { i ->
            if (uri != null) i.data = uri // 携带过去
            opt.extra?.also { i.putExtras(it) }
            when(opt.launcherMode) {
                SingleTop -> i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                SingleTask -> i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }, opt.options)

        if (opt.transitionAnimEnterResId != null && opt.transitionAnimExitResId != null) {
            if (this is Activity) overridePendingTransition(opt.transitionAnimEnterResId!!, opt.transitionAnimExitResId!!)
        } else {
            opt.transitionAnim?.also { if (this is Activity) overridePendingTransition(it) }
        }
    }
    private fun showToast(ctx: Context, msg: String) {
        warningToast?.also { it(ctx, msg); return }
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }

    var ErrMsg1 = "scheme url is empty!!"
    var ErrMsg2 = "scheme url error!"
}


class SchemeOption(private val ctx: Context?): SchemeOptionInterface {

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
}
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
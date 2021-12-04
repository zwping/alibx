package com.zwping.alibx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.WindowManager.LayoutParams.*
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.*
import androidx.fragment.app.Fragment

/**
 * 状态栏、导航栏常用功能平铺
 * zwping @ 2021/10/18
 */
private interface IBar {

    /*** android 11 flags大量废弃 ***/
    fun isAndroidR() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R // android 11
    /*** insets compat简化了insets操作 ***/
    fun insetsController(ac: Activity): WindowInsetsControllerCompat? {
        ac.window?.decorView ?: return null
        return WindowInsetsControllerCompat(ac.window, ac.window.decorView)
    }

    /**
     * 沉浸式 - 让view显示状态栏[/导航栏]后面
     * @param color 状态栏颜色 默认透明&沉浸到状态栏后面
     * @param darkMode 状态栏文本颜色
     * @param navImmersive 是否沉浸到导航栏后面
     * @param navColor 导航栏颜色
     * @param navDarkMode 导航栏文本颜色, 部分手机会根据背景色自动显示反色
     */
    fun immersive(ac: Activity,
                  @ColorInt color: Int=Color.TRANSPARENT,
                  darkMode: Boolean?=null,
                  navImmersive: Boolean=false,
                  @ColorInt navColor: Int? = null,
                  navDarkMode: Boolean?=null)

    /**
     * 设置全屏
     * @param full true-> 全屏 false-> 退出全屏
     * @param behavior 全屏时bar的行为
     *  2 -> [BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE] 滑动显示后自动隐藏
     *  0 -> [BEHAVIOR_SHOW_BARS_BY_TOUCH] 触碰显示
     *  1 -> [BEHAVIOR_SHOW_BARS_BY_SWIPE] 滑动显示
     * @param cutoutMode 凹形屏适应模式
     *  1 -> [LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES] 允许延伸到留海
     *  0 -> [LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT] 全屏不允许延伸, 默认允许延伸到留海
     *  2 -> [LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER]  不允许延伸
     *
     */
    fun setFullScreen(ac: Activity,
                      full: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                      cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)

    /**
     * 设置状态栏深色模式
     * @param darkMode true -> 字体电池黑色 false -> 字体电池白色
     */
    fun setStatusBarDarkMode(ac: Activity, darkMode: Boolean=true)
    fun setStatusBarDarkMode(fm: Fragment, darkMode: Boolean=true)
    fun setNavBarDarkMode(ac: Activity, darkMode: Boolean=false)
    fun setNavBarDarkMode(fm: Fragment, darkMode: Boolean=false)

    /**
     * 设置状态栏隐藏/显示
     * @param behavior 隐藏后bar显示的行为
     * @param cutoutMode 异形屏兼容模式
     */
    fun setStatusBarHide(ac: Activity,
                                  hide: Boolean=true,
                                  behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                                  cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    fun setStatusBarHide(fm: Fragment,
                         hide: Boolean=true,
                         behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                         cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    fun setNavBarHide(ac: Activity,
                      hide: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
    fun setNavBarHide(fm: Fragment,
                      hide: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)

    /**
     * 设置状态栏颜色
     */
    fun setStatusBarColor(ac: Activity, @ColorInt color: Int)
    fun setStatusBarColor(fm: Fragment, @ColorInt color: Int)
    fun setNavBarColor(ac: Activity, @ColorInt color: Int)
    fun setNavBarColor(fm: Fragment, @ColorInt color: Int)
    fun setNavBarDividerColor(ac: Activity, @ColorInt color: Int)

    fun setActionBarColor(compatAc: AppCompatActivity, @ColorInt color: Int)

    /**
     * 获取状态栏高度/px
     */
    fun getStatusBarHeight(ctx: Context?): Int
    fun getNavBarHeight(ctx: Context?): Int
    fun getActionBarHeight(ctx: Context?): Int
    fun getScreenWidth(ctx: Context?): Int
    fun getScreenHeight(ctx: Context?): Int

    fun isSupperNavBar(ctx: Context?): Boolean

    @RequiresApi(Build.VERSION_CODES.P)
    fun setCutoutMode(window: Window, cutoutMode: Int)

    /**
     * 手动补偿沉浸式后bar的空缺高度
     */
    fun getContentView(ac: Activity): View?
    fun addMarginBottomNavBarHeight(view: View?)
    fun subtractMarginBottomNavBarHeight(view: View?)
    fun addMarginTopStatusBarHeight(view: View?)
    fun subtractMarginTopStatusBarHeight(view: View?)
}


/**
 * 基于Android 5.0 状态栏/导航栏 散装方法
 * zwping @ 2021/10/22
 */
object Bar : IBar {

    override fun immersive(ac: Activity,
                           color: Int,
                           darkMode: Boolean?,
                           navImmersive: Boolean,
                           navColor: Int?,
                           navDarkMode: Boolean?) {
        val window = ac.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!navImmersive) { // 不考虑二次逆向调用
            addMarginBottomNavBarHeight(getContentView(ac))
        }
        setStatusBarColor(ac, color)
        darkMode?.also { setStatusBarDarkMode(ac, it) }
        navColor?.also { setNavBarColor(ac, it) }
        if (navImmersive && navColor == null) setNavBarColor(ac, Color.TRANSPARENT)
        navDarkMode?.also { setNavBarDarkMode(ac, it) }
    }


    @SuppressLint("NewApi")
    override fun setFullScreen(ac: Activity, full: Boolean, behavior: Int, cutoutMode: Int) {
        val window = ac.window ?: return
        setCutoutMode(window, cutoutMode)
        WindowCompat.setDecorFitsSystemWindows(window, !full)
        insetsController(ac)?.also {
            systemBars().also { t -> if (full) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
    }


    override fun setStatusBarDarkMode(ac: Activity, darkMode: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.R) {  // android 11不生效
            insetsController(ac)?.isAppearanceLightStatusBars = darkMode
            return
        }
        var uiflag = ac.window?.decorView?.systemUiVisibility ?: return
        uiflag = if (darkMode) { uiflag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
        else { uiflag and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() }
        ac.window?.decorView?.systemUiVisibility = uiflag
    }

    override fun setNavBarDarkMode(ac: Activity, darkMode: Boolean) {
        insetsController(ac)?.isAppearanceLightNavigationBars = darkMode // 部分手机会自动取navBar颜色的反色
    }

    override fun setStatusBarDarkMode(fm: Fragment, darkMode: Boolean) {
        fm.activity?.also { setStatusBarDarkMode(it, darkMode) }
    }

    override fun setNavBarDarkMode(fm: Fragment, darkMode: Boolean) {
        fm.activity?.also { setNavBarDarkMode(it, darkMode) }
    }


    @SuppressLint("NewApi")
    override fun setStatusBarHide(ac: Activity, hide: Boolean, behavior: Int, cutoutMode: Int) {
        val window = ac.window ?: return
        insetsController(ac)?.also {
            statusBars().also { t -> if (hide) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
        setCutoutMode(window, cutoutMode)
        WindowCompat.setDecorFitsSystemWindows(window, !hide)
        if (hide) addMarginBottomNavBarHeight(getContentView(ac))
        else subtractMarginBottomNavBarHeight(getContentView(ac))
    }

    override fun setNavBarHide(ac: Activity, hide: Boolean, behavior: Int) {
        insetsController(ac)?.also {
            navigationBars().also { t -> if (hide) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
    }

    override fun setStatusBarHide(fm: Fragment, hide: Boolean, behavior: Int, cutoutMode: Int) {
        fm.activity?.also { setStatusBarHide(it, hide, behavior, cutoutMode) }
    }

    override fun setNavBarHide(fm: Fragment, hide: Boolean, behavior: Int) {
        fm.activity?.also { setNavBarHide(it, hide, behavior) }
    }


    override fun setStatusBarColor(ac: Activity, color: Int) {
        ac.window?.statusBarColor = color
    }
    override fun setStatusBarColor(fm: Fragment, color: Int) {
        fm.activity?.also { setStatusBarColor(it, color) }
    }

    override fun setNavBarColor(ac: Activity, color: Int) {
        ac.window?.navigationBarColor = color
    }

    override fun setNavBarColor(fm: Fragment, color: Int) {
        fm.activity?.also { setNavBarColor(it, color) }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun setNavBarDividerColor(ac: Activity, color: Int) {
        ac.window?.navigationBarDividerColor = color
    }

    override fun setActionBarColor(compatAc: AppCompatActivity, color: Int) {
        compatAc.supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
    }


    override fun getStatusBarHeight(ctx: Context?): Int {
        ctx ?: return 0
        val resId = ctx.resources.getIdentifier("status_bar_height", "dimen", "android")
        return ctx.resources.getDimensionPixelSize(resId)
    }

    override fun getNavBarHeight(ctx: Context?): Int {
        ctx ?: return 0
        if (!isSupperNavBar(ctx)) return 0
        val resId = ctx.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resId == 0) return 0
        return ctx.resources.getDimensionPixelSize(resId)
    }

    override fun getActionBarHeight(ctx: Context?): Int {
        ctx ?: return 0
        val tv = TypedValue()
        if (ctx.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, ctx.resources.displayMetrics)
        }
        return 0
    }

    override fun getScreenHeight(ctx: Context?): Int {
        ctx ?: return 0
        return getDisplayOutMetrics(ctx).heightPixels
    }

    override fun getScreenWidth(ctx: Context?): Int {
        ctx ?: return 0
        return getDisplayOutMetrics(ctx).widthPixels
    }

    private fun getDisplayOutMetrics(ctx: Context): DisplayMetrics {
        val outMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ctx.display?.getRealMetrics(outMetrics)
        } else {
            val vm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            @Suppress("DEPRECATION")
            vm?.defaultDisplay?.getRealMetrics(outMetrics)
        }
        return outMetrics
    }

    override fun isSupperNavBar(ctx: Context?): Boolean {
        ctx ?: return false
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return false
        val display: Display = wm.defaultDisplay
        val size = Point()
        val realSize = Point()
        display.getSize(size)
        display.getRealSize(realSize)
        return realSize.y != size.y || realSize.x != size.x
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun setCutoutMode(window: Window, cutoutMode: Int) { // android9兼容刘海屏方案, 可在AppTheme中全局配置
        window.attributes = window.attributes.also { it.layoutInDisplayCutoutMode = cutoutMode }
    }


    override fun getContentView(ac: Activity): View? {
        return ac.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
    }

    override fun addMarginBottomNavBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val navh = getNavBarHeight(view.context)
        lp.bottomMargin = if (lp.bottomMargin >= navh) lp.bottomMargin else lp.bottomMargin+navh
        view.layoutParams = lp
    }

    override fun subtractMarginBottomNavBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val navh = getNavBarHeight(view.context)
        lp.bottomMargin = if (lp.bottomMargin < navh) navh else lp.bottomMargin-navh
        view.layoutParams = lp
    }

    override fun addMarginTopStatusBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val statusH = getStatusBarHeight(view.context)
        lp.topMargin = if (lp.topMargin >= statusH) lp.topMargin else lp.topMargin+statusH
        view.layoutParams = lp
    }

    override fun subtractMarginTopStatusBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val statusH = getStatusBarHeight(view.context)
        lp.topMargin = if (lp.topMargin < statusH) statusH else lp.topMargin-statusH
        view.layoutParams = lp
    }
}

/* ----------KTX----------- */

/**
 * 沉浸式 - 让view显示状态栏[/导航栏]后面
 * @param color 状态栏颜色 默认透明&沉浸到状态栏后面
 * @param darkMode 状态栏文本颜色
 * @param navImmersive 是否沉浸到导航栏后面
 * @param navColor 导航栏颜色
 * @param navDarkMode 导航栏文本颜色, 部分手机会根据背景色自动显示反色
 */
fun Activity.immersive(@ColorInt color: Int=Color.TRANSPARENT,
                       darkMode: Boolean?=null,
                       navImmersive: Boolean=false,
                       @ColorInt navColor: Int? = null,
                       navDarkMode: Boolean?=null) {
    Bar.immersive(this, color, darkMode, navImmersive, navColor, navDarkMode)
}

/**
 * 设置全屏
 * @param full true-> 全屏 false-> 退出全屏
 * @param behavior 全屏时bar的行为
 *  2 -> [BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE] 滑动显示后自动隐藏
 *  0 -> [BEHAVIOR_SHOW_BARS_BY_TOUCH] 触碰显示
 *  1 -> [BEHAVIOR_SHOW_BARS_BY_SWIPE] 滑动显示
 * @param cutoutMode 凹形屏适应模式
 *  1 -> [LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES] 允许延伸到留海
 *  0 -> [LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT] 全屏不允许延伸, 默认允许延伸到留海
 *  2 -> [LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER]  不允许延伸
 *
 */
fun Activity.setFullScreen(full: Boolean=true,
                           behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                           cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
    Bar.setFullScreen(this, full, behavior, cutoutMode)
}

/**
 * 设置状态栏深色模式
 * @param darkMode true -> 字体电池黑色 false -> 字体电池白色
 */
fun Activity.setStatusBarDarkMode(darkMode: Boolean=true) {
    Bar.setStatusBarDarkMode(this, darkMode)
}
fun Fragment.setStatusBarDarkMode(darkMode:Boolean=true) {
    Bar.setStatusBarDarkMode(this, darkMode)
}
fun Activity.setNavBarDarkMode(darkMode: Boolean=false) {
    Bar.setNavBarDarkMode(this, darkMode)
}
fun Fragment.setNavBarDarkMode(darkMode: Boolean=false) {
    Bar.setNavBarDarkMode(this, darkMode)
}

/**
 * 设置状态栏隐藏/显示
 * @param behavior 隐藏后bar显示的行为
 * @param cutoutMode 异形屏兼容模式
 */
fun Activity.setStatusBarHide(hide: Boolean=true,
                              behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                              cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
    Bar.setStatusBarHide(this, hide, behavior, cutoutMode)
}
fun Fragment.setStatusBarHide(hide: Boolean=true,
                              behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                              cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
    Bar.setStatusBarHide(this, hide, behavior, cutoutMode)
}
fun Activity.setNavBarHide(hide: Boolean=true,
                           behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
    Bar.setNavBarHide(this, hide, behavior)
}
fun Fragment.setNavBarHide(hide: Boolean=true,
                           behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
    Bar.setNavBarHide(this, hide, behavior)
}

/**
 * 设置状态栏颜色
 */
fun Activity.setStatusBarColor(@ColorInt color: Int) { Bar.setStatusBarColor(this, color) }
fun Fragment.setStatusBarColor( @ColorInt color: Int) { Bar.setStatusBarColor(this, color) }
fun Activity.setNavBarColor(@ColorInt color: Int) { Bar.setNavBarColor(this, color) }
fun Fragment.setNavBarColor( @ColorInt color: Int) { Bar.setNavBarColor(this, color) }
@RequiresApi(Build.VERSION_CODES.P)
fun Activity.setNavBarDividerColor(@ColorInt color: Int) { Bar.setNavBarDividerColor(this, color) }

fun AppCompatActivity.setActionBarColor(@ColorInt color: Int) { Bar.setActionBarColor(this, color) }

/**
 * 获取状态栏高度/px
 */
fun Context?.getStatusBarHeight(): Int = Bar.getStatusBarHeight(this)
fun Context?.getNavBarHeight(): Int = Bar.getNavBarHeight(this)
fun Context?.getActionBarHeight(): Int = Bar.getActionBarHeight(this)
fun Context?.getScreenWidth(): Int = Bar.getScreenWidth(this)
fun Context?.getScreenHeight(): Int = Bar.getScreenHeight(this)

fun Context?.isSupperNavBar(): Boolean = Bar.isSupperNavBar(this)

@RequiresApi(Build.VERSION_CODES.P)
fun Window.setCutoutMode(cutoutMode: Int) { Bar.setCutoutMode(this, cutoutMode) }

/**
 * 手动补偿沉浸式后bar的空缺高度
 */
fun Activity.getContentView(): View? = Bar.getContentView(this)
fun View?.addMarginBottomNavBarHeight() { Bar.addMarginBottomNavBarHeight(this) }
fun View?.subtractMarginBottomNavBarHeight() { Bar.subtractMarginBottomNavBarHeight(this) }
fun View?.addMarginTopStatusBarHeight() { Bar.addMarginTopStatusBarHeight(this) }
fun View?.subtractMarginTopStatusBarHeight() { Bar.subtractMarginTopStatusBarHeight(this) }
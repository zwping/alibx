package com.zwping.alibx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
 * @lastTime 2021年11月01日17:18:22
 */
interface BarInterface {

    /*** android 11 flags大量废弃 ***/
    fun isAndroidR() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R // android 11
    /*** insets compat简化了insets操作 ***/
    fun Activity.insetsController(): WindowInsetsControllerCompat? { window?.decorView ?: return null; return WindowInsetsControllerCompat(window, window.decorView) }

    /**
     * 沉浸式 - 让view显示状态栏[/导航栏]后面
     * @param color 状态栏颜色 默认透明&沉浸到状态栏后面
     * @param darkMode 状态栏文本颜色
     * @param navImmersive 是否沉浸到导航栏后面
     * @param navColor 导航栏颜色
     * @param navDarkMode 导航栏文本颜色, 部分手机会根据背景色自动显示反色
     */
    fun Activity.immersive(@ColorInt color: Int=Color.TRANSPARENT, darkMode: Boolean?=null,
                           navImmersive: Boolean=false, @ColorInt navColor: Int? = null, navDarkMode: Boolean?=null)

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
                               cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)

    /**
     * 设置状态栏深色模式
     * @param darkMode true -> 字体电池黑色 false -> 字体电池白色
     */
    fun Activity.setStatusBarDarkMode(darkMode: Boolean=true)
    fun Fragment.setStatusBarDarkMode(darkMode: Boolean=true)
    fun Activity.setNavBarDarkMode(darkMode: Boolean=false)
    fun Fragment.setNavBarDarkMode(darkMode: Boolean=false)

    /**
     * 设置状态栏隐藏/显示
     * @param behavior 隐藏后bar显示的行为
     * @param cutoutMode 异形屏兼容模式
     */
    fun Activity.setStatusBarHide(hide: Boolean=true,
                                  behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                                  cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    fun Fragment.setStatusBarHide(hide: Boolean=true,
                                  behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                                  cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    fun Activity.setNavBarHide(hide: Boolean=true, behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
    fun Fragment.setNavBarHide(hide: Boolean=true, behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)

    /**
     * 设置状态栏颜色
     */
    fun Activity.setStatusBarColor(@ColorInt color: Int)
    fun Fragment.setStatusBarColor(@ColorInt color: Int)
    fun Activity.setNavBarColor(@ColorInt color: Int)
    fun Fragment.setNavBarColor(@ColorInt color: Int)
    fun Activity.setNavBarDividerColor(@ColorInt color: Int)

    fun AppCompatActivity.setActionBarColor(@ColorInt color: Int)

    /**
     * 获取状态栏高度/px
     */
    fun Context?.getStatusBarHeight(): Int
    fun Context?.getNavBarHeight(): Int
    fun Context?.getActionBarHeight(): Int

    fun Context?.isSupperNavBar(): Boolean

    @RequiresApi(Build.VERSION_CODES.P)
    fun Window.setCutoutMode(cutoutMode: Int)

    /**
     * 手动补偿沉浸式后bar的空缺高度
     */
    fun Activity.getContentView(): View?
    fun View?.addMarginBottomNavBarHeight()
    fun View?.subtractMarginBottomNavBarHeight()
    fun View?.addMarginTopStatusBarHeight()
    fun View?.subtractMarginTopStatusBarHeight()
}


/**
 * 基于Android 5.0 状态栏/导航栏 散装方法
 * zwping @ 2021/10/22
 */
object Bar : BarInterface {

    fun Activity.immersive() { immersive(color = Color.TRANSPARENT) }
    override fun Activity.immersive(color: Int, darkMode: Boolean?,
                                    navImmersive: Boolean, navColor: Int?, navDarkMode: Boolean?) {
        window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!navImmersive) { // 不考虑二次逆向调用
            getContentView()?.addMarginBottomNavBarHeight()
        }
        setStatusBarColor(color)
        darkMode?.also { setStatusBarDarkMode(it) }
        navColor?.also { setNavBarColor(it) }
        if (navImmersive && navColor == null) setNavBarColor(Color.TRANSPARENT)
        navDarkMode?.also { setNavBarDarkMode(it) }
    }


    fun Activity.setFullScreen(full: Boolean=true) { setFullScreen(full, 2, 1) }
    @SuppressLint("NewApi")
    override fun Activity.setFullScreen(full: Boolean, behavior: Int, cutoutMode: Int) {
        window ?: return
        window.setCutoutMode(cutoutMode)
        WindowCompat.setDecorFitsSystemWindows(window, !full)
        insetsController()?.also {
            systemBars().also { t -> if (full) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
    }


    override fun Activity.setStatusBarDarkMode(darkMode: Boolean) {
         // insetsController()?.isAppearanceLightStatusBars = darkMode // android 11不生效
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        var uiflag = window?.decorView?.systemUiVisibility ?: return
        uiflag = if (darkMode) { uiflag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
        else { uiflag and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() }
        window?.decorView?.systemUiVisibility = uiflag
    }
    override fun Activity.setNavBarDarkMode(darkMode: Boolean) {
        insetsController()?.isAppearanceLightNavigationBars = darkMode // 部分手机会自动取navBar颜色的反色
    }
    override fun Fragment.setStatusBarDarkMode(darkMode: Boolean) { activity?.setStatusBarDarkMode(darkMode) }
    override fun Fragment.setNavBarDarkMode(darkMode: Boolean) { activity?.setNavBarDarkMode(darkMode) }



    fun Activity.setStatusBarHide(hide: Boolean=true) { setStatusBarHide(hide, 2, 1) }
    @SuppressLint("NewApi")
    override fun Activity.setStatusBarHide(hide: Boolean, behavior: Int, cutoutMode: Int) {
        window ?: return
        insetsController()?.also {
            statusBars().also { t -> if (hide) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
        window?.setCutoutMode(cutoutMode)
        WindowCompat.setDecorFitsSystemWindows(window, !hide)
        if (hide) getContentView().addMarginBottomNavBarHeight()
        else getContentView().subtractMarginBottomNavBarHeight()
    }
    fun Activity.setNavBarHide(hide: Boolean=true) { setNavBarHide(hide, 2) }
    override fun Activity.setNavBarHide(hide: Boolean, behavior: Int) {
        insetsController()?.also {
            navigationBars().also { t -> if (hide) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
    }
    fun Fragment.setStatusBarHide(hide: Boolean=true) { setStatusBarHide(hide, 2, 1) }
    override fun Fragment.setStatusBarHide(hide: Boolean, behavior: Int, cutoutMode: Int) { activity?.setStatusBarHide(hide, behavior, cutoutMode) }
    fun Fragment.setNavBarHide(hide: Boolean=true) { setNavBarHide(hide, 2) }
    override fun Fragment.setNavBarHide(hide: Boolean, behavior: Int) { activity?.setNavBarHide(hide, behavior) }


    override fun Activity.setStatusBarColor(color: Int) { window?.statusBarColor = color }
    override fun Fragment.setStatusBarColor(color: Int) { activity?.setStatusBarColor(color) }
    override fun Activity.setNavBarColor(color: Int) { window?.navigationBarColor = color }
    override fun Fragment.setNavBarColor(color: Int) { activity?.setNavBarColor(color) }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun Activity.setNavBarDividerColor(color: Int) { window?.navigationBarDividerColor = color }
    override fun AppCompatActivity.setActionBarColor(color: Int) { supportActionBar?.setBackgroundDrawable(ColorDrawable(color)) }


    override fun Context?.getStatusBarHeight(): Int {
        this ?: return 0
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resId)
    }
    override fun Context?.getNavBarHeight(): Int {
        this ?: return 0
        if (!isSupperNavBar()) return 0
        val resId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resId == 0) return 0
        return resources.getDimensionPixelSize(resId)
    }
    override fun Context?.getActionBarHeight(): Int {
        this ?: return 0
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        return 0
    }
    override fun Context?.isSupperNavBar(): Boolean {
        this ?: return false
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return false
        val display: Display = wm.defaultDisplay
        val size = Point()
        val realSize = Point()
        display.getSize(size)
        display.getRealSize(realSize)
        return realSize.y != size.y || realSize.x != size.x
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun Window.setCutoutMode(cutoutMode: Int) { // android9兼容刘海屏方案, 可在AppTheme中全局配置
        attributes = attributes.also { it.layoutInDisplayCutoutMode = cutoutMode }
    }


    override fun Activity.getContentView(): View? { return findViewById<ViewGroup>(android.R.id.content).getChildAt(0) }
    override fun View?.addMarginBottomNavBarHeight() {
        this ?: return
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        val navh = context.getNavBarHeight()
        lp.bottomMargin = if (lp.bottomMargin >= navh) lp.bottomMargin else lp.bottomMargin+navh
        layoutParams = lp
    }
    override fun View?.subtractMarginBottomNavBarHeight() {
        this ?: return
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        val navh = context.getNavBarHeight()
        lp.bottomMargin = if (lp.bottomMargin < navh) navh else lp.bottomMargin-navh
        layoutParams = lp
    }
    override fun View?.addMarginTopStatusBarHeight() {
        this ?: return
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        val statusH = context.getStatusBarHeight()
        lp.topMargin = if (lp.topMargin >= statusH) lp.topMargin else lp.topMargin+statusH
        layoutParams = lp
    }
    override fun View?.subtractMarginTopStatusBarHeight() {
        this ?: return
        val lp = layoutParams as ViewGroup.MarginLayoutParams
        val statusH = context.getStatusBarHeight()
        lp.topMargin = if (lp.topMargin < statusH) statusH else lp.topMargin-statusH
        layoutParams = lp
    }

}
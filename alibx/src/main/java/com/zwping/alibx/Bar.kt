package com.zwping.alibx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


/**
 * 基于Android 5.0 状态栏/导航栏 散装方法
 * @to-do 小屏模式未兼容
 * zwping @ 2021/10/22
 */
object Bar {

    /*** android 11 window.flags大量废弃 ***/
    val isR by lazy { android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R }

    /*** insets compat简化了insets操作 ***/
    fun insetsController(window: Window?): WindowInsetsControllerCompat? {
        window?.decorView ?: return null
        return WindowInsetsControllerCompat(window, window.decorView)
    }
    fun insets(view: View?): WindowInsetsCompat? {
        view ?: return null
        return ViewCompat.getRootWindowInsets(view)
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
    fun setFullScreen(window: Window?,
                      full: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                      cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
        window ?: return
        setCutoutMode(window, cutoutMode)
        WindowCompat.setDecorFitsSystemWindows(window, !full)
        insetsController(window)?.also {
            statusBars().also { t -> if (full) it.hide(t) else it.show(t) }
            navigationBars().also { t -> if (full) it.hide(t) else it.show(t) }
            it.systemBarsBehavior = behavior
        }
    }

    fun setFullScreen(ac: Activity?,
                      full: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                      cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
        setFullScreen(ac?.window, full, behavior, cutoutMode)
    }

    /**
     * 沉浸式 - 让view显示状态栏[/导航栏]后面
     * @param color 状态栏颜色 默认透明&沉浸到状态栏后面
     * @param darkMode 状态栏文本颜色
     * @param navImmersive 是否沉浸到导航栏后面
     * @param navColor 导航栏颜色
     * @param navDarkMode 导航栏文本颜色, 部分手机会根据背景色自动显示反色
     */
    fun immersive(window: Window?,
                  @ColorInt color: Int=Color.TRANSPARENT,
                  darkMode: Boolean?=null,
                  navImmersive: Boolean=false,
                  @ColorInt navColor: Int? = null,
                  navDarkMode: Boolean?=null) {
        window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setStatusBarColor(window, color)
        if (navImmersive && navColor == null) { // nav bar 默认透明
            setNavBarColor(window, Color.TRANSPARENT)
        }
        if (!navImmersive) { // 不考虑二次逆向调用
            addMarginBottomNavBarHeight(getContentView(window))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 关闭导航栏默认阴影
            window.isNavigationBarContrastEnforced = false
        }
        navColor?.also { setNavBarColor(window, it) }
        darkMode?.also { setStatusBarDarkMode(window, it) }
        navDarkMode?.also { setNavBarDarkMode(window, it) }
    }
    fun immersive(ac: Activity?,
                  @ColorInt color: Int=Color.TRANSPARENT,
                  darkMode: Boolean?=null,
                  navImmersive: Boolean=false,
                  @ColorInt navColor: Int? = null,
                  navDarkMode: Boolean?=null) {
        immersive(ac?.window, color, darkMode, navImmersive, navColor, navDarkMode)
    }

    /**
     * 设置状态栏深色模式
     * @param darkMode true -> 字体电池黑色 false -> 字体电池白色
     */
    fun setStatusBarDarkMode(window: Window?, darkMode: Boolean=true) {
        window ?: return
        insetsController(window)?.isAppearanceLightStatusBars = darkMode
    }
    fun setStatusBarDarkMode(ac: Activity?, darkMode: Boolean=true) { setStatusBarDarkMode(ac?.window, darkMode) }
    fun setStatusBarDarkMode(fm: Fragment?, darkMode: Boolean=true) { setStatusBarDarkMode(fm?.activity, darkMode) }

    fun setNavBarDarkMode(window: Window?, darkMode: Boolean=false) {
        // 部分手机会自动取navBar颜色的反色
        // 提示线无法控制 pixel 4a android 12 2022-01-17
        insetsController(window)?.isAppearanceLightNavigationBars = darkMode
    }
    fun setNavBarDarkMode(ac: Activity?, darkMode: Boolean=false) { setNavBarDarkMode(ac?.window, darkMode) }
    fun setNavBarDarkMode(fm: Fragment?, darkMode: Boolean=false) { setNavBarDarkMode(fm?.activity, darkMode) }


    /**
     * 设置状态栏隐藏/显示
     * @param behavior 隐藏后bar显示的行为
     * @param cutoutMode 异形屏兼容模式
     */
    fun setStatusBarHide(window: Window?,
                         hide: Boolean=true,
                         behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                         @SuppressLint("InlinedApi") cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
        window ?: return
        insetsController(window)?.also {
            if (hide) it.hide(statusBars()) else it.show(statusBars())
            it.systemBarsBehavior = behavior
        }
        setCutoutMode(window, cutoutMode)
        WindowCompat.setDecorFitsSystemWindows(window, !hide)
        if (hide) addMarginBottomNavBarHeight(getContentView(window))
        else subtractMarginBottomNavBarHeight(getContentView(window))
    }
    fun setStatusBarHide(ac: Activity?,
                         hide: Boolean=true,
                         behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                         @SuppressLint("InlinedApi") cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
        setStatusBarHide(ac?.window, hide, behavior, cutoutMode)
    }
    fun setStatusBarHide(fm: Fragment?,
                         hide: Boolean=true,
                         behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
                         @SuppressLint("InlinedApi") cutoutMode: Int=LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
        setStatusBarHide(fm?.activity, hide, behavior, cutoutMode)
    }

    fun setNavBarHide(window: Window?,
                      hide: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
        insetsController(window)?.also {
            if (hide) it.hide(navigationBars()) else it.show(navigationBars())
            it.systemBarsBehavior = behavior
        }
    }
    fun setNavBarHide(ac: Activity?,
                      hide: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
        setNavBarHide(ac?.window, hide, behavior)
    }
    fun setNavBarHide(fm: Fragment?,
                      hide: Boolean=true,
                      behavior: Int=BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
        setNavBarHide(fm?.activity, hide, behavior)
    }

    /**
     * 设置状态栏颜色
     */
    fun setStatusBarColor(window: Window?, @ColorInt color: Int) {
        window?.statusBarColor = color
    }
    fun setStatusBarColor(ac: Activity?, @ColorInt color: Int) { setStatusBarColor(ac?.window, color) }
    fun setStatusBarColor(fm: Fragment?, @ColorInt color: Int) { setStatusBarColor(fm?.activity, color) }
    fun setNavBarColor(window: Window?, @ColorInt color: Int) {
        window?.navigationBarColor = color
    }
    fun setNavBarColor(ac: Activity?, @ColorInt color: Int) { setNavBarColor(ac?.window, color) }
    fun setNavBarColor(fm: Fragment?, @ColorInt color: Int) { setNavBarColor(fm?.activity, color) }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setNavBarDividerColor(ac: Activity, @ColorInt color: Int) {
        ac.window?.navigationBarDividerColor = color
    }

    fun setActionBarColor(compatAc: AppCompatActivity, @ColorInt color: Int) {
        compatAc.supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
    }


    /**
     * status_bar_height
     * navigation_bar_height
     */
    private fun Context.getBarHeight(name: String): Int
            = resources.getDimensionPixelSize(resources.getIdentifier(name, "dimen", "android"))
    /**
     * 获取状态栏高度/px
     */
    fun getStatusBarHeight(ctx: Context?): Int {
        return ctx?.getBarHeight("status_bar_height") ?: 0
    }
    fun getNavBarHeight(ctx: Context?): Int {
        ctx ?: return 0
        /* // 并不能兼容国产rom 2022-01-17
        if (ctx is Activity) {
            val view = ctx.findViewById<View>(android.R.id.content)
            val high = ViewCompat.getRootWindowInsets(view)?.getInsets(navigationBars())?.bottom
            if (high != null) {
                return high
            }
        }
        */
        // if (!isSupperNavBar(ctx)) return 0   // 不支持nav bar
        if (isFullScreenGesture(ctx)) return 0  // 开启了全面屏
        if (!isNavBarVisible(ctx)) return 0     // nav bar不可见
        return ctx.getBarHeight("navigation_bar_height")
    }
    fun getActionBarHeight(ctx: Context?): Int {
        ctx ?: return 0
        val tv = TypedValue()
        if (ctx.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, ctx.resources.displayMetrics)
        }
        return 0
    }
    fun getScreenHeight(ctx: Context?): Int = getDisplayOutMetrics(ctx).heightPixels
    fun getScreenWidth(ctx: Context?): Int = getDisplayOutMetrics(ctx).widthPixels
    private fun getDisplayOutMetrics(ctx: Context?): DisplayMetrics {
        val outMetrics = DisplayMetrics()
        ctx ?: return outMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ctx.display?.getRealMetrics(outMetrics)
        } else {
            val vm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            vm?.defaultDisplay?.getRealMetrics(outMetrics)
        }
        return outMetrics
    }

    @Deprecated("isNavBarVisible")
    fun isSupperNavBar(ctx: Context?): Boolean {
        ctx ?: return false
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return false
        val display: Display = wm.defaultDisplay
        val size = Point()
        val realSize = Point()
        display.getSize(size)
        display.getRealSize(realSize)
        return realSize.y != size.y || realSize.x != size.x
    }

    /*** 是否有导航栏, 导航栏是否可见 ***/
    fun isNavBarVisible(context: Context?): Boolean {
        context ?: return false
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val d = wm.defaultDisplay
        val realDisplayMetrics = DisplayMetrics()

        d.getRealMetrics(realDisplayMetrics)

        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels

        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)

        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels

        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }

    /*** 是否开启全面屏手势
     * from ultimatebarx
     * ***/
    fun isFullScreenGesture(context: Context?): Boolean {
        context ?: return false
        return try {
            // adb shell settings list global  // 查看数据库，寻找是否隐藏 navigation
            var miui = Settings.Global.getInt(context.contentResolver, "force_fsg_nav_bar", -1) > 0
            if (miui) { // miui指示线可以手动隐藏, 如隐藏则判断其为未开启全面屏手势
                // k40 pro miui 12.5.8 新增隐藏手势线标识
                val hideLine = Settings.Global.getInt(context.contentResolver, "hide_gesture_line", 0)
                miui = if (hideLine == 1) {
                    true
                } else {
                    val navh = context.getBarHeight("navigation_bar_height")
                    val screenh = context.getScreenHeight()
                    navh>0 && screenh/navh<=30
                }
            }
            val emui = Settings.Global.getInt(context.contentResolver, "navigationbar_is_min", -1) > 0
            val funtouch = Settings.Secure.getInt(context.contentResolver, "navigation_gesture_on", -1) > 0
            miui || emui || funtouch
        } catch (e: Exception) {
            false
        }
    }

    fun setCutoutMode(window: Window, cutoutMode: Int) { // android9兼容刘海屏方案, 可在AppTheme中全局配置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply { layoutInDisplayCutoutMode = cutoutMode }
        }
    }

    /**
     * 手动补偿沉浸式后bar的空缺高度
     */
    fun getContentView(window: Window?): View? {
        return window?.findViewById<ViewGroup?>(Window.ID_ANDROID_CONTENT)
    }
    fun getContentView(ac: Activity?): View? = getContentView(ac?.window)

    fun addMarginBottomNavBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val navh = getNavBarHeight(view.context)
        lp.bottomMargin = if (lp.bottomMargin >= navh) lp.bottomMargin else lp.bottomMargin+navh
        view.layoutParams = lp
    }
    fun subtractMarginBottomNavBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val navh = getNavBarHeight(view.context)
        lp.bottomMargin = if (lp.bottomMargin < navh) navh else lp.bottomMargin-navh
        view.layoutParams = lp
    }
    fun addMarginTopStatusBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val statusH = getStatusBarHeight(view.context)
        lp.topMargin = if (lp.topMargin >= statusH) lp.topMargin else lp.topMargin+statusH
        view.layoutParams = lp
    }
    fun subtractMarginTopStatusBarHeight(view: View?) {
        view ?: return
        val lp = view.layoutParams as ViewGroup.MarginLayoutParams
        val statusH = getStatusBarHeight(view.context)
        lp.topMargin = if (lp.topMargin < statusH) statusH else lp.topMargin-statusH
        view.layoutParams = lp
    }

    /* ---------- ime insets ------------ */

    fun isKeyboardVisible(view: View?): Boolean {
        return insets(view)?.isVisible(ime()) ?: false
    }

    /**
     * 显示键盘, onCreate中需要延时100ms调用
     */
    fun showKeyboard(view: View?) {
        view ?: return
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        if (!view.requestFocus()) return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(view: View?) {
        view ?: return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 监听键盘改变事件
     * @param listener 直接显示ime结果
     * @param progress ime过程, 条件苛刻, 体验更好。[其window需具备沉浸式 & ADJUST_NOTHING]
     *
     * @see Bar.registerKeyboardChangeListener
     * @see Bar.unregisterKeyboardChangeListener
     */
    fun registerKeyboardChangeListener(
        window: Window?,
        listener: (show: Boolean, high: Int)-> Unit = {_, _ -> },
        progress: ((show: Boolean, diff: Int)-> Unit)? = null
    ) {
        window ?: return
        val contentView = window.findViewById<FrameLayout?>(android.R.id.content) ?: return

        if (progress != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // https://github.com/android/user-interface-samples/tree/master/WindowInsetsAnimation
            contentView.setWindowInsetsAnimationCallback(object: WindowInsetsAnimation.Callback(0){
                var insets: WindowInsets? = null
                // #1: 首先 onPrepare 被调用，这允许应用记录下当前布局中的任何视图状态
                override fun onPrepare(animation: WindowInsetsAnimation) {
                    super.onPrepare(animation)
                }
                // #2: 接下来我们实现 onStart() 方法，这会让我们先记录下这个视图结束时候的位置
                override fun onStart(animation: WindowInsetsAnimation, bounds: WindowInsetsAnimation.Bounds): WindowInsetsAnimation.Bounds {
                    val show = if (insets==null) true else insets?.isVisible(ime()) == false
                    val high = if (show) bounds.upperBound.bottom - getNavBarHeight(window.decorView.context) else 0
                    listener(show, high)
                    getDecorViewInvisibleHigh(window)
                    // progress?.invoke(show, high)
                    return bounds
                }
                // #3: 它会在动画中每次视窗属性改变的时候被调用
                override fun onProgress(insets: WindowInsets, p1: MutableList<WindowInsetsAnimation>): WindowInsets {
                    this.insets = insets
                    val typesInset = insets.getInsets(ime())
                    val otherInset = insets.getInsets(systemBars())
                    val subtract = android.graphics.Insets.subtract(typesInset, otherInset)
                    val diff = android.graphics.Insets.max(subtract, android.graphics.Insets.NONE).let { it.top - it.bottom }
                    progress?.invoke(insets.isVisible(ime()), diff)
                    return insets
                }
                // #4: 最后 onEnd 在动画已经结束的时候被调用。使用这个来清理任何旧的状态
                override fun onEnd(animation: WindowInsetsAnimation) { }
            })
            // ViewCompat.setWindowInsetsAnimationCallback()并未兼容<R @2022-01-14
            // ViewCompat.setWindowInsetsAnimationCallback(contentView, object: WindowInsetsAnimationCompat.Callback(0){
            //     override fun onProgress(insets: WindowInsetsCompat, p1: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
            //         return insets
            //     }
            // })
            return
        }

        if (window.attributes.softInputMode == SOFT_INPUT_ADJUST_NOTHING) {
            window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
        }

        var preHigh = getDecorViewInvisibleHigh(window)
        val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val high = getDecorViewInvisibleHigh(window)
            if (preHigh != high) {
                listener(high!=0, high)
                progress?.invoke(high!=0, high*-1)
                preHigh = high
            }
        }
        contentView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        contentView.setTag(TAG_Keyboard, onGlobalLayoutListener)
    }
    private const val TAG_Keyboard = -8

    fun unregisterKeyboardChangeListener(window: Window?) {
        window ?: return
        val contentView = window.findViewById<FrameLayout?>(android.R.id.content) ?: return
        val tag = contentView.getTag(TAG_Keyboard)
        if (tag is ViewTreeObserver.OnGlobalLayoutListener) {
            contentView.viewTreeObserver.removeOnGlobalLayoutListener(tag)
            contentView.setTag(TAG_Keyboard, null)
        }
    }

    fun registerKeyboardChangeListener(
        activity: FragmentActivity?,
        listener: (show: Boolean, high: Int)-> Unit = {_, _ -> },
        progress: ((show: Boolean, diff: Int)-> Unit)? = null
    ) {
        activity ?: return
        registerKeyboardChangeListener(activity.window, listener, progress)
        activity.lifecycle.addObserver(object: LifecycleEventObserver{
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when(event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        unregisterKeyboardChangeListener(activity.window)
                        activity.lifecycle.removeObserver(this)
                    }
                }
            }
        })
    }

    /**
     * decorView可视高度
     */
    fun getDecorViewInvisibleHigh(window: Window?): Int {
        window ?: return 0
        val decorView = window.decorView
        val outRect = Rect()
        decorView.getWindowVisibleDisplayFrame(outRect)
        val high = Math.abs(decorView.bottom - outRect.bottom)
        if (high <= getStatusBarHeight(decorView.context) + getNavBarHeight(decorView.context)) {
            sDecorViewDiff = high; return 0
        }
        return high - sDecorViewDiff // 获取两次差值得出键盘高度, 键盘不包含导航栏高度
    }
    private var sDecorViewDiff = 0

}
/* ---------KTX----------- */

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

fun Window.setCutoutMode(cutoutMode: Int) { Bar.setCutoutMode(this, cutoutMode) }

/**
 * 手动补偿沉浸式后bar的空缺高度
 */
fun Activity.getContentView(): View? = Bar.getContentView(this)
fun View?.addMarginBottomNavBarHeight() { Bar.addMarginBottomNavBarHeight(this) }
fun View?.subtractMarginBottomNavBarHeight() { Bar.subtractMarginBottomNavBarHeight(this) }
fun View?.addMarginTopStatusBarHeight() { Bar.addMarginTopStatusBarHeight(this) }
fun View?.subtractMarginTopStatusBarHeight() { Bar.subtractMarginTopStatusBarHeight(this) }

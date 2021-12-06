package com.zwping.alibx

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ActionProvider
import androidx.core.view.MenuItemCompat
import androidx.appcompat.widget.Toolbar

/**
 * Toolbar扩展
 *
 * zwping @ 2020/10/26
 */
/*
Toolbar布局: [ navigationIcon - logoIcon - title/subTitle - (childView) - menuLayout ]
官方文档: https://developer.android.com/reference/kotlin/androidx/appcompat/widget/Toolbar
方法:
    setTitleOfCenter(...) 设置Toolbar居中的标题
    setTitleCenter(...) 设置Toolbar标题居中
    addMenu(...)    快捷增加menu
    addMenuBadge(...)   快捷增加带角标的menu
    getActionProvider2(...)     获取带角标的ActionProvider2
    setStatusBarImmersion()     Toolbar支持状态栏的沉浸

    actionBar.setHomeButtonEnabled() 小于4.0版本的默认值为true的。但是在4.0及其以上是false
                                    决定左上角的图标是否可以点击
    actionBar.setDisplayHomeAsUpEnabled 给左上角图标的左边加上一个返回的图标 ActionBar.DISPLAY_HOME_AS_UP
    actionBar.setDisplayShowHomeEnabled 左上角图标是否显示 对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
    actionBar.setDisplayShowCustomEnabled 自定义的普通View能在title栏显示 actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    actionBar.setDisplayShowTitleEnabled  ActionBar.DISPLAY_SHOW_TITLE
*/
interface IToolbar {
    /**
     * 设置Toolbar居中的标题
     * 新增一个TextView, 相对于Toolbar完全居中
     *
     * @param title 标题
     * @return AppCompatTextView
     */
    fun setTitleOfCenter(toolbar: Toolbar?, title: CharSequence?): AppCompatTextView?

    /**
     * 设置Toolbar标题居中
     * 不改变原有结构, 相对于本身TextView居中
     *
     * @param title 标题 (注意其执行顺序, Toolbar内部动态addView)
     */
    fun setTitleCenter(toolbar: Toolbar?, title: CharSequence?)

    /**
     * 快捷增加menu
     * @param itemId menuId 0x01
     * @param iconRes 图标
     * @param title 标题
     * @param actionEnum 展现方式
     *                  总是显示在界面上 MenuItem.SHOW_AS_ACTION_ALWAYS
     *                  不显示在界面上 MenuItem.SHOW_AS_ACTION_NEVER
     *                  如果有位置才显示, 不然就出现在右边的三个点中 MenuItem.SHOW_AS_ACTION_IF_ROOM
     */
    fun addMenu(toolbar: Toolbar?,
                itemId: Int,
                @DrawableRes iconRes: Int,
                title: CharSequence,
                actionEnum: Int = MenuItem.SHOW_AS_ACTION_IF_ROOM)
    fun addMenu(menu: Menu?,
                itemId: Int,
                @DrawableRes iconRes: Int,
                title: CharSequence,
                actionEnum: Int = MenuItem.SHOW_AS_ACTION_IF_ROOM)

    /**
     * 快捷增加带角标的menu
     * @return [ActionProvider2]
     */
    fun addMenuBadge(toolbar: Toolbar?,
                     itemId: Int,
                     @DrawableRes iconRes: Int,
                     title: CharSequence,
                     menuItemClickListener: ((ActionProvider2) -> Unit)?=null): ActionProvider2?
    fun addMenuBadge(menu: Menu?,
                     ctx: Context?,
                     itemId: Int,
                     @DrawableRes iconRes: Int,
                     title: CharSequence,
                     menuItemClickListener: ((ActionProvider2) -> Unit)? = null): ActionProvider2?

    /**
     * 获取带角标的ActionProvider2
     * @return [ActionProvider2]
     */
    fun getActionProvider2(toolbar: Toolbar?, itemId: Int): ActionProvider2?
}

object ToolbarUtil : IToolbar {
    override fun setTitleOfCenter(toolbar: Toolbar?, title: CharSequence?): AppCompatTextView? {
        // setSupportActionBar(toolbar) // 不建议设置为ActionBar
        // supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar ?: return null
        toolbar.title = "" // 置空默认标题
        toolbar.contentInsetStartWithNavigation = 0
        for (i in 0 until toolbar.childCount) {
            val v = toolbar.getChildAt(i)
            if (v.tag?.toString() == "OfCenter" && v.parent != null && v.parent is ViewGroup)
                (v.parent as ViewGroup).removeView(v)
        }
        return AppCompatTextView(toolbar.context).also {
            it.tag = "OfCenter"
            it.layoutParams = ActionBar.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            it.setSingleLine();it.ellipsize = TextUtils.TruncateAt.END
            it.setTextAppearance(toolbar.context, android.R.style.TextAppearance_Material_Widget_Toolbar_Title)
            it.text = title
            toolbar.addView(it)
        }
    }

    override fun setTitleCenter(toolbar: Toolbar?, title: CharSequence?) {
        toolbar ?: return
        toolbar.title = title
        title ?: return
        toolbar.contentInsetStartWithNavigation = 0
        // setTitleMargin(0, 0, 0, 0)
        for (i in 0 until toolbar.childCount) {
            when (val v = toolbar.getChildAt(i)) {
                is TextView -> {
                    v.gravity = Gravity.CENTER;v.getLayoutParams().width = MATCH_PARENT
                }
                else -> { }
            }
        }
    }

    override fun addMenu(toolbar: Toolbar?, itemId: Int, iconRes: Int, title: CharSequence, actionEnum: Int) {
        addMenu(toolbar?.menu, itemId, iconRes, title, actionEnum)
    }

    override fun addMenu(menu: Menu?, itemId: Int, iconRes: Int, title: CharSequence, actionEnum: Int) {
        menu?.add(Menu.NONE, itemId, menu.size(), title)?.setIcon(iconRes)?.setShowAsAction(actionEnum)
    }

    override fun addMenuBadge(
        toolbar: Toolbar?,
        itemId: Int,
        iconRes: Int,
        title: CharSequence,
        menuItemClickListener: ((ActionProvider2) -> Unit)?
    ): ActionProvider2? {
        return addMenuBadge(toolbar?.menu, toolbar?.context, itemId, iconRes, title, menuItemClickListener)
    }

    override fun addMenuBadge(
        menu: Menu?,
        ctx: Context?,
        itemId: Int,
        iconRes: Int,
        title: CharSequence,
        menuItemClickListener: ((ActionProvider2) -> Unit)?
    ): ActionProvider2? {
        menu ?: return null
        var provider: ActionProvider2?
        menu.add(Menu.NONE, itemId, menu.size(), title).setIcon(iconRes).also {
            provider = ActionProvider2(ctx, it).setOnMenuItemClickListener(menuItemClickListener)
            MenuItemCompat.setActionProvider(it, provider)
        }.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return provider
    }

    override fun getActionProvider2(toolbar: Toolbar?, itemId: Int): ActionProvider2? {
        return MenuItemCompat.getActionProvider(toolbar?.menu?.findItem(itemId)) as ActionProvider2?
    }

}
/*** 圆形背景TextView ***/
class BadgeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    var color: Int = Color.RED // 默认小红点

    private val paint by lazy { Paint() }
    override fun onDraw(canvas: Canvas?) {
        paint.color = color;paint.isAntiAlias = true
        val w2 = width / 2F;canvas?.drawCircle(w2, w2, w2, paint)
        super.onDraw(canvas)
    }
}

/*** 自定义携带角标的ActionProvider ***/
class ActionProvider2(context: Context?, private val item: MenuItem) : ActionProvider(context) {

    var iconView: AppCompatImageView? = null
    var badgeView: BadgeTextView? = null
    var num: Int? = null

    /**
     * 设置角标数值
     * @param num
     */
    fun setBadgeNum(num: Int? = 0) {
        this.num = num
        badgeView?.visibility = if (null == num || 0 == num) View.INVISIBLE else View.VISIBLE
        badgeView?.text = "${if (null == num) 0 else if (num > 99) 99 else num.coerceAtLeast(1)}"
    }

    /**
     * 设置角标是否显示
     * @param visible
     */
    fun setBadgeVisible(visible: Boolean) {
        badgeView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * 设置menuItem点击事件
     * @param lis
     * @return [ActionProvider2]
     */
    fun setOnMenuItemClickListener(lis: ((ActionProvider2) -> Unit)?): ActionProvider2 {
        ly.setOnClickListener { lis?.invoke(this) };return this
    }

    private val ly by lazy {
        FrameLayout(context!!).apply {
            val defaultH = context.resources.getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material) - (10 * context.resources.displayMetrics.density).toInt() // 盲减, 约等于默认menuItem宽度
            layoutParams = ViewGroup.LayoutParams(defaultH, MATCH_PARENT)
            setBackgroundResource(TypedValue().also { getContext().theme.resolveAttribute(android.R.attr.actionBarItemBackground, it, true) }.resourceId)
            addView( // 增加图标
                AppCompatImageView(context).apply {
                    iconView = this
                    layoutParams = FrameLayout.LayoutParams(defaultH / 2, defaultH / 2).apply { gravity = Gravity.CENTER }
                    scaleType = ImageView.ScaleType.CENTER;setImageDrawable(item.icon)
                })
            addView( // 增加badge
                BadgeTextView(context).apply {
                    badgeView = this
                    val w = (14 * context.resources.displayMetrics.density).toInt()
                    layoutParams = FrameLayout.LayoutParams(w, w).apply { gravity = Gravity.CENTER; leftMargin = defaultH / 4; bottomMargin = defaultH / 4 }
                    gravity = Gravity.CENTER; setTextColor(Color.WHITE); textSize = 9F; visibility = View.INVISIBLE
                }
            )
        }
    }
    override fun onCreateActionView(): View = ly
}

/* ----------KTX----------- */

/**
 * 设置Toolbar居中的标题
 * 新增一个TextView, 相对于Toolbar完全居中
 *
 * @param title 标题
 * @return AppCompatTextView
 */
fun Toolbar?.setTitleOfCenter(title: CharSequence?): AppCompatTextView? {
    return ToolbarUtil.setTitleOfCenter(this, title)
}

/**
 * 设置Toolbar标题居中
 * 不改变原有结构, 相对于本身TextView居中
 *
 * @param title 标题 (注意其执行顺序, Toolbar内部动态addView)
 */
fun Toolbar?.setTitleCenter(title: CharSequence?) { ToolbarUtil.setTitleCenter(this, title) }

/**
 * 快捷增加menu
 * @param itemId menuId 0x01
 * @param iconRes 图标
 * @param title 标题
 * @param actionEnum 展现方式
 *                  总是显示在界面上 MenuItem.SHOW_AS_ACTION_ALWAYS
 *                  不显示在界面上 MenuItem.SHOW_AS_ACTION_NEVER
 *                  如果有位置才显示, 不然就出现在右边的三个点中 MenuItem.SHOW_AS_ACTION_IF_ROOM
 */
fun Toolbar?.addMenu(itemId: Int,
                     @DrawableRes iconRes: Int,
                     title: CharSequence,
                     actionEnum: Int = MenuItem.SHOW_AS_ACTION_IF_ROOM) {
    ToolbarUtil.addMenu(this, itemId, iconRes, title, actionEnum)
}
fun Menu?.addMenu(itemId: Int,
                  @DrawableRes iconRes: Int,
                  title: CharSequence,
                  actionEnum: Int = MenuItem.SHOW_AS_ACTION_IF_ROOM) {
    ToolbarUtil.addMenu(this, itemId, iconRes, title, actionEnum)
}

/**
 * 快捷增加带角标的menu
 * @return [ActionProvider2]
 */
fun Toolbar?.addMenuBadge(itemId: Int,
                          @DrawableRes iconRes: Int,
                          title: CharSequence,
                          menuItemClickListener: ((ActionProvider2) -> Unit)?=null): ActionProvider2? {
    return ToolbarUtil.addMenuBadge(this, itemId, iconRes, title, menuItemClickListener)
}
fun Menu?.addMenuBadge(ctx: Context?,
                       itemId: Int,
                       @DrawableRes iconRes: Int,
                       title: CharSequence,
                       menuItemClickListener: ((ActionProvider2) -> Unit)? = null): ActionProvider2? {
    return ToolbarUtil.addMenuBadge(this, ctx, itemId, iconRes, title, menuItemClickListener)
}

/**
 * 获取带角标的ActionProvider2
 * @return [ActionProvider2]
 */
fun Toolbar?.getActionProvider2(itemId: Int): ActionProvider2? {
    return ToolbarUtil.getActionProvider2(this, itemId)
}
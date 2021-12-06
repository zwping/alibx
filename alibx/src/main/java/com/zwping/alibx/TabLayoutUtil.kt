package com.zwping.alibx

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ImageViewCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout

/**
 * tabLayout扩展
 * zwping @ 2021/10/28
 */
private interface ITabLayout {
    /**
     * 借助[TabLayoutCustomView]实现类[BottomNavigationView]控件
     */
    fun initBottomNavigationView(tabLayout: TabLayout?, tabSize: Int, block: TabLayoutCustomView.(index: Int) -> Unit)
    fun getTabLayoutCustomView(tabLayout: TabLayout?, index: Int): TabLayoutCustomView?

    /**
     * 增加小红点
     * @param index
     * @param num null -> 小红点
     */
    fun setBadge(tabLayout: TabLayout?, index: Int, num: Int?=null)
    fun removeBadge(tabLayout: TabLayout?, index: Int)
}

object TabLayout: ITabLayout {

    override fun initBottomNavigationView(tabLayout: TabLayout?,
                                          tabSize: Int,
                                          block: TabLayoutCustomView.(index: Int) -> Unit) {
        tabLayout ?: return
        tabLayout.setSelectedTabIndicatorHeight(0) // 去掉indicator
        for(index in 0 until tabSize) {
            val tab = tabLayout.newTab()
            val cus = TabLayoutCustomView(tabLayout.context)
            tab.customView = cus
            block.invoke(cus, index)
            tabLayout.addTab(tab)
            ViewCompat.setPaddingRelative(tab.view, 0, 0, 0, 0) // 重置padding
        }
    }

    override fun getTabLayoutCustomView(tabLayout: TabLayout?, index: Int): TabLayoutCustomView? {
        tabLayout ?: return null
        val cus = tabLayout.getTabAt(index)?.customView
        if (cus is TabLayoutCustomView) return cus
        return null
    }

    override fun setBadge(tabLayout: TabLayout?, index: Int, num: Int?) {
        tabLayout ?: return
        getTabLayoutCustomView(tabLayout, index)?.also { it.setBadge(num); return }
        tabLayout.getTabAt(index)?.also {
            it.orCreateBadge.also { if (num != null) it.number = num }
        }
    }

    override fun removeBadge(tabLayout: TabLayout?, index: Int) {
        tabLayout ?: return
        getTabLayoutCustomView(tabLayout, index)?.also { it.removeBadge(); return }
        tabLayout.getTabAt(index)?.also {
            it.removeBadge()
        }
    }

}


class TabLayoutCustomView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    // 公开随便修改
    val titleTv by lazy { AppCompatTextView(context).apply { gravity = Gravity.CENTER_HORIZONTAL; maxLines = 1; ellipsize = TextUtils.TruncateAt.END } } // android.R.id.text1
    val iconIv by lazy { ShapeableImageView(context) }   // ShapeableImageView可自定义形状 android.R.id.icon
    val badgeTv by lazy { AppCompatTextView(context).also {
        addView(it, LayoutParams(-2, -2).apply { gravity = Gravity.CENTER_HORIZONTAL })
        it.gravity = Gravity.CENTER; it.setLines(1); it.visibility = View.GONE
        3F.dp2Px().also { t -> it.setPadding(t, 0, t, 0) }
        it.includeFontPadding = false
    } }

    init {
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(iconIv)
            addView(titleTv)
        })
        // initStyle()
    }

    fun initStyle(tvSizeSp: Float=13F, norTitleColor: Int= Color.GRAY, selTitleColor: Int= Color.BLACK,
                  tvMarginTopDp: Float=3F,
                  iconWHDp: Float=24F, norIconColor: Int?=null, selIconColor: Int?=null) {
        titleTv.textSize = tvSizeSp
        titleTv.setTextColor(
            ColorStateList(arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
                intArrayOf(selTitleColor, norTitleColor))
        )
        titleTv.updateLayoutParams<MarginLayoutParams> { topMargin = tvMarginTopDp.dp2Px() }
        iconIv.updateLayoutParams { iconWHDp.dp2Px().also { width = it; height =it } }
        if (norIconColor != null && selIconColor != null) {
            ImageViewCompat.setImageTintList(iconIv, ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
                intArrayOf(selIconColor, norIconColor)))
        }
    }

    /**
     * 初始化红点样式, 如果需要自定义可[getTabLayoutCustomView]获取[badgeTv]自定义
     *
     */
    private fun initBadgeStyle(isPoint: Boolean) {
        val h = (if(isPoint) 5F else 15F).dp2Px()
        badgeTv.minWidth = h
        badgeTv.updateLayoutParams<MarginLayoutParams> {
            height = h
            leftMargin = 12F.dp2Px() // topMargin = 3F.dp2Px()
        }
        createBadgeBg(h, strokeWDp = 0.5F, strokeColor = Color.WHITE)
        badgeTv.textSize = 10F
        badgeTv.setTextColor(Color.WHITE)
    }

    fun setBadge(num: Int?=null) {
        initBadgeStyle(num == null || num < 1)
        when (num) {
            null -> badgeTv.text = ""
            0 -> { badgeTv.text = "$num"; removeBadge(); return }
            else -> badgeTv.text = "${if (num>99) "99+" else num}"
        }
        if (badgeTv.visibility == View.GONE) {
            badgeTv.scaleX = 0F; badgeTv.scaleY = 0F
            badgeTv.animate().scaleX(1F).scaleY(1F).start()
        }
        badgeTv.visibility = View.VISIBLE
    }

    fun removeBadge() {
        badgeTv.animate().scaleX(0F).scaleY(0F).withEndAction {
            badgeTv.visibility = View.GONE
        }.start()
    }

    /*** 创建badgeTv背景 ***/
    fun createBadgeBg(tvHPx: Int, color: Int=Color.RED, strokeWDp: Float?=null, strokeColor: Int?=null) {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = tvHPx/2F
            if (strokeWDp != null && strokeColor != null) setStroke(strokeWDp.dp2Px(), strokeColor)
        }
        ViewCompat.setBackground(badgeTv, bg)
    }
    private fun Float.dp2Px() = (0.5F+this*Resources.getSystem().displayMetrics.density).toInt()
}

/* ----------KTX----------- */

/**
 * 借助[TabLayoutCustomView]实现类[BottomNavigationView]控件
 */
fun TabLayout?.initBottomNavigationView(tabSize: Int, block: TabLayoutCustomView.(index: Int) -> Unit) {
    com.zwping.alibx.TabLayout.initBottomNavigationView(this, tabSize, block)
}
fun TabLayout?.getTabLayoutCustomView(index: Int): TabLayoutCustomView? {
    return com.zwping.alibx.TabLayout.getTabLayoutCustomView(this, index)
}

/**
 * 增加小红点
 * @param index
 * @param num null -> 小红点
 */
fun TabLayout?.setBadge(index: Int, num: Int?=null) { com.zwping.alibx.TabLayout.setBadge(this, index, num) }
fun TabLayout?.removeBadge(index: Int) { com.zwping.alibx.TabLayout.removeBadge(this, index) }
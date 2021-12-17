package com.zwping.alibx

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * 资源的扩展
 *  - [dp2px] [px2dp]
 *  - [GradientDrawable] 具备不同形状/渐变的Drawable
 *  - [StateListDrawable] 代码创建view不同状态[States]的Drawable
 *  - [ColorStateList2] 代码创建view不同状态[States]的Color
 *
 * zwping @ 12/29/20
 */
object ResourceUtil {

    private val density by lazy { Resources.getSystem().displayMetrics.density }

    fun dpToPx(value: Float): Float = 0.5f + value * density
    fun dp2px(value: Float): Int = dpToPx(value).toInt()
    fun px2dp(value: Int): Float = value / density

    fun toInt2(str: String?): Int {
        return try { if (str.isNullOrBlank()) 0 else str.toInt() } catch (e: Exception) { 0 }
    }

    fun getColor2(ctx: Context, id: Int): Int = ContextCompat.getColor(ctx, id)

    /**
     * xml shape java代码实现方式
     * @param shape
     *  [GradientDrawable.LINE] 线
     *  [GradientDrawable.OVAL] 圆
     *  [GradientDrawable.RECTANGLE] 矩形
     *  [GradientDrawable.LINEAR_GRADIENT] 虚线矩形
     */
    fun createGradientDrawable(shape: Int=GradientDrawable.RECTANGLE,
                               block: GradientDrawable.() -> Unit): Drawable {
        return GradientDrawable().also { it.shape = shape; block(it) }
    }

    /**
     * xml selector item drawable java代码实现, 代码创建view不同状态的Drawable
     */
    fun createStateListDrawable(ctx: Context?,
                                @DrawableRes defaultRes: Int,
                                block: (States<Int>) -> Unit = {}): StateListDrawable? {
        if (null == ctx) return null
        val states = States<Int>().also(block)
        return StateListDrawable().apply {
            states.map.forEach { addState(intArrayOf(it.key), ContextCompat.getDrawable(ctx, it.value)) }
            addState(intArrayOf(), ContextCompat.getDrawable(ctx, defaultRes))
        }
    }

    fun createStateListDrawable(default: Drawable,
                                block: (States<Drawable>) -> Unit = {}): StateListDrawable {
        val states = States<Drawable>().also(block)
        return StateListDrawable().apply {
            states.map.forEach { addState(intArrayOf(it.key), it.value) }
            addState(intArrayOf(), default)
        }
    }

}

/*** xml selector item color java代码实现 ***/
class ColorStateList2(@ColorInt defaultColor: Int, dsl: States<Int>.() -> Unit = {}) {
    private val ids = mutableListOf<IntArray>()
    private val colors = mutableListOf<Int>()

    init {
        val states = States<Int>(); dsl.invoke(states)
        states.map.entries.forEach { ids.add(intArrayOf(it.key));colors.add(it.value) }
        ids.add(intArrayOf());colors.add(defaultColor)
    }

    fun create(): ColorStateList = ColorStateList(ids.toTypedArray(), colors.toIntArray())
}

/*** view状态的实现 ***/
class States<T> {
    val map = hashMapOf<Int, T>()
    fun pressed(value: T?) { value?.also { map[android.R.attr.state_pressed] = value } }
    fun focused(value: T?) { value?.also { map[android.R.attr.state_focused] = value } }
    fun selected(value: T?) { value?.also { map[android.R.attr.state_selected] = value } }
    fun checkable(value: T?) { value?.also { map[android.R.attr.state_checkable] = value } }
    fun checked(value: T?) { value?.also { map[android.R.attr.state_checked] = value } }
    @Deprecated("命名不恰当", ReplaceWith("unEnabled(value)", "android"))
    fun enabled(value: T?) { value?.also { map[-android.R.attr.state_enabled] = value } }
    fun unEnabled(value: T?) { value?.also { map[-android.R.attr.state_enabled] = value } }
    fun window_focused(value: T?) { value?.also { map[android.R.attr.state_window_focused] = value } }
}

/* ---------KTX----------- */

fun Float.dpToPx(): Float = ResourceUtil.dpToPx(this)
fun Float.dp2px(): Int = ResourceUtil.dp2px(this)
fun Int.px2dp(): Float = ResourceUtil.px2dp(this)

fun String?.toInt2(): Int = ResourceUtil.toInt2(this)

fun Context.getColor2(@ColorRes id: Int): Int = ResourceUtil.getColor2(this, id)

/**
 * xml shape java代码实现方式
 * @param shape [GradientDrawable.LINE] 线 [GradientDrawable.OVAL] 圆
 * [GradientDrawable.RECTANGLE] 矩形 [GradientDrawable.LINEAR_GRADIENT] 虚线矩形
 */
fun createGradientDrawable(shape: Int=GradientDrawable.RECTANGLE,
                           block: GradientDrawable.() -> Unit): Drawable {
    return ResourceUtil.createGradientDrawable(shape, block)
}

/**
 * xml selector item drawable java代码实现, 代码创建view不同状态的Drawable
 */
fun createStateListDrawable(ctx: Context?,
                            @DrawableRes defaultRes: Int,
                            block: (States<Int>) -> Unit = {}): StateListDrawable? {
    return ResourceUtil.createStateListDrawable(ctx, defaultRes, block)
}
fun createStateListDrawable(default: Drawable,
                            block: (States<Drawable>) -> Unit = {}): StateListDrawable {
    return ResourceUtil.createStateListDrawable(default, block)
}
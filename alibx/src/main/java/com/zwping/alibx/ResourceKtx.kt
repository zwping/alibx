package com.zwping.alibx

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
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
val density by lazy { Resources.getSystem().displayMetrics.density }
inline fun Float.dpToPx(): Float = 0.5f + this * density
inline fun Float.dp2px(): Int = dpToPx().toInt()
inline fun Int.px2dp(): Float = (this / density)

inline fun String?.toInt2(): Int = if (this == null || this == "") 0 else toInt()

inline fun Context.getColor2(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

/**
 * xml shape java代码实现方式
 * @param shape [GradientDrawable.LINE] 线 [GradientDrawable.OVAL] 圆
 * [GradientDrawable.RECTANGLE] 矩形 [GradientDrawable.LINEAR_GRADIENT] 虚线矩形
 */
inline fun GradientDrawable.create(shape: Int, dsl: GradientDrawable.() -> Unit): Drawable {
    this.shape = shape
    dsl.invoke(this)
    return this
}
inline fun GradientDrawable.create(dsl: GradientDrawable.() -> Unit): Drawable { return create(GradientDrawable.RECTANGLE, dsl) }

/**
 * xml selector item drawable java代码实现, 代码创建view不同状态的Drawable
 * @param defaultRes @DrawableRes or @Drawable
 */
inline fun <T> StateListDrawable.create(ctx: Context?, defaultRes: T, dsl: States<T>.() -> Unit = {}): StateListDrawable {
    if (null == ctx) return this
    val states = States<T>(); dsl.invoke(states)
    states.map.entries.forEach { val v = it.value; addState(intArrayOf(it.key), if (v is Drawable) v else ContextCompat.getDrawable(ctx, v as Int)) }
    addState(intArrayOf(), if (defaultRes is Drawable) defaultRes else ContextCompat.getDrawable(ctx, defaultRes as Int))
    return this
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
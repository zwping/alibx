package com.zwping.alibx

import android.animation.Animator
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton

/**
 * View相关辅助操作
 * zwping @ 2021/11/30
 */

object ViewUtil{

    /*** 左右抖动 ***/
    fun shakelr(view: View?) {
        val animation = TranslateAnimation(0F, 10F, 0F, 0F)
        animation.interpolator = CycleInterpolator(5F)
        animation.duration = 1000
        view?.startAnimation(animation)
    }

    /**
     * 节流点击
     * @param time 点击事件最大时间间隔, 连点间隔时间才生效一次
     */
    fun <V: View> setOnClickThrottleListener(view: V?, time: Long=500L, block: (V) -> Unit) {
        view ?: return
        var lastTime = 0L
        view.setOnClickListener {
            System.currentTimeMillis().also {
                if (it - lastTime > time) { lastTime = it; block(view) }
            }
        }
    }

    /**
     * 防抖点击
     * @param time 点击最大时间间隔, 连点无效
     */
    fun <V: View> setOnClickDebounceListener(view: V?, time: Long=500L, block: (V) -> Unit) {
        view ?: return
        var lastTime = 0L
        view.setOnClickListener {
            System.currentTimeMillis().also {
                if (it - lastTime < time) { lastTime = it; return@also }
                lastTime = it; block(view)
            }
        }
    }

    fun showLoading(btn: MaterialButton?, @ColorInt color: Int?=btn?.textColors?.defaultColor, enabled: Boolean?=false) {
        btn ?: return
        enabled?.also { btn.isEnabled = it }
        btn.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        var dw = btn.icon
        if (dw !is Animatable) {
            val value = TypedValue()
            btn.context.theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
            val progressBarStyle = value.data
            val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
            val typedArray = btn.context.obtainStyledAttributes(progressBarStyle, attributes)
            dw  = typedArray.getDrawable(0)
            typedArray.recycle()
            btn.icon = dw
        }
        color?.also { dw?.setTint(it) }
        if (dw is Animatable) { dw.start() }
    }

    fun hideLoading(btn: MaterialButton?, enabled: Boolean?=true) {
        btn ?: return
        enabled?.also { btn.isEnabled=it }; btn.icon = null
    }

    /**
     * 转移焦点
     */
    fun focus(view: View?) {
        view?.apply { isFocusable = true; isFocusableInTouchMode = true; requestFocus() }
    }

    /**
     * 获取字符宽、高
     */
    fun measureTextWidth(text: String, textSize: Float): IntArray {
        val paint = Paint()
        paint.textSize = textSize
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return intArrayOf(rect.width(), rect.height())
    }

    /**
     * 水波纹
     * @param circle true -> actionBar Navigation点击圆圈效果 / false -> 默认水波纹
     */
    fun setRipple(view: View?, circle: Boolean=false) {
        view ?: return
        val typed = TypedValue()
        val rid = if (circle) android.R.attr.actionBarItemBackground else android.R.attr.selectableItemBackground
        view.context.theme.resolveAttribute(rid, typed, true)
        if (typed.resourceId != 0) view.setBackgroundResource(typed.resourceId)
        else view.setBackgroundColor(typed.data)
    }

    /* ====================== */
    fun View?.setGone(gone: Boolean) { this?.apply { visibility = if (gone) View.GONE else View.VISIBLE } }
    fun View?.setInvisible(invisible: Boolean) { this?.apply { visibility = if (invisible) View.INVISIBLE else View.VISIBLE } }

}
/* ---------KTX----------- */

/*** 左右抖动 ***/
fun View?.shakelr() { ViewUtil.shakelr(this) }

/**
 * 节流点击
 * @param time 点击事件最大时间间隔, 连点间隔时间才生效一次
 */
fun <V: View> V?.setOnClickThrottleListener(time: Long=500L, block: (V) -> Unit) {
    ViewUtil.setOnClickThrottleListener(this, time, block)
}
/**
 * 防抖点击
 * @param time 点击最大时间间隔, 连点无效
 */
fun <V: View> V?.setOnClickDebounceListener(time: Long=500L, block: (V) -> Unit) {
    ViewUtil.setOnClickDebounceListener(this, time, block)
}

fun MaterialButton?.showLoading(@ColorInt color: Int?=this?.textColors?.defaultColor, enabled: Boolean?=false) {
    ViewUtil.showLoading(this, color, enabled)
}
fun MaterialButton?.hideLoading(enabled: Boolean?=true) {
    ViewUtil.hideLoading(this, enabled)
}
/**
 * 转移焦点
 */
fun View?.focus() {
    ViewUtil.focus(this)
}

/**
 * 水波纹
 * @param circle true -> actionBar Navigation点击圆圈效果 / false -> 默认水波纹
 */
fun View?.setRipple(circle: Boolean=false) {
    ViewUtil.setRipple(this, circle)
}

/**
 * [View.animate].setListener扩展, @see [Animator.addListener]
 */
inline fun ViewPropertyAnimator.setListener(
    crossinline onEnd: (animator: Animator) -> Unit = {},
    crossinline onStart: (animator: Animator) -> Unit = {},
    crossinline onCancel: (animator: Animator) -> Unit = {},
    crossinline onRepeat: (animator: Animator) -> Unit = {}
): ViewPropertyAnimator {
    val listener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animator: Animator) = onRepeat(animator)
        override fun onAnimationEnd(animator: Animator) = onEnd(animator)
        override fun onAnimationCancel(animator: Animator) = onCancel(animator)
        override fun onAnimationStart(animator: Animator) = onStart(animator)
    }
    setListener(listener)
    return this
}

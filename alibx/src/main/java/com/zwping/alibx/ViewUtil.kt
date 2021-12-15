package com.zwping.alibx

import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton

/**
 * View相关辅助操作
 * zwping @ 2021/11/30
 */
interface IView{
    /*** 左右抖动 ***/
    fun shakelr(view: View?)

    /**
     * 节流点击
     * @param time 点击事件最大时间间隔, 连点间隔时间才生效一次
     */
    fun <V: View> setOnClickThrottleListener(view: V?, time: Long=500L, block: (V) -> Unit)
    /**
     * 防抖点击
     * @param time 点击最大时间间隔, 连点无效
     */
    fun <V: View> setOnClickDebounceListener(view: V?, time: Long=500L, block: (V) -> Unit)

    fun showLoading(btn: MaterialButton?, @ColorInt color: Int?=btn?.textColors?.defaultColor, enabled: Boolean?=false)
    fun hideLoading(btn: MaterialButton?, enabled: Boolean?=true)

    /**
     * 转移焦点
     */
    fun focus(view: View?)
}

object ViewUtil : IView{

    override fun shakelr(view: View?) {
        val animation = TranslateAnimation(0F, 10F, 0F, 0F)
        animation.interpolator = CycleInterpolator(5F)
        animation.duration = 1000
        view?.startAnimation(animation)
    }

    override fun <V : View> setOnClickThrottleListener(view: V?, time: Long, block: (V) -> Unit) {
        view ?: return
        var lastTime = 0L
        view.setOnClickListener {
            System.currentTimeMillis().also {
                if (it - lastTime > time) { lastTime = it; block(view) }
            }
        }
    }

    override fun <V : View> setOnClickDebounceListener(view: V?, time: Long, block: (V) -> Unit) {
        view ?: return
        var lastTime = 0L
        view.setOnClickListener {
            System.currentTimeMillis().also {
                if (it - lastTime < time) { lastTime = it; return@also }
                lastTime = it; block(view)
            }
        }
    }

    override fun showLoading(btn: MaterialButton?, color: Int?, enabled: Boolean?) {
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

    override fun hideLoading(btn: MaterialButton?, enabled: Boolean?) {
        btn ?: return
        enabled?.also { btn.isEnabled=it }; btn.icon = null
    }

    override fun focus(view: View?) {
        view?.apply { isFocusable = true; isFocusableInTouchMode = true; requestFocus() }
    }
}

/* ----------KTX----------- */

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
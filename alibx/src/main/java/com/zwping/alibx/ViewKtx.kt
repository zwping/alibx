package com.zwping.alibx

import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton

/**
 * View相关辅助操作
 */
interface ViewKtxInterface{
    /*** 左右抖动 ***/
    fun View.shakelr()

    fun MaterialButton?.showLoading(@ColorInt color: Int?=this?.textColors?.defaultColor, enabled: Boolean?=false)
    fun MaterialButton?.hideLoading(enabled: Boolean?=true)
}

object ViewKtx : ViewKtxInterface{

    override fun View.shakelr() {
        val animation = TranslateAnimation(-5F, 5F, 0F, 0F)
        animation.interpolator = OvershootInterpolator()
        animation.duration = 100
        animation.repeatCount = 3
        animation.repeatMode = Animation.REVERSE
        startAnimation(animation)
    }

    override fun MaterialButton?.showLoading(@ColorInt color: Int?, enabled: Boolean?) {
        this ?: return
        enabled?.also { isEnabled = it }
        iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        var dw = icon
        if (dw !is Animatable) {
            val value = TypedValue()
            context.theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
            val progressBarStyle = value.data
            val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
            val typedArray = context.obtainStyledAttributes(progressBarStyle, attributes)
            dw  = typedArray.getDrawable(0)
            typedArray.recycle()
            icon = dw
        }
        color?.also { dw?.setTint(it) }
        if (dw is Animatable) { dw.start() }
    }
    override fun MaterialButton?.hideLoading(enabled: Boolean?) {
        this ?: return
        enabled?.also { isEnabled = it }; icon = null
    }
}

/*** 节流点击 ***/
inline fun <V: View> V?.setOnClickThrottleListener(time: Long = 500L, crossinline block: (V) -> Unit) {
    this ?: return
    var lastTime = 0L
    setOnClickListener {
        System.currentTimeMillis().also { // 每隔0.5秒只能点击一次
            if (it - lastTime > time) { lastTime = it; block(this) }
        }
    }
}
/*** 防抖点击 ***/
//inline fun <V: View> V?.setOnClickDebounceListener(time: Long = 500L, crossinline block: (V) -> Unit) {
//}

/*** View双击 ***/
inline fun <V : View> V.setOnDoubleClickListener(intervalTime: Long = 500L, crossinline secondClickListener: (V) -> Unit) {
    var lastTime = 0L
    setOnClickListener {
        val curTime = System.currentTimeMillis()
        if (curTime - lastTime > intervalTime) {
            lastTime = curTime
        } else {
            lastTime = 0L
            secondClickListener(this)
        }
    }
}

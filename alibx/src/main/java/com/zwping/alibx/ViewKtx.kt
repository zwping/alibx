package com.zwping.alibx

import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Checkable

/**
 *
 * zwping @ 1/11/21
 *
 * View防抖点击
 */
inline fun <V : View> V.setOnDebounceClickListener(time: Long = 500L, crossinline lambda: (V) -> Unit) {
    var lastTime = 0L
    setOnClickListener {
        val curTime = System.currentTimeMillis()
        if (curTime - lastTime > time) { // 0.5秒只能点击一次
            lastTime = curTime
            lambda(this)
        }
    }
}

/*** View双击 ***/
inline fun <V : View> V.setOnDoubleClickListener(
        intervalTime: Long = 500L,
        crossinline secondClickListener: (V) -> Unit) {
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

/*** 左右抖动 ***/
inline fun View.shakelr() {
    val animation = TranslateAnimation(-5F, 5F, 0F, 0F)
    animation.interpolator = OvershootInterpolator()
    animation.duration = 100
    animation.repeatCount = 3
    animation.repeatMode = Animation.REVERSE
    startAnimation(animation)
}
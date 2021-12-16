package com.zwping.a

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.marginLeft
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.zwping.alibx.*
import com.zwping.alibx.ImageLoader.KTX.glide
import com.zwping.alibx.ResourceUtil.KTX.createGradientDrawable
import com.zwping.alibx.ResourceUtil.KTX.dp2px
import kotlin.random.Random

/**
 *
 * zwping @ 2021/12/6
 */
@SuppressLint("ViewConstructor")
class LayerFindTeacher(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
//        setBackgroundColor(Color.DKGRAY)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Math.min(measuredWidth, measuredHeight).also {
            setMeasuredDimension(it, it)
            r = it
        }
    }

    private var r1 = 60F.dp2px() // 起始直径
    private var r = 0 // 直径
    var avatarWidth = 25F.dp2px()
    var cw = 2F.dp2px()  // 线宽
    var cc = (0xffE1EBF5).toInt()
    var speed = 2000L
    var density = 6 // 密度, 多少个圆


    fun start(owner: LifecycleOwner, avatars: MutableList<String>?) {
        val delay = speed / density - 0
        ITimer({
            addView(circle())
            if (it.count >= density)  {
                it.cancel()
            }
            if (it.count >= density/2 && lastAvatar == "" && !avatars.isNullOrEmpty()) {
                nextAvatar(avatars)
            }
        }, delay, delay).schedule(owner)
    }

    private var lastAvatar = ""
    private val iv by lazy { ImageView(context).also {
        it.layoutParams = ViewGroup.LayoutParams(avatarWidth, avatarWidth)
        addView(it)
    } }
    private fun nextAvatar(avatars: MutableList<String>){
        if (visibility != View.VISIBLE) return
        avatars[Random.nextInt(0, avatars.size)].also {
            if (lastAvatar == it) { nextAvatar(avatars); return }
            lastAvatar = it
            iv.glide(it) { circleCrop() }
            val range = r-avatarWidth
            if (range <= 0) return@also
            iv.updateLayoutParams<MarginLayoutParams> {
                var t = Random.nextInt(0, range)
                var l = Random.nextInt(0, range)
                if (r1 != 0 && t in r/2-r1/2 .. r/2+r1/2) {
                    t = if (Random.nextBoolean()) (t+r1) else (t-r1)
                }
                if (r1 != 0 && l in r/2-r1/2 .. r/2+r1/2) {
                    l = if (Random.nextBoolean()) (l+r1) else (l-r1)
                }
                topMargin = t; leftMargin = l
            }
            val anim = ObjectAnimator.ofFloat(iv, "alpha", 0F, 1F, 0F)
            anim.repeatCount = 0
            // anim.duration = speed
            anim.duration = 1000
            anim.addListener(object: Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    nextAvatar(avatars)
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
            anim.start()
        }
    }

    private fun circle() = View(context).also {
        it.layoutParams = ViewGroup.LayoutParams(r-cw/2, r-cw/2)
        it.background = createGradientDrawable {
            shape = GradientDrawable.OVAL
            setStroke(cw, cc)
            setColor(Color.TRANSPARENT)
        }
        anim(it)
    }

    private fun anim(it: View) {
        if (r == 0) return
        val s1 = r1/r.toFloat()
        val scaleX = ObjectAnimator.ofFloat(it, "scaleX", s1, 1.1F).also {
            it.duration = speed
            it.repeatCount = ValueAnimator.INFINITE
            it.interpolator = AccelerateInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(it, "scaleY", s1, 1.1F).also {
            it.duration = speed
            it.repeatCount = ValueAnimator.INFINITE
            it.interpolator = AccelerateInterpolator()
        }
        val alpha = ObjectAnimator.ofFloat(it, "alpha", 0.3F, 0.9F, 0.4F, 0.0F).also {
            it.duration = speed
            it.repeatCount = ValueAnimator.INFINITE
            it.interpolator = AccelerateInterpolator()
        }
        anims.add(scaleX)
        anims.add(scaleY)
        anims.add(alpha)
        scaleX.start()
        scaleY.start()
        alpha.start()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            anims.forEach { it.pause() }
        }
    }
    private val anims = mutableListOf<ObjectAnimator>()

}
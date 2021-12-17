package com.zwping.alibx

import android.app.Application
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.core.view.*

/**
 * toast
 * zwping @ 2021/11/21
 */
object ToastUtil {

    private var app: Application? = null
    private val globalOption by lazy { ToastUtilOption() }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var toast: Toast? = null
        get() {
            if (field != null) return field
            if (app == null) return null
             field = Toast.makeText(app, "", Toast.LENGTH_SHORT)
            return field
        }

    fun init(app: Application, option: (ToastUtilOption)-> Unit = {}) {
        this.app = app; globalOption.also(option)
    }

    fun show(msg: Any?) {
        show("$msg", duration = Toast.LENGTH_SHORT)
    }

    @JvmStatic
    @JvmOverloads
    fun show(msg: CharSequence,
             @IntRange(from=0, to=1) duration: Int = Toast.LENGTH_SHORT,
             option: (ToastUtilOption) -> Unit={}) {
        runOnUi {
            cancel()
            toast?.also {
                val opt = globalOption.clone().also(option)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                    app?.applicationInfo?.targetSdkVersion ?: 0 < Build.VERSION_CODES.R) {
                    it.setGravity(opt.gravity, opt.xOffset, opt.yOffset)
                    it.view?.also {
                        it.setPadding(0)
                        it.background = opt.msgBg
                        it.findViewById<TextView>(android.R.id.message)?.also {
                            it.setTextColor(opt.msgColor)
                            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, opt.msgSize)
                            it.setPadding(0)
                            it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                setMargins(opt.msgMarginLeftRight, opt.msgMarginTopBottom, opt.msgMarginLeftRight, opt.msgMarginTopBottom)
                            }
                        }
                    }
                }
                it.duration = duration
                it.setText(msg)
                it.show()
            }
        }
    }

    fun cancel() {
        toast?.cancel()
        toast = null
    }

    private fun runOnUi(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) { runnable.run(); return }
        handler.post(runnable)
    }

}

/**
 * toast是一个application级别的事件
 * android 11及以后, toast交由Android系统显示, 不能定制背景色及位置
 */
class ToastUtilOption {

    var gravity: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
    var xOffset = 0
    var yOffset = dpToPx(80F).toInt()

    var msgMarginTopBottom = dpToPx(6F).toInt()
    var msgMarginLeftRight = dpToPx(14F).toInt()
    var msgSize = dpToPx(14F)
    var msgColor = Color.WHITE
    var msgBg = GradientDrawable().also {
        it.setColor((0xBB000000).toInt())
        it.cornerRadius = dpToPx(6F)
    }

    fun clone() = ToastUtilOption().also {
        it.gravity = gravity
        it.xOffset = xOffset
        it.yOffset = yOffset
        it.msgMarginTopBottom = msgMarginTopBottom
        it.msgMarginLeftRight = msgMarginLeftRight
        it.msgSize = msgSize
        it.msgColor = msgColor
        it.msgBg = msgBg
    }

    private fun dpToPx(dp: Float): Float = 0.5F + dp*Resources.getSystem().displayMetrics.density
}
/* ---------KTX----------- */
fun showToast(msg: Any?, duration: Int=Toast.LENGTH_SHORT) { ToastUtil.show("$msg", duration=duration) }

package com.zwping.alibx

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView

/**
 * 状态布局切换 (loading / empty / error|netError / content)
 * zwping @ 2021/11/18
 */
class StateLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        var cfg = Cfg()

        @JvmStatic
        fun init(cfg: Cfg) { this.cfg = cfg }

        // 代码引用
        fun wrap(ac: Activity?): StateLayout = wrap(ac?.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0))
        // fun wrap(fm: Fragment?): StateLayout = wrap(fm?.view?.apply { tag = R.id.fragment_container_view_tag }) // crash
        fun wrap(view: View?): StateLayout { // 不通用
            if (null == view) throw  RuntimeException("空view")
            val parent = view.parent as ViewGroup? ?: throw RuntimeException("无根view")
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            return StateLayout(view.context).also {
                it.addView(view, 0)
                parent.addView(it, index, view.layoutParams)
            }
        }

        data class Cfg(
            var LoadingLayoutId: Int? = null,
            var EmptyLayoutId: Int? = null,
            var ErrorLayoutId: Int? = null,
            var DefaultProgressBarColor: Int? = null,
            var DefaultEmptyIconResId: Int? = null,
            var DefaultErrorIconResId: Int? = null,
            var DefaultNetErrorIconResId: Int? = null,
            var DefaultLoadingTxt: CharSequence = "加载中...",
            var DefaultEmptyTxt: CharSequence = "暂无数据",
            var DefaultErrorTxt: CharSequence = "加载失败\n轻触屏幕重新加载",
            var DefaultNetErrorTxt: CharSequence = "无网络连接\n轻触屏幕重新加载",
            var DefaultRemindTxtColor: Int = Color.parseColor("#8a8a8a"),
            var DefaultRemindTxtSize: Float = 15F, // dp
            var DefaultIconWidth: Float = 85F, // dp
        )

        private val density by lazy { Resources.getSystem().displayMetrics.density }
        private fun Float.dpToPx(): Float = 0.5f + this * density
        private fun Float.dp2px(): Int = dpToPx().toInt()
    }

    enum class State { CONTENT, LOADING, EMPTY, ERROR }

    fun isLoadingState() = curState == State.LOADING
    // 句柄公开
    var curState: State = State.CONTENT // onFinishInflate后立即变为LOADING
        private set
    var animationDuration = 200L
    var retryClickListener: (() -> Unit)? = null
    val loadingView: View by lazy { inflaterView(cfg.LoadingLayoutId) { DefaultLoadingView(getContext()) } }
    val emptyView: View by lazy { inflaterView(cfg.EmptyLayoutId) { DefaultEmptyView(getContext()) } }
    val errorView: View by lazy { inflaterView(cfg.ErrorLayoutId) { DefaultErrorView(getContext()) }.apply {
        setOnClickListener { Toast.makeText(getContext(), "未注册retryClick", Toast.LENGTH_SHORT).show() }
    } }

    private val contentView: View? by lazy { if (childCount>0) getChildAt(0) else null }  // contentView只允许一个布局
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1)  {
            for (i in 0 until childCount) getChildAt(i).visibility = INVISIBLE
            addView(TextView(context).apply { gravity = Gravity.CENTER; setTextColor(Color.RED); text = "只允许挂载单一子布局" })
            throw RuntimeException("只允许挂载单一子布局")
        }
        // showLoadingView() // 影响xml编写, 白屏问题 @see init()
    }

    fun init(lis: () -> Unit) { showLoadingView(); this.retryClickListener = lis }
    fun showContentView() { showView(State.CONTENT) }
    fun showLoadingView(txt: CharSequence? = null) { showView(State.LOADING, txt) }
    @JvmOverloads
    fun showEmptyView(txt: CharSequence? = null, iconResId: Int? = null) { showView(State.EMPTY, txt, iconResId) }
    @JvmOverloads
    fun showErrorView(txt: CharSequence? = null, iconResId: Int? = null) { showView(State.ERROR, txt, iconResId) }
    // fun setRetryClickListener(lis: ()->Unit) { this.retryClickListener = lis }

    private fun inflaterView(layoutId: Int?, defaultViewLazy: () -> View) : View {
        return when {
            layoutId != null -> LayoutInflater.from(context).inflate(layoutId, null)
            else -> defaultViewLazy.invoke()
        }.also { it.visibility = INVISIBLE; addView(it) }
    }

    @SuppressLint("MissingPermission")
    private fun showView(state: State, txt: CharSequence? = null, iconResId: Int? = null) {
        if (curState == state) return
        if (state == State.ERROR && curState == State.CONTENT) return
        curState = state
        for (i in 0 until childCount) { getChildAt(i).also { it.animate().cancel(); it.visibility = INVISIBLE } }
        when(state){
            State.CONTENT -> {
                showViewAnim(contentView)
            }
            State.LOADING -> {
                showViewAnim(loadingView)
                loadingView.also {
                    if (it is DefaultLoadingView) txt?.also { s -> it.remindTv.text = s }
                }
            }
            State.EMPTY -> {
                showViewAnim(emptyView)
                emptyView.also {
                    if (it is DefaultEmptyView) {
                        txt?.also { s -> it.remindTv.text = s }
                        iconResId?.also { s -> it.iconIv.setImageResource(s) }
                    }
                }
            }
            State.ERROR -> {
                showViewAnim(errorView)
                errorView.also {
                    if (it is DefaultErrorView) {
                        txt?.also { s -> it.remindTv.text = s }
                        iconResId?.also { s -> it.iconIv.setImageResource(s) }
                        if (txt == null && iconResId == null) {
                            it.autoShowErrType()
                        }
                    }
                    retryClickListener?.also { lis -> errorView.setOnClickListener { showView(State.LOADING); lis() } }
                }
            }
        }
    }

    private fun showViewAnim(v: View?) {
        try {
            if (v == null) return
            v.animate().cancel()
            v.alpha = 0.1F
            v.animate().alpha(1F)
                .setDuration(animationDuration)
                .setStartDelay(if (curState == State.LOADING) 300 else 0) // 300ms后再展示loading
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        v.visibility = View.VISIBLE
                        if (curState == State.LOADING) contentView?.visibility = INVISIBLE
                    }
                })
                .start()
        } catch (e: Exception) { v?.alpha = 1F; v?.visibility = View.VISIBLE }
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    private fun DefaultErrorView.autoShowErrType() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val conn = cm?.activeNetworkInfo?.isConnected == true
        remindTv.text = if (conn) cfg.DefaultErrorTxt else cfg.DefaultNetErrorTxt
        if (cfg.DefaultErrorIconResId != null && cfg.DefaultNetErrorIconResId != null)
            iconIv.setImageResource(if (conn) cfg.DefaultErrorIconResId!! else cfg.DefaultNetErrorIconResId!!)
    }

    /** 默认View **/

    private inner class DefaultLoadingView(context: Context) : LinearLayout(context) {
        val progressBar by lazy { ProgressBar(context).apply {
            cfg.DefaultProgressBarColor?.also { indeterminateTintList = ColorStateList.valueOf(it) }
        } }
        val remindTv by lazy { AppCompatTextView(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(0, 10F.dp2px(), 0, 0)
            setTextColor(cfg.DefaultRemindTxtColor)
            textSize = cfg.DefaultRemindTxtSize
            text = cfg.DefaultLoadingTxt
        } }
        init {
            orientation = VERTICAL
            layoutTransition = LayoutTransition()
            gravity = Gravity.CENTER
            addView(progressBar)
            addView(remindTv)
        }
    }

    private inner class DefaultEmptyView(context: Context) : LinearLayout(context) {
        val iconIv by lazy { AppCompatImageView(context).apply {
            layoutParams = LayoutParams(cfg.DefaultIconWidth.dp2px(), ViewGroup.LayoutParams.WRAP_CONTENT)
            cfg.DefaultEmptyIconResId?.also { setImageResource(it) }
        } }
        val remindTv by lazy { AppCompatTextView(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setTextColor(cfg.DefaultRemindTxtColor)
            textSize = cfg.DefaultRemindTxtSize
            text = cfg.DefaultEmptyTxt
            gravity = Gravity.CENTER
        } }
        init {
            orientation = VERTICAL
            layoutTransition = LayoutTransition()
            gravity = Gravity.CENTER
            addView(iconIv)
            if (cfg.DefaultEmptyIconResId != null) remindTv.setPadding(0, 10F.dp2px(), 0, 0)
            addView(remindTv)
        }
    }

    private inner class DefaultErrorView(context: Context) : LinearLayout(context) {
        val iconIv by lazy { AppCompatImageView(context).apply {
            layoutParams = LayoutParams(cfg.DefaultIconWidth.dp2px(), ViewGroup.LayoutParams.WRAP_CONTENT)
            cfg.DefaultErrorIconResId?.also { setImageResource(it) }
        } }
        val remindTv by lazy { AppCompatTextView(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER
            setTextColor(cfg.DefaultRemindTxtColor)
            textSize = cfg.DefaultRemindTxtSize
            text = cfg.DefaultErrorTxt
        } }
        init {
            orientation = VERTICAL
            layoutTransition = LayoutTransition()
            gravity = Gravity.CENTER
            addView(iconIv)
            if (cfg.DefaultErrorIconResId != null) remindTv.setPadding(0, 10F.dp2px(), 0, 0)
            addView(remindTv)
        }
    }
}

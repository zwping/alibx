package com.zwping.alibx

import android.animation.LayoutTransition
import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

/**
 * 紧凑的封装
 * zwping @ 2021/10/18
 */
interface BaseAcInterface<VB: ViewBinding> {

    // 快捷
    fun onCreate2() { }

    // ViewBinding功能提供
    fun onCreateVB(inflater: LayoutInflater): VB? { return null }       // 实现
    val _binding: VB?
    val vb: VB get() = _binding!!                                       // 使用

    // LoadingDialog功能提供
    val _loading: Dialog
    fun showLoading() { showLoading(null, true) }
    fun showLoading(txt: CharSequence?=null, delayed: Boolean=true)
    fun hideLoading()

    // 辅助
    val handler: Handler
    fun dpToPx(dp: Float): Float { return 0.5F+dp*Resources.getSystem().displayMetrics.density }
}
interface BaseFmInterface<VB: ViewBinding> {

    // 快捷
    fun onViewCreated2() { }
    fun onLazyResume() { }

    // base 联动
    fun baseAc() = if (ac is BaseAc<*>) ac as BaseAc<ViewBinding> else null
    val ac: FragmentActivity?

    // ViewBinding功能提供
    fun onCreateVB(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean=false): VB? { return null }    // 实现, ide快捷输出
    val _binding: VB?
    val vb: VB get() = _binding!!       // 使用

    // LoadingDialog功能提供
    fun showLoading() { showLoading(null, true) }
    fun showLoading(txt: CharSequence?=null, delayed: Boolean=true) { baseAc()?.showLoading(txt, delayed) }
    fun hideLoading() { baseAc()?.hideLoading() }
}


/**
 * ac基类
 *      原始FindViewById写法
 *      class MainActivity: BaseAc<ViewBinding>(R.layout.activity_main) {
 *          fun onCreate(bundle) {
 *              showLoading(..)
 *              hideLoading()
 *              ...
 *          }
 *      }
 *      ViewBinding写法
 *      class MainActivity: BaseAc<ActivityMainBinding>() {
 *          fun onCreateVB(inflater) {
 *              return ActivityMainBinding.inflater(inflater)
 *          }
 *          fun onCreate2() {
 *              vb.toolbar...
 *              showLoading(..)
 *              hideLoading()
 *              ...
 *          }
 *      }
 */
open class BaseAc<VB: ViewBinding> : AppCompatActivity, BaseAcInterface<VB> {

    constructor()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)
    override val _binding: VB? by lazy { onCreateVB(layoutInflater) }
    override val _loading: Dialog by lazy { _initDialog() }
    override val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding?.also { setContentView(vb.root) }
        onCreate2()
    }

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }


    /* ----- 实现 ------ */

    @Synchronized
    override fun showLoading(txt: CharSequence?, delayed: Boolean) {
        txt?.also { _loading.findViewById<TextView>(android.R.id.text1)?.also { tv ->
            tv.visibility = View.VISIBLE; tv.text = it
        } }
        if (_loading.isShowing) return
        handler.removeCallbacks(_showRunnable)
        handler.removeCallbacks(_hideRunnable)
        handler.postDelayed(_showRunnable, if (delayed) 300 else 0) // 300ms后再展示
    }
    @Synchronized
    override fun hideLoading() {
        handler.removeCallbacks(_showRunnable)
        handler.removeCallbacks(_hideRunnable)
        val t = (System.currentTimeMillis() - _leastShowTime).also { if (it < 300) 300 - it else 0L } // 最少展示300ms, 避免闪烁
        handler.postDelayed(_hideRunnable, t)
    }
    private val _showRunnable = Runnable { _loading.show(); _leastShowTime = System.currentTimeMillis() }
    private val _hideRunnable = Runnable { _loading.dismiss() }
    private var _leastShowTime = 0L
    private fun _initDialog() = Dialog(this).also { // 单文件功能
        val dp5 = dpToPx(5F).toInt()
        it.requestWindowFeature(Window.FEATURE_NO_TITLE)
        it.setCanceledOnTouchOutside(false)
        it.window?.setBackgroundDrawable(GradientDrawable().also {
            it.shape = GradientDrawable.RECTANGLE
            it.cornerRadius = dpToPx(5F)
            it.setColor((0xcc000000).toInt())
        })
        val root = LinearLayout(this).also {
            it.minimumWidth = dpToPx(70F).toInt()
            it.minimumHeight = dpToPx(70F).toInt()
            it.orientation = LinearLayout.VERTICAL
            it.layoutTransition = LayoutTransition()
            it.gravity = Gravity.CENTER
            it.setPadding(dpToPx(10F).toInt(), dp5, dpToPx(10F).toInt(), dp5)
        }
        root.addView(ProgressBar(root.context), dpToPx(40F).toInt(), dpToPx(40F).toInt())
        root.addView(TextView(root.context).also {
            it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            it.id = android.R.id.text1
            it.setTextColor((0xffe9e9e9).toInt())
            it.textSize = 13F
            it.gravity = Gravity.CENTER_HORIZONTAL
            it.text = "Loading..."
            it.visibility = View.GONE
        })
        it.setContentView(root)
    }
}

/**
 * fm基类
 *     原始FindViewById写法
 *     class HomeFragment: BaseFm<ViewBinding>(R.layout.fragment_home) {
 *          fun onViewCreated(view, bundle) {
 *              showLoading(..)
 *              hideLoading()
 *              ...
 *          }
 *          fun onLazyResume() {
 *              ...
 *          }
 *     }
 *     ViewBinding写法
 *     class HomeFragment: BaseFm<FragmentHomeBinding>() {
 *          fun onCreateVB(inflater, parent, attachToParent) {
 *              return FragmentHomeBinding.inflater(inflater, parent, attachToParent)
 *          }
 *          fun onViewCreated2() {
 *              vb.tvTitle....
 *              showLoading(..)
 *              hideLoading()
 *              ...
 *          }
 *          fun onLazyResume() {
 *              ...
 *          }
 *     }
 */
open class BaseFm<VB: ViewBinding> : Fragment, BaseFmInterface<VB> {

    constructor()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)
    override val _binding: VB? by lazy { onCreateVB(_inf!!, _cont, false) }

    override val ac: FragmentActivity? by lazy { activity }

    private var _inf: LayoutInflater? = null
    private var _cont: ViewGroup? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _inf = inflater; _cont = container
        if (_binding != null) { return vb.root }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated2()
    }

    override fun onResume() {
        super.onResume()
        if (_lazy) { _lazy=false; onLazyResume() }
    }
    protected var _lazy = true

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }
}
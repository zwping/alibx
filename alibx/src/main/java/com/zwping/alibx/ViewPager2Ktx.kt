package com.zwping.alibx

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView
import java.lang.ref.WeakReference

/**
 *
 * zwping @ 2021/11/2
 */
interface ViewPager2Interface {

    /**
     * 针对ImageView的便捷调用, 后续接口返回数据调用[setBannerData]
     * @param bindView 数据绑定至view
     * @param autoStart 非空传入则自动开始轮播
     * @param opt ViewPager2相关配置 & 回调
     */
    fun <T> ViewPager2.initBanner(
                bindView: (shapeableImageView: ShapeableImageView, data: MutableList<T>, position: Int)->Unit,
                autoStart: LifecycleOwner?=null,
                opt: ViewPager2Opt<T>.()->Unit={}):
            BannerAdapter<T>

    /**
     * ViewPager2标准实现banner, 后续接口返回数据调用[setBannerData]
     * @param createHolderView 自定义HolderView
     * @param bindView 数据绑定至view
     * @param autoStart 非空传入则自动开始轮播
     * @param opt ViewPager2相关配置 & 回调
     */
    fun <T> ViewPager2.initBanner(
                createHolderView: (parent: ViewGroup)->View,
                bindView: (view: View, data: MutableList<T>, position: Int)->Unit,
                autoStart: LifecycleOwner?=null,
                opt: ViewPager2Opt<T>.()->Unit={}):
            BannerAdapter<T>

    /**
     * 设置banner数据 setCurrentItem(2, false)
     */
    fun <T> ViewPager2?.setBannerData(data: MutableList<T>?)
    fun <T> ViewPager2?.getBannerAdp(): BannerAdapter<T>?

    /**
     * 设置banner一页多屏
     * @param itemMargin item之间的间隔
     * @param vp2Padding viewPager2内RecyclerView内部排挤值
     */
    fun ViewPager2?.setBannerMultiItem(itemMargin: Int, vp2Padding: Int)
}
interface ViewPager2OptInterface<T> {
    /**
     * 关联指示器, 提供一类通用指示器[BannerIndicator]
     */
    val indicator: BannerIndicator?
    /*** 声明周期感知 ***/
    var owner: LifecycleOwner?
    /*** 轮播时间间隔 ***/
    var autoLoopInterval: Long
    /*** 是否自动播放 ***/
    var autoStart: Boolean
    /*** 轮播管理器, owner失效时可外部管理[BannerAutoLoop.pause] ***/
    var autoLoopManage: BannerAutoLoop?

    var onPageScrolled: (position: Int, Offset: Float, OffsetPixels: Int) -> Unit
    var onPageSelected: (position: Int, count: Int, data: T?)->Unit
    var onPageScrollStateChanged: (state: Int)->Unit
}
class ViewPager2Opt<T>: ViewPager2OptInterface<T> {
    override val indicator: BannerIndicator? = null
    override var owner: LifecycleOwner? = null
        set(value) { if (value != null) field = value }
    override var autoLoopInterval: Long = 3000L
    override var autoStart: Boolean = false // 建议传入owner自动播放
    override var autoLoopManage: BannerAutoLoop? = null

    override var onPageScrolled: (position: Int, Offset: Float, OffsetPixels: Int) -> Unit = {_,_,_->}
    override var onPageSelected: (position: Int, count: Int, data: T?) -> Unit = {_,_,_->}
    override var onPageScrollStateChanged: (state: Int)->Unit = {}
}
object ViewPager2: ViewPager2Interface {

    override fun <T> ViewPager2.initBanner(
                                bindView: (shapeableImageView: ShapeableImageView, data: MutableList<T>, position: Int)->Unit,
                                autoStart: LifecycleOwner?,
                                opt: ViewPager2Opt<T>.()->Unit):
            BannerAdapter<T> {
        val imageView = { parent: ViewGroup -> ShapeableImageView(parent.context).also {
            it.layoutParams = parent.layoutParams; it.scaleType=ImageView.ScaleType.CENTER_CROP
            // it.shapeAppearanceModel = ShapeAppearanceModel.Builder().setAllCornerSizes(50F).build() // 圆角
            // it.strokeWidth = 5F; it.strokeColor = ColorStateList.valueOf((0xffff0000).toInt()) // 边框
        } }
        return initBanner(imageView,
                          { view, data, position -> bindView(view as ShapeableImageView, data, position) },
                            autoStart, opt)
    }

    override fun <T> ViewPager2.initBanner(createHolderView: (parent: ViewGroup)->View,
                                  bindView: (view: View, data: MutableList<T>, position: Int)->Unit,
                                           autoStart: LifecycleOwner?,
                                           opt: ViewPager2Opt<T>.()->Unit) :
            BannerAdapter<T> {
        val option = ViewPager2Opt<T>()
        autoStart?.also { option.owner=it; option.autoStart=true } // 方法直接传入则自动轮播
        val autoLoop = BannerAutoLoop(WeakReference(this), option.autoLoopInterval, option.owner)
        val touchOccupy = { occupy: Boolean -> if (occupy) autoLoop.pause() else autoLoop.start() }
        val holder = { parent: ViewGroup -> BannerHolder(createHolderView(parent), touchOccupy) }
        val adp = BannerAdapter(holder, bindView)
        adapter = adp
        offscreenPageLimit = 3
        var selectedLock = -1 // 防止setCurrentItem多次触发
        registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                option.onPageScrolled(position, positionOffset, positionOffsetPixels)
                option.indicator?.onPageScrolled(position, positionOffset, currentItem, adp.realPosition(currentItem), adp.dataSize)
            }
            override fun onPageSelected(position: Int) {
                val realPosition = adp.realPosition(position)
                if (selectedLock == realPosition) return
                selectedLock = realPosition
                val size = adp.dataSize
                option.onPageSelected(realPosition, size, adp.data?.get(realPosition))
                option.indicator?.onPageSelected(realPosition, size)
            }
            override fun onPageScrollStateChanged(state: Int) {
                when(state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> { // 滑动衔接
                        when(currentItem) { // 左右各加2个预留位
                            0 -> setCurrentItem(3, false)
                            1 -> setCurrentItem(4, false)
                            adp.dataSize+4-2 -> setCurrentItem(2, false)
                            adp.dataSize+4-1 -> setCurrentItem(3, false)
                        }
                    }
                    ViewPager2.SCROLL_STATE_IDLE -> { // 定时器衔接
                        if (currentItem == adp.dataSize+2) setCurrentItem(2, false)
                    }
                }
                option.onPageScrollStateChanged(state)
            }
        })
        return adp
    }

    override fun <T> ViewPager2?.setBannerData(data: MutableList<T>?) {
        this ?: return
        getBannerAdp<T>()?.data = data
        if (data != null) setCurrentItem(2, false) // data+4 兼容multiView一屏多Item视觉效果
    }

    override fun <T> ViewPager2?.getBannerAdp(): BannerAdapter<T>? {
        this ?: return null
        if (adapter is BannerAdapter<*>) return adapter as BannerAdapter<T>
        return null
    }

    override fun ViewPager2?.setBannerMultiItem(itemMargin: Int, vp2Padding: Int) {
        this ?: return
        setPageTransformer(MarginPageTransformer(itemMargin))
        with(getChildAt(0) as RecyclerView) {
            setPadding(vp2Padding, 0, vp2Padding, 0)
            clipToPadding = false
        }
    }
}

/**
 * BannerHolderView
 * @param touchStateLis true->触摸了 false->触摸释放
 */
@SuppressLint("ClickableViewAccessibility")
class BannerHolder(val view: View, touchStateLis: (Boolean)->Unit): RecyclerView.ViewHolder(view) {
    init {
        view.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> touchStateLis(true)
                MotionEvent.ACTION_OUTSIDE,
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> touchStateLis(false)
            }
            false
        }
    }
}
class BannerAdapter<T>(private val createHolder: (parent: ViewGroup)->BannerHolder,
                       private val bindView: (view: View, data: MutableList<T>, position: Int)->Unit):
    RecyclerView.Adapter<BannerHolder>() {

    var data: MutableList<T>? = null
        set(value) { field = value; notifyDataSetChanged() }
    var dataSize: Int = 0
        get() = if (data == null) 0 else data!!.size
        private set
    var realPosition = { position: Int ->
        if (data.isNullOrEmpty() || data!!.size == 1) 0
        else {
            val size = data!!.size
            when (position) {
                0 -> size-2
                1 -> size-1
                size+4-2 -> 0
                size+4-1 -> 1
                else -> position-2
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerHolder {
        return createHolder(parent)
    }
    override fun onBindViewHolder(holder: BannerHolder, position: Int) {
        if (data.isNullOrEmpty()) return
        bindView(holder.view, data!!, realPosition(position))
    }
    override fun getItemCount() = if (data.isNullOrEmpty()) 0 else data!!.size+4
}

class BannerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var w = 20
    private var h = 20
    private var interval = 10
    private var norBg: Drawable? = GradientDrawable().also {
        it.cornerRadii = (h/2F).let { floatArrayOf(it,it, it,it, it,it, it,it) }
        it.setColor(Color.DKGRAY) }
    private var selBg: Drawable? = GradientDrawable().also {
        it.cornerRadii = (h/2F).let { floatArrayOf(it,it, it,it, it,it, it,it) }
        it.setColor(Color.RED) }

    private var total: Int = -1

    private val norLayout by lazy { LinearLayout(context).also { it.orientation = LinearLayout.HORIZONTAL } }
    private val selPoint by lazy { View(context).also { it.layoutParams = FrameLayout.LayoutParams(w, h); it.background=selBg } }

    init {
        addView(norLayout)
        addView(selPoint)
    }


    fun initStyle(pointWPx: Int, pointHPx: Int, interval: Int,
                  norBg: Drawable = GradientDrawable().also {
                    it.cornerRadii = (pointHPx/2F).let { floatArrayOf(it,it, it,it, it,it, it,it) }
                    it.setColor(Color.DKGRAY) },
                  selBg: Drawable = GradientDrawable().also {
                      it.cornerRadii = (pointHPx/2F).let { floatArrayOf(it,it, it,it, it,it, it,it) }
                      it.setColor(Color.RED)
                  }) {
        this.w = pointWPx; this.h = pointHPx; this.interval = interval
        this.norBg = norBg; this.selBg = norBg
        updateLayoutParams {  width=w*total+interval*(total-1) }
        repeat(norLayout.childCount) { i ->
            norLayout.getChildAt(i).also {
                it.updateLayoutParams<MarginLayoutParams> {
                    width = w; height = h; if (i!=0) leftMargin = interval
                }
                it.background = norBg
            }
        }
        selPoint.updateLayoutParams { width = w; height = h }
        selPoint.background = selBg
    }


    private var lastPosition = -1
    fun onPageSelected(realPosition: Int, dataSize: Int) {
        if(lastPosition == realPosition) return
        if (dataSize != total) { total=dataSize; updateLayoutParams {  width=w*total+interval*(total-1) }; addBg() }
        selPoint.updateLayoutParams<MarginLayoutParams> { leftMargin = (w+interval)*realPosition }
        selPoint.animate().cancel()
        if (realPosition == 0 && (lastPosition==dataSize-1)) { // 首尾衔接动画
            selPoint.translationX = -w.toFloat()
            selPoint.animate().translationX(0F).start()
        }
        if (realPosition==dataSize-1 && lastPosition==0) { // 首尾衔接动画
            selPoint.translationX = w.toFloat()
            selPoint.animate().translationX(0F).start()
        }
        lastPosition = realPosition
    }
    fun onPageScrolled(position: Int, positionOffset: Float, current: Int, realPosition: Int, dataSize: Int) {
        val left = position!=current
        // if ((left && realPosition==0) || (!left && realPosition==dataSize-1)) return
        val width = w+interval
        val leftMargin = (w+interval)*realPosition
        val offset = if (left) 1-positionOffset else positionOffset
        selPoint.updateLayoutParams<MarginLayoutParams> {
            this.leftMargin = (offset*width).toInt().let { if(left) leftMargin-it else leftMargin+it }
        }
    }

    private fun addBg() {
        norLayout.removeAllViews()
        repeat(total) { i ->
            norLayout.addView(View(context).also {
                it.layoutParams = LinearLayout.LayoutParams(w, h).also { if (i != 0) it.leftMargin = interval }
                it.background = norBg
            })
        }
    }
}

interface BannerAutoLoopInterface {
    var vp2: WeakReference<ViewPager2>?
    var intervalTime: Long
    var autoStart: Boolean

    fun start()
    fun pause()
    fun destroy()
}
class BannerAutoLoop(
    override var vp2: WeakReference<ViewPager2>?,
    override var intervalTime: Long,
    private val owner: LifecycleOwner?=null,
    override var autoStart: Boolean=false):
    BannerAutoLoopInterface, LifecycleEventObserver{

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val task by lazy { Runnable { nextPage() } }
    private fun nextPage() {
        vp2?.get()?.also { it.currentItem = it.currentItem+1 }
        handler.postDelayed(task, intervalTime)
    }

    init {
        owner?.lifecycle?.addObserver(this)
        if(autoStart) start()
    }

    override fun start() { pause(); handler.postDelayed(task, intervalTime) }
    override fun pause() { handler.removeCallbacks(task) }
    override fun destroy() { pause(); owner?.lifecycle?.removeObserver(this) }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event) {
            Lifecycle.Event.ON_RESUME -> start()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> destroy()
        }
    }

}
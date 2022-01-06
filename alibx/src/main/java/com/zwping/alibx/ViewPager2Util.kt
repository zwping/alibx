package com.zwping.alibx

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.Px
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView

/**
 * viewPager2
 * zwping @ 2021/11/2
 */
object ViewPager2Util {

    /**
     * 设置banner一页多屏
     * @param itemMargin item之间的间隔
     * @param vp2Padding viewPager2内RecyclerView内部排挤值
     */
    fun setBannerMultiItem(viewPager2: ViewPager2?, @Px itemMargin: Int, @Px vp2Padding: Int) {
        viewPager2 ?: return
        // PageTransformer与offscreenPageLimit不协调, 当使用PageTransformer时不建议设置offscreenPageLimit
        // 如下, adapter.notifyDataChange时page会跳动
        viewPager2.setPageTransformer(MarginPageTransformer(itemMargin))
        with(viewPager2.getChildAt(0) as RecyclerView) {
            setPadding(vp2Padding, 0, vp2Padding, 0)
            clipToPadding = false
        }
    }

}


/**
 * 基于ViewPager2[BannerAdapter]实现banner功能
 * @bug todo recyclerview 自动轮播 抢占焦点
 */
class Banner<T> @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null):
    FrameLayout(context, attrs), LifecycleEventObserver {

    var adapter: BannerAdapter<T, out RecyclerView.ViewHolder>?
        set(value) { viewPager2.adapter = value }
        get() = viewPager2.adapter as? BannerAdapter<T, out RecyclerView.ViewHolder>
    // 不复写ViewPager2, 选择通过扩展Adapter实现Banner功能
    val viewPager2 by lazy { ViewPager2(context).apply { layoutParams = LayoutParams(-1, -1) } }
    // 循环滚动(扩充4个Item)
    var hasLoop = true
        set(value) { field=value; adapter?.hasLoop=value }
    // 自动轮播
    var autoLoop = false
        private set
    // 自动管理轮播播放状态
    var owner: LifecycleOwner? = null
        private set(value) {
            field = value
            field?.lifecycle?.also { it.removeObserver(this); it.addObserver(this) }
        }
    // 轮播间隔
    var loopInterval = 3000L
    // 轮播速度
    var scrollTime = 600
    // 默认指示器
    var defIndicator: BannerIndicator? = null

    // banner滑动完成事件
    var onPageSelected: ((realPosition: Int, data: T)->Unit)? = null

    private var lockSelected = -1
    private val callback = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            adapter?.also { adp ->
                val index = adp.realPosition(position)
                if (lockSelected == index) return
                lockSelected = index
                defIndicator?.onPageSelected(position, adp.datas.size)
                onPageSelected?.invoke(index, adp.datas[index])
            }
        }
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            adapter?.also { adp ->
                val curItem = viewPager2.currentItem
                defIndicator?.onPageScrolled(position, positionOffset, curItem, adp.realPosition(curItem), adp.datas.size)
            }
        }
        override fun onPageScrollStateChanged(state: Int) {
            if (!hasLoop || adapter == null) return
            when(state) {
                ViewPager2.SCROLL_STATE_DRAGGING -> {       // 滑动中衔接
                    loopCompensation()
                }
                ViewPager2.SCROLL_STATE_SETTLING -> { }     // 手指滑动完成, 进入惯性滑动
                ViewPager2.SCROLL_STATE_IDLE -> {           // 滑动闲置衔接
                    loopCompensation()
                }
            }
        }
    }

    private fun loopCompensation() {
        val size = adapter?.datas?.size ?: 0
        when(viewPager2.currentItem) {
            0 -> viewPager2.setCurrentItem(size, false)
            1 -> viewPager2.setCurrentItem(size+1, false)
            size+2 -> viewPager2.setCurrentItem(2, false)
            size+3 -> viewPager2.setCurrentItem(3, false)
        }
    }

    init {
        ScrollSpeedManager.reflectLayoutManager(this)
        addView(viewPager2)
        viewPager2.unregisterOnPageChangeCallback(callback)
        viewPager2.registerOnPageChangeCallback(callback)
    }

    /**
     * @param bindView 记得类型强转
     * @bug1 glide将图片设置了transform转换[圆角], 首尾衔接会有闪动。@Fix 使用ShapeableImageView设置IV圆角
     */
    fun setAdapterImage(bindView: (iv: ShapeableImageView, entity: T)->Unit,
                        owner: LifecycleOwner?=null,
                        useIndicator: Boolean=true,
                        hasLoop: Boolean=true): Banner<T> {
        class Holder(parent: ViewGroup): RecyclerView.ViewHolder(ShapeableImageView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(-1, -1)
        })
        setAdapter(object: BannerAdapter<T, RecyclerView.ViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(parent)
            override fun onBindViewHolder2(holder: RecyclerView.ViewHolder, position: Int) {
                bindView(holder.itemView as ShapeableImageView, datas[position])
            }
        }, owner, useIndicator, hasLoop)
        return this
    }

    @JvmOverloads
    fun setAdapter(adapter: BannerAdapter<T, out RecyclerView.ViewHolder>,
                   owner: LifecycleOwner?=null,
                   useIndicator: Boolean=true,
                   hasLoop: Boolean=true): Banner<T> {
        this.adapter = adapter
        owner?.also { setAutoLoop(it) }
        if (useIndicator) setDefIndicator()
        setHasLoop(hasLoop)
        return this
    }
    fun setDatas(datas: MutableList<T>?) {
        adapter?.datas = datas ?: mutableListOf()
        adapter?.notifyDataSetChanged()
        viewPager2.setCurrentItem(if (hasLoop) 2 else 0, false)
        defIndicator?.invalidate(datas?.size ?: -1)
        start()
    }
    /**
     * 开始自动轮播
     * @param owner 轮播状态自动跟随
     */
    fun setAutoLoop(owner: LifecycleOwner?=null): Banner<T>{
        autoLoop = true; this.owner = owner; return this
    }
    /**
     * 使用默认指示器
     * layoutParams可以自定义
     */
    fun setDefIndicator(): Banner<T>{
        if (defIndicator == null) {
            defIndicator = BannerIndicator(context)
            addView(defIndicator)
        }
        defIndicator?.layoutParams = FrameLayout.LayoutParams(-2, -2).also {
            it.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            it.bottomMargin = (Resources.getSystem().displayMetrics.density * 10 + 0.5F).toInt()
        }
        return this
    }
    /**
     * 是否可以循环滚动, 循环滚动默认位置2
     */
    fun setHasLoop(hasLoop: Boolean): Banner<T> {
        this.hasLoop = hasLoop; return this
    }


    // --------- loop manager ---------

    // 开始轮播
    fun start() { pause(); if (autoLoop) postDelayed(task, loopInterval) }
    fun pause() { removeCallbacks(task) }
    fun destroy() { pause(); owner?.lifecycle?.removeObserver(this) }

    private val task: Runnable by lazy { Runnable {
        viewPager2.also { it.currentItem = it.currentItem+1 }
        postDelayed(task, loopInterval)
    } }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event) {
            Lifecycle.Event.ON_RESUME -> start()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> destroy()
        }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pause()
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> pause()
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> start()
        }
        return super.dispatchTouchEvent(ev)
    }
}

class ScrollSpeedManager(val banner: Banner<*>, lm: LinearLayoutManager) :
    LinearLayoutManager(banner.context, lm.orientation, false) {
    // from youth banner

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val ctx = recyclerView?.context
        if (ctx == null) {
            super.smoothScrollToPosition(recyclerView, state, position); return
        }
        val linearSmoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(ctx) {
                override fun calculateTimeForDeceleration(dx: Int) = banner.scrollTime
            }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    companion object {
        fun reflectLayoutManager(banner: Banner<*>) {
            try {
                val viewPager2 = banner.viewPager2
                val recyclerView = viewPager2.getChildAt(0) as RecyclerView
                recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                val speedManger = ScrollSpeedManager(banner, recyclerView.layoutManager as LinearLayoutManager)
                recyclerView.layoutManager = speedManger

                val LayoutMangerField = ViewPager2::class.java.getDeclaredField("mLayoutManager")
                LayoutMangerField.isAccessible = true
                LayoutMangerField[viewPager2] = speedManger

                val pageTransformerAdapterField = ViewPager2::class.java.getDeclaredField("mPageTransformerAdapter")
                pageTransformerAdapterField.isAccessible = true
                val mPageTransformerAdapter = pageTransformerAdapterField[viewPager2]
                if (mPageTransformerAdapter != null) {
                    val aClass: Class<*> = mPageTransformerAdapter.javaClass
                    val layoutManager = aClass.getDeclaredField("mLayoutManager")
                    layoutManager.isAccessible = true
                    layoutManager[mPageTransformerAdapter] = speedManger
                }
                val scrollEventAdapterField = ViewPager2::class.java.getDeclaredField("mScrollEventAdapter")
                scrollEventAdapterField.isAccessible = true
                val mScrollEventAdapter = scrollEventAdapterField[viewPager2]
                if (mScrollEventAdapter != null) {
                    val aClass: Class<*> = mScrollEventAdapter.javaClass
                    val layoutManager = aClass.getDeclaredField("mLayoutManager")
                    layoutManager.isAccessible = true
                    layoutManager[mScrollEventAdapter] = speedManger
                }
            } catch (e: Exception) { }
        }
    }

}

abstract class BannerAdapter<T, VH: RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    // 循环滚动(扩充4个Item)
    var hasLoop = true
    var datas: MutableList<T> = mutableListOf()
    var realPosition = { position: Int ->
        val size = this.datas.size
        when {
            !hasLoop -> position
            size == 1 -> 0
            position == 0 -> size-2
            position == 1 -> size-1
            position == size+2 -> 0
            position == size+3 -> 1
            else -> position-2
        }
    }

    abstract fun onBindViewHolder2(holder: VH, position: Int)

    final override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder2(holder, realPosition(position))
    }
    override fun getItemCount(): Int = datas.size + (if (hasLoop) 4 else 0)

}

class BannerIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var w = 20
    private var h = 20
    private var interval = 10
    private var norBg: Drawable? = GradientDrawable().also {
        it.cornerRadii = (h/2F).let { floatArrayOf(it,it, it,it, it,it, it,it) }
        it.setColor((0xCC444444).toInt()) }
    private var selBg: Drawable? = GradientDrawable().also {
        it.cornerRadii = (h/2F).let { floatArrayOf(it,it, it,it, it,it, it,it) }
        it.setColor((0xCCFF0000).toInt()) }

    private var total: Int = -1

    private val norLayout by lazy { LinearLayout(context).also { it.orientation = LinearLayout.HORIZONTAL } }
    private val selPoint by lazy { View(context).also { it.layoutParams = FrameLayout.LayoutParams(w, h); it.background=selBg } }

    init {
        addView(norLayout)
        addView(selPoint)
    }

    fun invalidate(dataSize: Int) {
        if (total == dataSize) return
        total=dataSize; updateLayoutParams { width=w*total+interval*(total-1) }; addBg()
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
        invalidate(dataSize)
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

/* ---------KTX----------- */

/**
 * 设置banner一页多屏
 * @param itemMargin item之间的间隔 -> 10dp
 * @param vp2Padding viewPager2内RecyclerView内部排挤值 -> 20dp
 */
fun ViewPager2?.setBannerMultiItem(itemMargin: Int, vp2Padding: Int) {
    ViewPager2Util.setBannerMultiItem(this, itemMargin, vp2Padding)
}

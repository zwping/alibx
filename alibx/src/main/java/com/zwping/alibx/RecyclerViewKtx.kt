package com.zwping.alibx

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding的成熟推动了原生Adapter实用
 * zwping @ 5/10/21
 * @lastTime: 2021年11月08日11:00:43
 */
abstract class BaseAdapter<E> : RecyclerView.Adapter<BaseViewHolder<E, View>>() {

    var datas = mutableListOf<E>()
        private set(value) {
            field = value
            pag.curPage = 1
            pag.curTotal = datas.size
            datasStateCallback?.refreshState(true)
            datasStateCallback?.noMoreData(pag.hasEnd())  // 默认协议每次返回数量为指定的PageSize(总数量%每页数量!=0 == 咩有更多数据)
        }
    var pag = Pagination()  // pageSize默认20
        private set

    var datasStateCallback: DatasStataCallback? = null   // 控制RefreshLayout状态, util配置可兼容所有刷新控件
    var diffCallback : DiffCallback<E>? = null           // 初始化 才可Diff渲染数据

    fun setDataOrNull(data: MutableList<E>?, detectMoves: Boolean = true) { setData(data ?: mutableListOf(), detectMoves) }
    fun setData(data: MutableList<E>, detectMoves: Boolean = true) {
        if (null == diffCallback) {
            datas = data; notifyDataSetChanged()
            return
        }
        _od.clear(); _od.addAll(datas); _nd.clear(); _nd.addAll(data)
        DiffUtil.calculateDiff(_diffCallBack, detectMoves).dispatchUpdatesTo(this)
        datas = data
    }
    fun addDataOrNull(data: MutableList<E>?) { if (!data.isNullOrEmpty()) addData(data, false) }
    fun addDataOrNull(data: MutableList<E>?, refreshLastPosition: Boolean=false) { if (!data.isNullOrEmpty()) addData(data, refreshLastPosition) }
    fun addData(data: MutableList<E>) { addData(data, false) }
    fun addData(data: MutableList<E>, refreshLastPosition: Boolean=false) {
        datas.addAll(data);
        notifyItemRangeChanged(datas.size-data.size-(if (refreshLastPosition) 1 else 0), datas.size) // -1=更新lastPositionUI
        pag.curPage += 1
        pag.curTotal = datas.size
        datasStateCallback?.noMoreData(pag.hasEnd())
        datasStateCallback?.loadMoreState(true)
    }
    fun setDataErr(hasRefresh: Boolean) {
        datasStateCallback?.apply {
            if (hasRefresh) {
                refreshState(false)
                if (datas.size == 0) noMoreData(true)
            } else loadMoreState(false)
        }
    }

    // override fun getItemViewType(position: Int): Int { } // 多布局实现
    override fun onBindViewHolder(holder: BaseViewHolder<E, View>, position: Int) { holder.bind(datas, position) }

    override fun getItemCount(): Int = datas.size

    private val _od by lazy { mutableListOf<E>() }
    private val _nd by lazy { mutableListOf<E>() }
    private val _diffCallBack by lazy {
        object: DiffUtil.Callback(){
            override fun getOldListSize(): Int = _od.size
            override fun getNewListSize(): Int = _nd.size

            override fun areItemsTheSame(op: Int, np: Int): Boolean = diffCallback?.areItemsTheSame(_od[op], _nd[np]) ?: false
            override fun areContentsTheSame(op: Int, np: Int): Boolean = diffCallback?.areContentsTheSame(_od[op], _nd[np]) ?: false
            override fun getChangePayload(op: Int, np: Int): Any? = diffCallback?.getChangePayload(_od[op], _nd[np])
        }
    }
}
/*** 一行代码代码实现Adapter ***/
open class BaseAdapterQuick<E>(val createViewHolder: (ViewGroup) -> BaseViewHolder<E, View>): BaseAdapter<E>(){
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<E, View> {
        return createViewHolder(parent)
    }
}
interface ItemViewType { var itemViewType: Enum<*> }  // Support RecyclerView ItemViewType
/*** 借助enum类特征实现多布局 ***/
abstract class BaseAdapterMulti<ENUM: Enum<*>>(private val enums: Array<ENUM>): BaseAdapter<ItemViewType>() {
    final override fun getItemViewType(position: Int): Int {
        return datas[position].itemViewType.ordinal
    }
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ItemViewType, View> {
        return onCreateViewHolder(parent, enums[viewType])  // 穷举ItemViewType
    }
    abstract fun onCreateViewHolder(parent: ViewGroup, enum: ENUM): BaseViewHolder<ItemViewType, View>
}

open class BaseViewHolder<E, out V: View>(
            val view: V,
            private val bindViewHolder: BaseViewHolder<E, V>.(view: V, entity: E) -> Unit):
        RecyclerView.ViewHolder(view) {
    var entity: E?=null
    private var _datas: MutableList<E>?=null
    fun bind(datas: MutableList<E>, position: Int) { _datas=datas; bind(datas[position]) }
    fun bind(entity: E) { this.entity=entity; bindViewHolder(this, view, entity) }

    fun isLastPosition() = (_datas?.size ?: 0)-1 == adapterPosition
}
/*** ViewHolder ViewBinding支持 ***/
open class BaseViewHolderVB<E, out VB: ViewBinding>(
            val vb: VB,
            private val bindViewHolder: BaseViewHolderVB<E, VB>.(vb: VB, entity: E) -> Unit) :
        BaseViewHolder<E, View>(vb.root, {_, entity -> bindViewHolder(this as BaseViewHolderVB<E, VB>, vb, entity) })

interface DiffCallback<E> {
    fun areItemsTheSame(od: E, nd: E): Boolean
    fun areContentsTheSame(od: E, nd: E): Boolean = false
    fun getChangePayload(od: E, nd: E): Any? = null
}
interface DatasStataCallback {
    fun refreshState(suc: Boolean)          // 刷新成功或失败
    fun loadMoreState(suc: Boolean)         // 加载更多数据成功或失败
    fun noMoreData(no: Boolean)             // 没有更多数据
}

fun ViewGroup.getLayoutInflater(): LayoutInflater { return LayoutInflater.from(context) }
fun ViewGroup.getLayoutInflater(@LayoutRes id: Int): View = getLayoutInflater().inflate(id, this, false)

inline fun RecyclerView.removeFocus() { isFocusableInTouchMode = false; requestFocus() }
inline fun RecyclerView.setLinearLayoutManager(
    @RecyclerView.Orientation ort: Int = RecyclerView.VERTICAL,
    noScrollV: Boolean = false,
    noScrollH: Boolean = false) {
    layoutManager = object : LinearLayoutManager2(context, ort, false) {
        override fun canScrollVertically(): Boolean {
            return if (noScrollV) false else super.canScrollVertically()
        }
        override fun canScrollHorizontally(): Boolean {
            return if (noScrollH) false else super.canScrollHorizontally()
        }
    }
}
inline fun RecyclerView.setGradLayoutManager(
    spanCount: Int,
    @RecyclerView.Orientation ort: Int = RecyclerView.VERTICAL,
    noScrollV: Boolean = false,
    noScrollH: Boolean = false) {
    layoutManager = object : GridLayoutManager2(context, spanCount, ort, false) {
        override fun canScrollVertically(): Boolean {
            return if (noScrollV) false else super.canScrollVertically()
        }
        override fun canScrollHorizontally(): Boolean {
            return if (noScrollH) false else super.canScrollHorizontally()
        }
    }
}

/**
 * recyclerView分割线 及 最后一个item底部偏移量
 */
inline fun RecyclerView.addItemDecorationLine(@ColorInt color: Int,
                                              dividerHeight: Int,
                                              dividerMargin: Int=0,
                                              dividerMarginLeft: Int?=null,
                                              dividerMarginRight: Int?=null,
                                              lastPositionOffset: Int=0,
                                              adp: BaseAdapter<*>?=null) {
    addItemDecoration(object: RecyclerView.ItemDecoration() {
        val paint = Paint().also { it.isAntiAlias = true; it.color = color }
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent.getChildAdapterPosition(view)
            if (index != 0) outRect.top = dividerHeight
            if (lastPositionOffset > 0 && index == (adp?.datas?.size ?: 0) - 1) outRect.bottom = lastPositionOffset
        }
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            repeat(parent.childCount) {
                val view = parent.getChildAt(it)
                val index = parent.getChildAdapterPosition(view)
                if (index == 0) return@repeat
                val t = view.top.toFloat() - dividerHeight
                val b = view.top.toFloat()
                val l = parent.paddingLeft.toFloat() + (dividerMarginLeft ?: dividerMargin)
                val r = parent.width.toFloat() - parent.paddingRight - (dividerMarginRight ?: dividerMargin)
                c.drawRect(l, t, r, b, paint)
            }
        }
    })
}

/*** 管理分页信息  */
open class Pagination {

    var curPage = 1
    var curTotal = 0

    var nextPage = -1
    var pageSize = 20
    var totalPage = -1
    var totalSize = -1

    fun nextPage(r: Boolean): Int = if (r) 1 else curPage + 1

    /*** 是否到达最后一页 ***/
    fun hasEnd(): Boolean
            = curTotal == 0 ||
            (curTotal > 0 &&
                    (
                            (nextPage > -1 && curPage >= nextPage) ||
                                    (totalPage > -1 && curPage >= totalPage) ||
                                    (totalSize > -1 && curTotal >= totalSize) ||
                                    ((-1 == nextPage && -1 == totalPage && -1 == totalSize) && curTotal % pageSize != 0)  // 未填充总页数或总数，则以每页约定数量PageSize取模判断是否最后一页
                            ))
}

// diffUtil中易出现 Fix Google Bug https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in/37050829
open class LinearLayoutManager2(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try { super.onLayoutChildren(recycler, state) }
        catch (e: Exception) { }
    }
}
open class GridLayoutManager2(context: Context?, spanCount: Int, orientation: Int, reverseLayout: Boolean) :
    GridLayoutManager(context, spanCount, orientation, reverseLayout) {
    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try { super.onLayoutChildren(recycler, state) }
        catch (e: Exception) { }
    }
}


// ========= 过时方法 ==========
/**
 * @deprecated [BaseViewHolderVB]
 */
@Deprecated("规范命名 BaseViewHolderVB")
open class BaseVH<E, out VB : ViewBinding>(
    val vb: VB,
    private val bindViewHolder: BaseVH<E, VB>.(vb: VB, entity: E) -> Unit) :
    BaseViewHolder<E, View>(vb.root, {_, entity -> bindViewHolder(this as BaseVH<E, VB>, vb, entity) })

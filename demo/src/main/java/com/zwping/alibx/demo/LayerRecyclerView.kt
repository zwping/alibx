package com.zwping.alibx.demo

import android.graphics.Color
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zwping.alibx.*

/**
 *
 * zwping @ 2022/4/15
 */
class LayerRecyclerView(val acMain: AcMain) {

    fun recyclerView() = RecyclerView(acMain).apply {
        layoutParams = ViewGroup.LayoutParams(-1, 100F.dp2px()); setBackgroundColor(acMain.randomColor())
        layoutManager = LinearLayoutManager(context)
        adapter = BaseAdapterQuick{ Holder(it) }.also {
            val data = mutableListOf<Int>()
            for(i in 0 until 10) { data.add(i) }
            it.setData(data)
        }
    }

    init {
        acMain.apply {
            vb.lyContainer.addView(recyclerView().also { it.addItemDecorationTop(10F) })
        }
    }

    private class Holder(parent: ViewGroup): BaseViewHolder<Int, TextView>(
        TextView(parent.context).apply { layoutParams = ViewGroup.LayoutParams(-1, 15F.dp2px()); setBackgroundColor((0x1a000000).toInt()) },
        { view, entity -> view.text = "$adapterPosition" }
    )

}
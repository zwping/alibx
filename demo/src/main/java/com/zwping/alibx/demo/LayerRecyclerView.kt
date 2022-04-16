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
        layoutParams = ViewGroup.LayoutParams(-1, 50F.dp2px()); setBackgroundColor(acMain.randomColor())
        layoutManager = LinearLayoutManager(context)
//        adapter = BaseAdapter
    }

    init {
        acMain.apply {
            vb.lyContainer.addView(recyclerView().also { it.addItemDecorationTop(10F) })
        }
    }

    private class Holder(parent: ViewGroup): BaseViewHolder<Int, TextView>(
        TextView(parent.context).apply { layoutParams = ViewGroup.LayoutParams(-1, 15F.dp2px()) },
        { view, entity -> view.text = "$adapterPosition" }
    )

}
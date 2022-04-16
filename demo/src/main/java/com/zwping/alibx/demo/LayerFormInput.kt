package com.zwping.alibx.demo

import android.graphics.Color
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zwping.alibx.BaseAdapterQuick
import com.zwping.alibx.SpanUtils

/**
 *
 * zwping @ 2022/4/15
 */
class LayerFormInput(val acMain: AcMain) {
    init {
        acMain.apply {
            // 表单输入良好的用户体验:
            // 外层滚动布局
            // window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            // isSingleLine = true; imeOptions = EditorInfo.IME_ACTION_NEXT // 连续输入
            val p1_1 = vb.lyContainer.parent.parent as ViewGroup
            (vb.lyContainer.parent as NestedScrollView).removeAllViews()
            p1_1.addView(vb.lyContainer)
            vb.lyContainer.addView(line("最外层布局不同"))
            val container = LinearLayout(vb.lyContainer.context).apply { setBackgroundColor(Color.DKGRAY);layoutParams = ViewGroup.LayoutParams(-1,-1); orientation = LinearLayout.VERTICAL }
            vb.lyContainer.addView(btn(SpanUtils().append("LinearLayout").setStrikethrough().create()) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                container.removeAllViews()
                repeat(20) { i-> container.addView(AcMain.HolderEt(container).let { it.view.hint="输入框${i}"; it.itemView }) }
            })
            vb.lyContainer.addView(btn("NestedScrollView") {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                container.removeAllViews()
                val sv = NestedScrollView(container.context)
                val cont =  LinearLayout(vb.lyContainer.context).apply { layoutParams = ViewGroup.LayoutParams(-1,-1); orientation = LinearLayout.VERTICAL }
                sv.addView(cont)
                repeat(20) { i-> cont.addView(AcMain.HolderEt(container).let { it.view.hint="输入框${i}"; it.itemView }) }
                container.addView(sv)
            })
            vb.lyContainer.addView(btn("RecyclerView") {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                container.removeAllViews()
                val rv = RecyclerView(container.context).apply { layoutManager = LinearLayoutManager(container.context) }
                rv.adapter = BaseAdapterQuick { AcMain.HolderEt(it) }.apply { setData(mutableListOf<Int>().apply { repeat(20) { add(it) } }) }
                container.addView(rv)
            })
            vb.lyContainer.addView(container)
        }
    }
}
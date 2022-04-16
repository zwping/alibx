package com.zwping.alibx.demo

import android.graphics.Color
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zwping.alibx.AdapterQuick
import com.zwping.alibx.SpanUtils
import com.zwping.alibx.dp2px
import com.zwping.alibx.glideColor

/**
 *
 * zwping @ 2022/4/15
 */
class LayerImageLoader(val acMain: AcMain) {

    private fun iv() = ImageView(acMain).apply { layoutParams = ViewGroup.LayoutParams(50F.dp2px(), 50F.dp2px()) }

    init {
        acMain.apply {
            vb.lyContainer.addView(line("展位图 glideColor(color, raidus)"))
            vb.lyContainer.addView(iv().also { it.glideColor() })
            vb.lyContainer.addView(iv().also { it.glideColor(Color.CYAN, 5F) })
        }
    }
}
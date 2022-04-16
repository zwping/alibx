package com.zwping.alibx.demo

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            vb.lyContainer.gravity = Gravity.CENTER_HORIZONTAL
            vb.lyContainer.addView(line("占位图背景 glideColor(color, raidus)"))
            vb.lyContainer.addView(iv().also { it.glideColor(randomColor(), 5F) })
        }
    }
}
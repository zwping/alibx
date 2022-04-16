package com.zwping.alibx.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.updateLayoutParams
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zwping.alibx.*
import com.zwping.alibx.R
import com.zwping.alibx.TabLayoutUtil.attach
import com.zwping.alibx.demo.databinding.LayerTabLayoutBinding

/**
 *
 * zwping @ 2022/4/15
 */
class LayerProgressBar(val acMain: AcMain) {
    init {
        acMain.init()
    }

    private fun AcMain.init(){
        vb.lyContainer.addView(line("横向进度条二阶色"))
        vb.lyContainer.addView(ProgressBar(this, null, android.R.style.Widget_Material_ProgressBar_Horizontal).also { progressBar ->
            progressBar.setColors(randomColor(), randomColor())
            progressBar.progress = 50
        })
        vb.lyContainer.addView(line("横向进度条三阶色"))
        vb.lyContainer.addView(ProgressBar(this, null, android.R.style.Widget_Material_ProgressBar_Horizontal).also { progressBar ->
            progressBar.setColors(randomColor(), randomColor(), randomColor())
            progressBar.progress = 50
            progressBar.secondaryProgress = 70
        })
    }
}
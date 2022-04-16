package com.zwping.alibx.demo

import android.graphics.Color
import androidx.core.view.updateLayoutParams
import com.zwping.alibx.*
import com.zwping.alibx.R
import com.zwping.alibx.demo.databinding.LayerTabLayoutBinding

/**
 *
 * zwping @ 2022/4/15
 */
class LayerTabLayout(val acMain: AcMain) {
    init {
        acMain.vb.lyContainer.post { acMain.init() }
    }

    private val vb1 by lazy { with(acMain.vb.lyContainer) { LayerTabLayoutBinding.inflate(getLayoutInflater(), this, false) } }
    private val tabs = listOf("实践", "职培", "消息", "我的")

    private fun AcMain.init(){
        vb.lyContainer.addView(vb1.root)

        vb1.tabLayoutMainBottom.initBottomNavigationView(tabs.size) { index ->
            initStyle(10F, (0xff999999).toInt(), Color.BLUE, 2F, 28F)
            titleTv.text = tabs[index]
            iconIv.setImageResource(R.drawable.customactivityoncrash_error_image)
//          iconIv.setImageDrawable(createStateListDrawable(this@MainActivity, tabicon[index]) { it.selected(tabicons[index]) })
            // setBadge()
        }
        vb1.tabLayoutMainBottom.getTabLayoutCustomView(2)?.showBadge(3)
        vb1.tabLayoutMainBottom.getTabLayoutCustomView(3)?.showBadge()
    }
}
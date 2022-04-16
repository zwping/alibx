package com.zwping.alibx.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
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
class LayerTabLayout(val acMain: AcMain) {
    init {
        acMain.vb.lyContainer.post { acMain.init() }
    }

    private val vb1 by lazy { with(acMain.vb.lyContainer) { LayerTabLayoutBinding.inflate(getLayoutInflater(), this, false) } }
    private val tabs = listOf("首页", "职培", "消息", "我的")

    private fun AcMain.init(){
        vb.lyContainer.addView(line("首页底部导航"))
        vb.lyContainer.addView(TabLayout(this).also { tabLayout ->
            tabLayout.layoutParams = ViewGroup.LayoutParams(-1, 44F.dp2px())
            tabLayout.initBottomNavigationView(tabs.size) { index ->
                initStyle(10F, (0xff999999).toInt(), Color.BLUE, 2F, 28F)

                titleTv.text = tabs[index]
                iconIv.glideColor(randomColor())
            }
            tabLayout.getTabLayoutCustomView(2)?.showBadge(3)
            tabLayout.getTabLayoutCustomView(3)?.showBadge()
        })
        vb.lyContainer.addView(line("TabLayout关联ViewPager2"))
        val viewPager2 = Banner<Int>(this).also { banner ->
            banner.layoutParams = ViewGroup.LayoutParams(-1, 50F.dp2px())
            banner.setAdapterImage({ iv, entity -> iv.glideColor(randomColor()) }, hasLoop = false)
            banner.setDatas(randomData(tabs.size))
        }
        vb.lyContainer.addView(viewPager2)
        vb.lyContainer.addView(TabLayout(this).also { tabLayout ->
            tabLayout.layoutParams = ViewGroup.LayoutParams(-1, 44F.dp2px())

//            logd(tabLayout.tabSelectedIndicator)

            tabLayout.attach(viewPager2.viewPager2, smoothScroll = false) { tab, position -> tab.setText(tabs[position]) }
        })
    }
}
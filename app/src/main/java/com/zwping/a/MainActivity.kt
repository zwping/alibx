package com.zwping.a

import android.view.LayoutInflater
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.a.databinding.TestBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.ViewPager2.initBannerOfVB
import com.zwping.alibx.ViewPager2.setBannerData
import com.zwping.alibx.ViewPager2.setBannerMultiItem
import com.zwping.alibx.dp2px

class MainActivity : BaseAc<ActivityMainBinding>() {

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    private var isLastPage = false
    private var isDargPage = false

    override fun initView() {
        vb.viewPager2.setBannerMultiItem(6F.dp2px(), 15F.dp2px())
        vb.viewPager2.initBannerOfVB<String, TestBinding>(
            {TestBinding.inflate(LayoutInflater.from(it.context), it, false)},
            {vb, data, position ->
                vb.tvTest.text = "${data[position]}"
            }, opt= {
                hasLoop = false
                onPageScrolled = {position, offset, offsetPixels ->
                    if (isLastPage && isDargPage && offsetPixels==0) {
                        println("=====")
                    }
                }
                onPageSelected = {position, count, data ->
                    isLastPage = position==count-1
                }
                onPageScrollStateChanged = {state ->
                    isDargPage = state==1
                }
            })

        vb.viewPager2.setBannerData(mutableListOf("11", "22", "33"), false)
    }

}
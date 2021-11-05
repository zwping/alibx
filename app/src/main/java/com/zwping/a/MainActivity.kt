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

    override fun initView() {
    }

}
package com.zwping.a

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.ViewPager2.initBanner

class MainActivity : BaseAc<ActivityMainBinding>() {

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        vb.viewPager2.initBanner<String>({shapeableImageView, data, position ->  },
            opt={
                onPageSelected = {position, count, data ->

                }
            })
    }

}
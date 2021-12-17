package com.zwping.a

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.ITimer
import com.zwping.alibx.showToast


/**
 *
 * zwping @ 2021/11/22
 */
class AcSecond: BaseAc<AcMainBinding>() {
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    val prefix by lazy { intent?.getStringExtra("prefix") }
    val item by lazy { intent?.getStringExtra("item") }

    override fun initView() {
        val us = mutableListOf<String>()

        item?.split(",")?.forEach { us.add("$prefix$it") }

        val u = "https://pic2.zhimg.com/80/v2-df7fd62e3fffcd9c14c9f82db55a9a3d_1440w.jpg"
        vb.layerFindTeacher.start(this, u, us)
        val htime = 1000
        ITimer({ vb.lyFindTeacher.visibility = View.GONE; vb.layerFindTeacher.stop() }, 5000L-htime).schedule(this)
    }
}
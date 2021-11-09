package com.zwping.a

import android.view.LayoutInflater
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.ViewKtx.hideLoading
import com.zwping.alibx.ViewKtx.showLoading


class MainActivity : BaseAc<ActivityMainBinding>() {

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }
    override fun initView() {
        vb.btn.setOnClickListener {
            vb.btn.showLoading()
            handler.postDelayed({ vb.btn.hideLoading() }, 2000)
        }
    }
}
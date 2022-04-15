package com.zwping.a

import android.view.*
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.*


class AcMain : BaseAc<AcMainBinding>() {
//
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }


    override fun initView() {
        vb.tv1.setOnClickListener {
            showLoading()
        }

    }
}

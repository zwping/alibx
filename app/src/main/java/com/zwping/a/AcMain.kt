package com.zwping.a

import android.view.LayoutInflater
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.BaseAc


class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    override fun initView() {
    }

}

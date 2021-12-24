package com.zwping.a

import android.view.LayoutInflater
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.BaseAc


/**
 *
 * zwping @ 2021/11/22
 */
class AcSecond: BaseAc<AcMainBinding>() {
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    override fun initView() {
    }


}
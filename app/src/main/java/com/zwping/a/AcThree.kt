package com.zwping.a

import android.graphics.Color
import android.view.LayoutInflater
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.LauncherMode
import com.zwping.alibx.open

/**
 *
 * zwping @ 2021/11/24
 */
class AcThree: BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(layoutInflater)
    }
    override fun initView() {
        vb?.lyRoot?.setBackgroundColor(Color.DKGRAY)

        vb?.lyRoot?.setOnClickListener {
            open(AcSecond::class.java) {
                launcherMode = LauncherMode.SingleTask
            }
        }
    }

}
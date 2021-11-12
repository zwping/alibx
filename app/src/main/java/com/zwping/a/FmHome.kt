package com.zwping.a

import android.view.LayoutInflater
import android.view.ViewGroup
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.BaseFm

/**
 *
 * zwping @ 2021/11/10
 */
class FmHome: BaseFm<Test1Binding>() {

    override fun initVB(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): Test1Binding? {
        return Test1Binding.inflate(inflater, parent, attachToParent)
    }

    override fun initView() {
        vb.tv.text = "-------="
        showLoading()
    }
}
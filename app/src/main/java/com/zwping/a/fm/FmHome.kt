package com.zwping.a.fm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.zwping.a.Test
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.BaseFm
import com.zwping.alibx.Bus
import kotlinx.coroutines.coroutineScope

/**
 *
 * zwping @ 2021/11/10
 */
class FmHome: BaseFm<Test1Binding>() {

    override fun initVB(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): Test1Binding? {
        return Test1Binding.inflate(inflater, parent, attachToParent)
    }

    override fun initView() {
        vb?.tv?.text = "-------="
//        showLoading()


        Bus.subscribe(this, "tag") { vb?.tv?.text = "$it" }

        Thread({
//            Bus.post("tag", "3333")
            vb?.tv?.text = "1111"
        }).start()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("destroy fm")
    }
}
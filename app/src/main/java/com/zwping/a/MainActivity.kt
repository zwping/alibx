package com.zwping.a

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.BaseAdapterQuick
import com.zwping.alibx.BaseVH
import com.zwping.alibx.getLayoutInflater

class MainActivity : BaseAc<ActivityMainBinding>() {

    private val adp by lazy {
        object: BaseAdapterQuick<String>({ BaseVH(Test1Binding.inflate(it.getLayoutInflater(), it, false),
            { vb, entity ->
                vb.tv.text = "${entity}"
                vb.v.visibility = if (isLastPosition()) View.GONE else View.VISIBLE
            }) }) {}
    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        vb.recyclerView.layoutManager = LinearLayoutManager(this)
        vb.recyclerView.adapter = adp

        adp.setData(mutableListOf("1", "2", "3"))
        handler.postDelayed({
            adp.addData(mutableListOf("1", "2", "3"))
        }, 2000)
    }

}
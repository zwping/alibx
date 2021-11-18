package com.zwping.a

import android.view.LayoutInflater
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.IJson
import com.zwping.alibx.ItemViewType
import org.json.JSONObject


class MainActivity : BaseAc<ActivityMainBinding>() {


    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }
    override fun initView() {
        val fmHome = FmHome()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fmHome).show(fmHome).commitAllowingStateLoss()

        println("${Test.Bean(JSONObject("{'type': 2, 'title': 'name'}"))} ==")
    }

}
package com.zwping.a

import android.view.LayoutInflater
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.IJson
import org.json.JSONObject


class MainActivity : BaseAc<ActivityMainBinding>() {

    class Bean(ob: JSONObject?): IJson(ob, true) {
        var title: String?=null
        var bean: Bean?=null
        var beans: MutableList<Bean>?=null
        override fun toString(): String {
            return "Bean(bean=$bean, beans=$beans, title=$title)"
        }

    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }
    override fun initView() {
        val fmHome = FmHome()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fmHome).show(fmHome).commitAllowingStateLoss()

        println("${Bean(JSONObject("{}"))} ==")
    }
}
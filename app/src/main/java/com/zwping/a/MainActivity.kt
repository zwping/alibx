package com.zwping.a

import android.view.LayoutInflater
import android.widget.Toast
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.IJson
import org.json.JSONObject

class MainActivity : BaseAc<ActivityMainBinding>() {

    companion object {
        class Bean(obj: JSONObject?) : IJson(obj, autoReflexParse = true) {

            var title: String? = null
//            private var title: Int? = null
//            private var title: Boolean? = null
//            private var title: Double? = null
//            private var item: Items? = null
            var items: MutableList<Int>? = null
            var itemss: MutableList<Items>? = null
            var item: Items? = null

            class Items(ob: JSONObject?):IJson(ob, autoReflexParse = true) {
                var content: String?=null
                override fun toString(): String {
                    return "items $content"
                }
            }

            init {
                // title = obj?.optString("title")

                println(_log)
            }

            override fun toString(): String {
                return "$items $title $itemss $item"
            }


        }
    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        val ob = JSONObject("{'item':{'content': 999};'title':11.99999999;'items':['123'];'itemss':[{'content':222}]}")
        val t = Bean(ob)
        println("--$t")
        Toast.makeText(this, "--$t", Toast.LENGTH_SHORT).show()
    }

}
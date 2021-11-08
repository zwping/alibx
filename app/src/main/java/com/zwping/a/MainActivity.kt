package com.zwping.a

import android.view.LayoutInflater
import android.widget.Toast
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.IJson
import org.json.JSONObject

class MainActivity : BaseAc<ActivityMainBinding>() {

    companion object {
        class Base(ob: JSONObject?): IJson(ob, autoReflexParse = true) {
            var resultcode: Int?=null
            var reason: String?=null
            var result: Result?=null
            override fun toString(): String {
                return "Base(resultcode=$resultcode, reason=$reason, result=$result)"
            }

        }
        class Result(ob: JSONObject?): IJson(ob, autoReflexParse = true){
            var province: String?=null
            var city: String?=null
            var areacode: String?=null
            var zip: String?=null
            var company: String?=null
            var card: String?=null
            override fun toString(): String {
                return "Result(province=$province, city=$city, areacode=$areacode, zip=$zip, company=$company, card=$card)"
            }

        }
    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        val s = """
            {
            "resultcode":"200",
            "reason":"Return Successd!",
            "result":{
                "province":"浙江",
                "city":"杭州",
                "areacode":"0571",
                "zip":"310000",
                "company":"中国移动",
                "card":""
            }
            }
        """.trimIndent()
        println(Base(JSONObject(s)))
    }

}
package com.zwping.alibx

import org.json.JSONArray
import org.json.JSONObject

/**
 * 标准化手动解析过程
 *  - 定制构造函数
 *  - 规避GSON无法解决的问题, 后台数据结构不稳定导致无法解析
 *
 * abstract class [IJson]
 *  class method [optJSONObjectOrNull] [optJSONArrayOrNull] [optJSONArrayBasic]
 * public method [optJSONObject2] [optJSONArray2]
 *
 * zwping @ 2020/9/25
 */
abstract class IJson(obj: JSONObject? = null) {

    /*** 手动解析对象 ***/
    fun <T : IJson> JSONObject.optJSONObjectOrNull(key: String, lis: (JSONObject) -> T): T? {
        optJSONObject(key)?.also { return lis.invoke(it) }
        return null
    }

    /*** 手动解析数组 ***/
    fun <T : IJson> JSONObject.optJSONArrayOrNull(key: String, lis: (JSONObject) -> T): MutableList<T>? {
        var data: MutableList<T>? = null
        optJSONArray(key)?.apply {
            data = mutableListOf()
            for (i in 0 until length()) {
                optJSONObject(i)?.also { data?.add(lis.invoke(it)) }
            }
        }
        return data
    }

    companion object {

        fun <BasicType> JSONObject.optJSONArrayBasic(key: String, lis: (Any?) -> BasicType?): MutableList<BasicType>? {
            var data: MutableList<BasicType>? = null
            optJSONArray(key)?.apply {
                data = mutableListOf()
                for (i in 0 until length()) { lis.invoke(get(i))?.also { data?.add(it) } }
            }
            return data
        }

        fun <BasicType> JSONArray.optJSONArrayBasic(lis: (Any?) -> BasicType?): MutableList<BasicType> {
            val data = mutableListOf<BasicType>()
            for(i in 0 until length()) { lis.invoke(get(i))?.also { data.add(it) } }
            return data
        }

        fun <T> JSONArray.optJSONArrayOrNull(lis: (JSONObject?) -> T?): MutableList<T> {
            val data = mutableListOf<T>()
            for(i in 0 until length()) { lis.invoke(optJSONObject(i))?.also { data.add(it) } }
            return data
        }

        fun JSONArray.forEach(lis: (ob: JSONObject?) -> Unit) { for (i in 0 until length()) { lis.invoke(optJSONObject(i)) } }
        fun JSONArray.forEachIndexed(lis: (index: Int, ob: JSONObject?) -> Unit) { for (i in 0 until length()) { lis.invoke(i, optJSONObject(i)) } }
    }

}
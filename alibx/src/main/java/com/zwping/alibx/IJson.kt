package com.zwping.alibx

import org.json.JSONArray
import org.json.JSONObject

/**
 * 原生json解析扩充
 *
 * zwping @ 2020/9/25
 * @lastTime 2021年11月01日17:18:22
 */
abstract class IJson(obj: JSONObject? = null) {

    companion object {

        @JvmStatic fun JSONArray.forEach(lis: (ob: JSONObject?) -> Unit) { for (i in 0 until length()) { lis.invoke(optJSONObject(i)) } }
        @JvmStatic fun JSONArray.forEachIndexed(lis: (index: Int, ob: JSONObject?) -> Unit) { for (i in 0 until length()) { lis.invoke(i, optJSONObject(i)) } }

        /*
        [
            '',
            ''
        ]
        array.optJSONArrayBasic { "$it" }
         */
        @JvmStatic fun <BasicType> JSONArray?.optJSONArrayBasic(lis: (Any) -> BasicType):
                MutableList<BasicType>? {
            this ?: return null
            val data = mutableListOf<BasicType>()
            for(i in 0 until length()) { get(i)?.also { data.add(lis(it)) } }
            return data
        }

        /*
        [
            {},
            {}
        ]
        array.optJSONArrayOrNull { Entity(it) }
         */
        @JvmStatic fun <T> JSONArray?.optJSONArrayOrNull(lis: (JSONObject) -> T):
                MutableList<T>? {
            this ?: return null
            val data = mutableListOf<T>()
            for(i in 0 until length()) { optJSONObject(i)?.also { data.add(lis(it)) } }
            return data
        }

    }
}
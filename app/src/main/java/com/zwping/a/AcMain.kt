package com.zwping.a

import android.content.Context
import android.view.*
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.*
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.inputmethod.InputMethodManager

import androidx.annotation.NonNull
import org.json.JSONArray
import org.json.JSONObject


class AcMain : BaseAc<AcMainBinding>() {
//
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    class Bean(ob: JSONObject?): IJson(ob, true) {
        var list1: JSONObject? = null
        var list2: JSONArray? = null
        override fun toString(): String {
            return "Bean(list1=$list1, list2=$list2) _log=$_log ${JSONObject::class.java}"
        }

    }

    override fun initView() {
        val str = """
            {
                'list1': {'a': 1},
                'list2': [1, 2, 3]
            }
        """.trimIndent()
        logd(Bean(JSONObject(str)))
    }

    override fun onResume() {
        super.onResume()
    }
}

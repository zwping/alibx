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


    override fun initView() {
        vb.webView.initClient({vc, cc ->  })
        vb.webView.loadUrl("https://www.qq.com")

    }

}

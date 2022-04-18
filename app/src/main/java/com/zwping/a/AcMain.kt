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
import org.json.JSONObject


class AcMain : BaseAc<AcMainBinding>() {
//
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }


    override fun initView() {
//        Bar.showKeyboard(vb.etContent)
//        ITimer({Bar.showKeyboard(vb.etContent)}, 400).schedule(this)
//        vb.etContent.post { Bar.showKeyboard(vb.etContent) }
        Requests.simpleLoggingInterceptor = { method, url, optional ->
            val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImp0aSI6IjEifQ.eyJpYXQiOjE2NTAyNTE2ODAsImp0aSI6IjEiLCJuYmYiOjE2NTAyNTE2ODAsImV4cCI6MTY1MDYxMTY4MCwidWlkIjoxfQ.YXtY7xSXf0Fa_UXXPYIwc7oWLnQaynQ79_7kjxLWyZk"
            optional.setHeader("Authorization", "Bearer $token")
            logd(method, url, optional)
        }
        Requests.get("http://app.shijianxingqiu.com/api/v1/cert/unreceived", hashMapOf("mobile" to "18870912166", "code" to "1111"))
            .enqueue2(this, { call, response, s -> logd(s) },
                { call, msg -> logd(msg)  })
    }

    override fun onResume() {
        super.onResume()
    }
}

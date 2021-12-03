package com.zwping.a

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.BaseAc
import com.zwping.alibx.Scheme.open
import com.zwping.alibx.dp2px


/**
 *
 * zwping @ 2021/11/22
 */
class AcSecond: BaseAc<AcMainBinding>() {
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }
    override fun initView() {
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("onnewintent")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun finish() {
        super.finish()
//        overridePendingTransition(OPTAnim.FadeOut)
    }
}
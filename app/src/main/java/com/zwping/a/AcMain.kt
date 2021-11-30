package com.zwping.a

import android.Manifest
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.fm.FmHome
import com.zwping.alibx.BaseAc
import com.zwping.alibx.ImageLoader.glide
import com.zwping.alibx.Scheme.open
import com.zwping.alibx.Util


class AcMain : BaseAc<AcMainBinding>() {


    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private val fmHome by lazy { FmHome() }
    override fun initView() {
    }


}
package com.zwping.a

import android.Manifest
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.a.fm.FmHome
import com.zwping.alibx.*
import com.zwping.alibx.ImageLoader.glide
import com.zwping.alibx.Scheme.open
import com.zwping.alibx.ViewPager2.initBanner
import com.zwping.alibx.ViewPager2.initBannerOfVB
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private val fmHome by lazy { FmHome() }
    override fun initView() {
        dataStore.put("title", true)
        Util.logd(dataStore.get("title"))

        vb?.viewPager2?.initBannerOfVB<String, Test1Binding>({Test1Binding.inflate(it.getLayoutInflater(), it, false)},
            {vb, data, position ->

            }, this)
    }


}
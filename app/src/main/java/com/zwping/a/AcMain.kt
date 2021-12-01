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
import com.zwping.alibx.ViewPager2.setBannerData
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

        vb?.viewPager2?.initBannerOfImg<String>({shapeableImageView, data, position ->
            shapeableImageView.glide(data[position])
        })
        val url1 = "http://img.daimg.com/uploads/allimg/211027/1-21102G22404.jpg"
        val url2 = "http://img.daimg.com/uploads/allimg/211020/1-211020143249.jpg"
        val url3 = "http://img.daimg.com/uploads/allimg/211026/1-2110261K108.jpg"
        vb?.viewPager2?.setBannerData(mutableListOf(url1, url2, url3))
    }


}
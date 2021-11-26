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


class AcMain : BaseAc<AcMainBinding>() {


    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private val fmHome by lazy { FmHome() }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        val url1 = "https://img.zcool.cn/community/01110d59813ae8a801215603213387.gif"
        vb?.iv?.glide(url1) {
            scaleType = ImageView.ScaleType.CENTER_CROP
            circleCrop()
            setStroke(3F, Color.RED)
        }
        vb?.lyRoot?.setOnClickListener {
            println(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA))
            println(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
            reqPermission.launch(Manifest.permission.CAMERA)
//            startAcScheme(AcSecond::class.java) {
//                extra { putString("url", url1) }
//                setSharedTransitionAnim(vb.iv)
//            }
            open(AcSecond::class.java)
        }
        Uri.parse("yikao://host/path?title=1233").also {
            println(it.scheme)
        }
    }

    private val reqPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        println("---$it")
    }

    override fun onDestroy() {
        super.onDestroy()
        // _loading.dismiss()
    }

}
package com.zwping.a

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.*
import com.zwping.alibx.ImageLoader.glide
import android.R
import android.content.Intent
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource

import com.bumptech.glide.load.engine.GlideException

import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zwping.alibx.Scheme.startAcScheme


/**
 *
 * zwping @ 2021/11/22
 */
class AcSecond: BaseAc<AcMainBinding>() {
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }
    override fun initView() {
        val s = System.currentTimeMillis()
//        postponeEnterTransition()
        vb.iv.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = 100F.dp2px() }
        vb.iv.glide(intent.getStringExtra("url")) {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
//        vb.lyRoot.setBackgroundColor(Color.YELLOW)
        vb.lyRoot.setOnClickListener {
//            finish()
            // finishAfterTransition()
            startAcScheme(AcThree::class.java)
        }
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
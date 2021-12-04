package com.zwping.a

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.webkit.ValueCallback
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.fm.FmHome
import com.zwping.alibx.*
import com.zwping.alibx.Requests.enqueue2

class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private val fmHome by lazy { FmHome() }
    override fun initView() {
        ToastUtil.show("123")
        vb?.progressBar?.setColors(Color.DKGRAY, Color.YELLOW, Color.RED)
        vb?.webView?.also {
            it.initSetting()
            it.initClient({vc, cc ->

            }, onProgress = {
                vb?.progressBar?.also { bar ->
                    bar.progress = it
                    bar.visibility = if (it==100) View.GONE else View.VISIBLE
                }
            }, shouldOverrideUrlLoading = { web, url ->
                try {
                    if (url.startsWith("weixin://")) {
                        web.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    } else false
                } catch (e: Exception){
                    false
                }
                }, onShowFileChooser = { web, saf2webFileCallback ->
                this.saf2webFileCallback = saf2webFileCallback
                openSAFFileChooser(ChooserType.IMAGE, 1000)
                true
            })



            it.postUrl("https://support.qq.com/product/368448?d-wx-push=1",
                "nickname=test&avatar=https://txc.qq.com/static/desktop/img/products/def-product-logo.png&openid=001".toByteArray())
        }
    }

    var saf2webFileCallback: ValueCallback<Array<Uri>>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        saf2webFileCallback?.onActivityResult(1000, requestCode, resultCode, data)
        saf2webFileCallback = null
    }


}
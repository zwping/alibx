package com.zwping.alibx

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.webkit.*
import android.widget.ProgressBar


/**
 *
 * zwping @ 1/15/21
 */
inline fun WebView.initSetting() : WebSettings =
    settings.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //5.0以上开启混合模式加载
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        loadWithOverviewMode = true
        useWideViewPort = true
        javaScriptEnabled = true  //允许js代码
        domStorageEnabled = true  //允许SessionStorage/LocalStorage存储
        displayZoomControls = false  //禁用放缩
        builtInZoomControls = false
        textZoom = 100  //禁用文字缩放
        setAppCacheMaxSize(10 * 1024 * 1024)  //10M缓存，api 18后，系统自动管理。
        setAppCacheEnabled(true)  //允许缓存，设置缓存位置
        setAppCachePath(context.getDir("appcache", 0).path)
        allowFileAccess = true  //允许WebView使用File协议
        savePassword = false  //不保存密码
        //设置UA
        // setUserAgentString(getUserAgentString() + " kaolaApp/" + AppUtils.getVersionName())
        // KaolaWebViewSecurity.removeJavascriptInterfaces(webView)  //移除部分系统JavaScript接口
        loadsImagesAutomatically = true  //自动加载图片
    }

inline fun WebView.initClient(lis: (vc: WebViewClient, cc: WebChromeClient)->Unit,
                              crossinline onProgress: (Int) -> Unit = {},
                              crossinline pageTitleLis:(String?)->Unit = {},
                              crossinline shouldOverrideUrlLoading: (WebView, String)-> Boolean = { _, _ -> false },
                              crossinline onShowFileChooser: (webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?) -> Boolean = { _, _ -> false }) {
    val webChromeClient = object: WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            pageTitleLis.invoke(title)
        }
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            onProgress.invoke(newProgress)
        }


        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            if (onShowFileChooser(webView, filePathCallback)) return true
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }
    val webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) "${request.url}" else "$request"
            if (shouldOverrideUrlLoading(view, url)) return true // js url协议外部消化, 不重载url
            view.loadUrl(url)
            return true
        }
    }
    setWebChromeClient(webChromeClient)
    setWebViewClient(webViewClient)
    lis.invoke(webViewClient, webChromeClient)
}

inline fun WebView.goBack2(): Boolean = if (canGoBack()) { goBack(); true} else false

inline fun WebView.android2Js(func: String, callback: ValueCallback<String>) {
    val script = if (func.startsWith("javascript:")) func else "javascript:$func"
    evaluateJavascript(script.also { /*it._log()*/ }, callback)
}
inline fun WebView.js2Android(obj: Any, interfaceName: String) { // shouldOverrideUrlLoading定制url协议更安全
    addJavascriptInterface(obj, interfaceName) // @JavascriptInterface
}

inline fun ProgressBar.setColors(bgColor: Int, progressColor: Int, secondaryProgress: Int = -1) {
    val bgClipDrawable = ClipDrawable(ColorDrawable(bgColor), Gravity.LEFT, ClipDrawable.HORIZONTAL)
    bgClipDrawable.level = 10000
    val progressClip = ClipDrawable(ColorDrawable(progressColor), Gravity.LEFT, ClipDrawable.HORIZONTAL)
    val secondaryClip = if (secondaryProgress == -1) null else ClipDrawable(ColorDrawable(secondaryProgress), Gravity.LEFT, ClipDrawable.HORIZONTAL)
    val progressDrawables = arrayOf<Drawable>(bgClipDrawable, secondaryClip ?: progressClip, progressClip)
    val progressLayerDrawable = LayerDrawable(progressDrawables)
    progressLayerDrawable.setId(0, android.R.id.background)
    progressLayerDrawable.setId(1, android.R.id.secondaryProgress)
    progressLayerDrawable.setId(2, android.R.id.progress)
    progressDrawable = progressLayerDrawable
}

enum class ChooserType{ FILE, IMAGE }

/*** js开启文件选择 ***/
fun showFileChooser(ac: Activity?, type: ChooserType, requestCode: Int) {
    val intent1 = Intent(Intent.ACTION_GET_CONTENT)
    intent1.addCategory(Intent.CATEGORY_OPENABLE)
    when(type){
        ChooserType.FILE -> intent1.type = "*/*"
        ChooserType.IMAGE -> intent1.type = "image/*"
    }
    val chooser = Intent(Intent.ACTION_CHOOSER)
    chooser.putExtra(Intent.EXTRA_TITLE, "选择${if (type == ChooserType.IMAGE) "图片" else "文件"}")
    chooser.putExtra(Intent.EXTRA_INTENT, intent1)
    ac?.startActivityForResult(chooser, requestCode)
}

/*** 文件选择回调回传至js ***/
fun ValueCallback<Array<Uri>>?.onActivityResult(requestCode: Int, resultCode: Int, data: Intent?,
                                                userRequestCode: Int) {
    if (userRequestCode == requestCode && (resultCode == Activity.RESULT_OK || requestCode == Activity.RESULT_CANCELED)) {
        val uris =WebChromeClient.FileChooserParams.parseResult(resultCode, data)
        this?.onReceiveValue(uris)
    }
    // this = null 最后一定要重新赋null值
}
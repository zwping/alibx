package com.zwping.alibx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.webkit.*
import android.widget.ProgressBar
import androidx.annotation.Px

/**
 * webview
 * zwping @ 1/15/21
 */
object WebViewUtil {

    fun initSetting(webView: WebView?): WebSettings? {
        webView ?: return null
        webView.settings.apply {
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //5.0以上开启混合模式加载
            loadWithOverviewMode = true
            useWideViewPort = true
            @SuppressLint("SetJavaScriptEnabled")
            javaScriptEnabled = true  //允许js代码
            domStorageEnabled = true  //允许SessionStorage/LocalStorage存储
            displayZoomControls = false  //禁用放缩
            builtInZoomControls = false
            textZoom = 100  //禁用文字缩放
            setAppCacheMaxSize(10L * 1024 * 1024)  //10M缓存，api 18后，系统自动管理。
            setAppCacheEnabled(true)  //允许缓存，设置缓存位置
            setAppCachePath(webView.context.getDir("appcache", 0).path)
            allowFileAccess = true  //允许WebView使用File协议
            savePassword = false  //不保存密码
            //设置UA
            // setUserAgentString(getUserAgentString() + " kaolaApp/" + AppUtils.getVersionName())
            // WebViewSecurity.removeJavascriptInterfaces(webView)  //移除部分系统JavaScript接口
            loadsImagesAutomatically = true  //自动加载图片
            return this
        }
    }

    fun initClient(webView: WebView?,
                   lis: (vc: WebViewClient, cc: WebChromeClient)->Unit,
                   onProgress: (Int) -> Unit = {},
                   pageTitleLis:(String?)->Unit = {},
                   shouldOverrideUrlLoading: (web: WebView, url: String)-> Boolean = { _, _ -> false },
                   onShowFileChooser: (web: WebView?, saf2webFileCallback: ValueCallback<Array<Uri>>?) -> Boolean = { _, _ -> false }){
       webView ?: return
        val webChromeClient = object: WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                pageTitleLis.invoke(title)
            }
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgress.invoke(newProgress)
            }

            override fun onShowFileChooser(webView: WebView?, saf2webFileCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                if (onShowFileChooser(webView, saf2webFileCallback)) return true
                return super.onShowFileChooser(webView, saf2webFileCallback, fileChooserParams)
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
        webView.webChromeClient = webChromeClient
        webView.webViewClient = webViewClient
        lis.invoke(webViewClient, webChromeClient)
    }

    fun goBack2(webView: WebView?): Boolean {
        webView ?: return false
        return if (webView.canGoBack()) { webView.goBack(); true} else false
    }

    fun setProgressBarColor(progressBar: ProgressBar?,
                            bgColor: Int,
                            progressColor: Int,
                            secondaryColor: Int = -1,
                            @Px radius: Float = 0F,
                            @Px height: Int = -1){
        progressBar ?: return
        val bgClipDrawable = ClipDrawable(GradientDrawable().apply { setColor(bgColor); cornerRadius = radius },
            Gravity.LEFT, ClipDrawable.HORIZONTAL)
        bgClipDrawable.level = 10000
        val progressClip = ClipDrawable(GradientDrawable().apply { setColor(progressColor); cornerRadius = radius },
            Gravity.LEFT, ClipDrawable.HORIZONTAL)
        var secondaryClip: ClipDrawable? = null
        if (secondaryColor != -1)
            secondaryClip = ClipDrawable(GradientDrawable().apply { setColor(secondaryColor); cornerRadius = radius },
                Gravity.LEFT, ClipDrawable.HORIZONTAL)
        val progressDrawables = arrayOf<Drawable>(bgClipDrawable, secondaryClip ?: progressClip, progressClip)
        val progressLayerDrawable = LayerDrawable(progressDrawables)
        progressLayerDrawable.setId(0, android.R.id.background)
        progressLayerDrawable.setId(1, android.R.id.secondaryProgress)
        progressLayerDrawable.setId(2, android.R.id.progress)
        if (height != -1 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            progressLayerDrawable.setLayerHeight(0, height)
            progressLayerDrawable.setLayerHeight(1, height)
            progressLayerDrawable.setLayerHeight(2, height)
        }
        progressBar.progressDrawable = progressLayerDrawable
    }

    fun android2Js(webView: WebView?, func: String, callback: ValueCallback<String>) {
        val script = if (func.startsWith("javascript:")) func else "javascript:$func"
        webView?.evaluateJavascript(script.also { /*it._log()*/ }, callback)
    }

    @SuppressLint("JavascriptInterface")
    fun js2Android(webView: WebView?, obj: Any, interfaceName: String) {
        // shouldOverrideUrlLoading定制url协议更安全
        webView?.addJavascriptInterface(obj, interfaceName)  // @JavascriptInterface
    }

    /*** 开启SAF文件选择 ***/
    fun openSAFFileChooser(ac: Activity?, type: ChooserType, webReqCode: Int) {
        val intent1 = Intent(Intent.ACTION_GET_CONTENT)
        intent1.addCategory(Intent.CATEGORY_OPENABLE)
        when(type){
            ChooserType.FILE -> intent1.type = "*/*"
            ChooserType.IMAGE -> intent1.type = "image/*"
        }
        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_TITLE, "选择${if (type == ChooserType.IMAGE) "图片" else "文件"}")
        chooser.putExtra(Intent.EXTRA_INTENT, intent1)
        ac?.startActivityForResult(chooser, webReqCode)
    }

    /*** SAF文件选择器结果回传webView ***/
    fun onActivityResult(saf2webFileCallback: ValueCallback<Array<Uri>>?,
                         webReqCode: Int,
                         requestCode: Int,
                         resultCode: Int,
                         data: Intent?){
        if (saf2webFileCallback != null &&
            webReqCode == requestCode &&
            (resultCode == Activity.RESULT_OK || requestCode == Activity.RESULT_CANCELED)) {
            val uris = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            saf2webFileCallback.onReceiveValue(uris)
        }
        // saf2webFileCallback = null // 最后一定要重新赋null值
    }

}

enum class ChooserType{ FILE, IMAGE }

/* ---------KTX----------- */

fun WebView?.initSetting(): WebSettings? = WebViewUtil.initSetting(this)

fun WebView?.initClient(lis: (vc: WebViewClient, cc: WebChromeClient)->Unit,
                        onProgress: (Int) -> Unit = {},
                        pageTitleLis:(String?)->Unit = {},
                        shouldOverrideUrlLoading: (web: WebView, url: String)-> Boolean = { _, _ -> false },
                        onShowFileChooser: (web: WebView?, saf2webFileCallback: ValueCallback<Array<Uri>>?) -> Boolean = { _, _ -> false }) {
    WebViewUtil.initClient(this, lis, onProgress, pageTitleLis, shouldOverrideUrlLoading, onShowFileChooser)
}

fun WebView?.goBack2(): Boolean = WebViewUtil.goBack2(this)
fun ProgressBar?.setColors(bgColor: Int,
                           progressColor: Int,
                           secondaryProgress: Int = -1,
                           @Px radius: Float = 0F,
                           @Px height: Int = -1) {
    WebViewUtil.setProgressBarColor(this, bgColor, progressColor, secondaryProgress, radius, height)
}

fun WebView?.android2Js(func: String, callback: ValueCallback<String>) {
    WebViewUtil.android2Js(this, func, callback)
}
fun WebView?.js2Android(obj: Any, interfaceName: String) {
    WebViewUtil.js2Android(this, obj, interfaceName)
}

/*** 开启SAF文件选择 ***/
fun Activity?.openSAFFileChooser(type: ChooserType, requestCode: Int) {
    WebViewUtil.openSAFFileChooser(this, type, requestCode)
}
/*** SAF文件选择器结果回传webView ***/
fun ValueCallback<Array<Uri>>?.onActivityResult(webReqCode: Int,
                                                requestCode: Int,
                                                resultCode: Int,
                                                data: Intent?) {
    WebViewUtil.onActivityResult(this, webReqCode, requestCode, resultCode, data)
    // saf2webFileCallback = null // 最后一定要重新赋null值
}

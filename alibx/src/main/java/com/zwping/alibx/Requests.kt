package com.zwping.alibx

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.random.Random

/**
 * 简易封装okhttp
 *   - 满足restful api / down file / upload file / coroutine
 * zwping @ 5/24/21
 * @lastTime: 2021年08月26日11:00:08
 */
object Requests {

    private val okHttpClient by lazy { _okHttpBuilder.build() }
    private val _okHttpBuilder by lazy { OkHttpClient.Builder() }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    fun init(block: OkHttpClient.Builder.() -> Unit) {
        // okHttpBuilder.addInterceptor(LogInterceptor(ctx.isAppDebug()))
        _okHttpBuilder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS)
        _okHttpBuilder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
        _okHttpBuilder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
        _okHttpBuilder.retryOnConnectionFailure(true)
        block.invoke(_okHttpBuilder)
    }

    fun get(url: String, params: HashMap<Any, Any?>? = null, kwargs: Optional.() -> Unit = {}): Call
            = okHttpClient.newCall(Request.Builder()._method("GET", url, kwargs, _params = params).build())

    fun post(url: String, data: HashMap<Any, Any?>? = null, json: JSONObject? = null, kwargs: Optional.() -> Unit = {}): Call
            = okHttpClient.newCall(Request.Builder()._method("POST", url, kwargs, _data = data, _json = json).build())

    fun put(url: String, data: HashMap<Any, Any?>? = null, kwargs: Optional.() -> Unit = {}): Call
            = okHttpClient.newCall(Request.Builder()._method("PUT", url, kwargs, _data = data).build())

    fun delete(url: String, kwargs: Optional.() -> Unit = {}): Call
            = okHttpClient.newCall(Request.Builder()._method("DELETE", url, kwargs).build())

    suspend fun Call.execute2(): Response2 = withContext(Dispatchers.Default) { _execute() }

    fun Call.enqueue2(
        owner: LifecycleOwner?,
        onResponse: (Call, Response, String) -> Unit,
        onFailure: (Call, msg: String) -> Unit,
        onStart: () -> Unit = {},
        onEnd: () -> Unit = {}
    ) {
        if (owner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) return
        val once = AtomicBoolean()
        owner?._bindLifecycle(once, onEnd, this)
        onStart.invoke()
        enqueue(object: Callback{
            override fun onResponse(call: Call, response: Response) {
                _onResponse(call, response, once, onResponse, onFailure, onEnd)
            }
            override fun onFailure(call: Call, e: IOException) {
                _onFailure(call, e, once, onFailure, onEnd)
            }
        })
    }

    fun Call.enqueueDown(
        owner: LifecycleOwner?,
        dir: String,
        onFinish: (Call, Response, filePath: String) -> Unit,
        onFailure: (Call, msg: String) -> Unit,
        onProgress: (progress: Int, curSize: Long, total: Long, fileName: String) -> Unit = { _, _, _, _ -> },
        name: String? = null,
        onStart: () -> Unit = {},
        onEnd: () -> Unit = {})
    {
        if (owner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) return
        val once = AtomicBoolean()
        owner?._bindLifecycle(once, onEnd, this)
        onStart.invoke()
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    handler.post {
                        onFailure(call, "请求失败-${response.code}")
                        onEnd.invoke()
                    }
                    return
                }
                val fileName = if (name?.isNotEmpty() == true) name
                else try { with(URL(call.request().url.toString()).path) { substring(lastIndexOf("/") + 1) } } catch (e: Exception) { "${System.currentTimeMillis()}-${Random.nextInt()}" }
                val file = File(dir, fileName)
                if (null != owner) {
                    owner.lifecycleScope.launch(Dispatchers.IO) {
                        if (response.writeFile(file, onProgress)) {
                            _onFinish(file, call, response, once, onFinish, onFailure, onEnd)
                        } else {
                            _onFailure(call, null, once, onFailure, onEnd)
                        }
                    }
                    return
                }
                if (response.writeFile(file, onProgress)) {
                    _onFinish(file, call, response, once, onFinish, onFailure, onEnd)
                } else {
                    _onFailure(call, null, once, onFailure, onEnd)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                _onFailure(call, e, once, onFailure, onEnd)
            }
        })
    }

    fun Response.writeFile(file: File, onProgress: (progress: Int, curSize: Long, total: Long, fileName: String) -> Unit): Boolean{
        if (!isSuccessful || body == null) return false
        try {
            val total = body!!.contentLength()
            var write = 0L
            var length: Int
            val ins = body!!.byteStream()
            val ous = FileOutputStream(file)
            val data = ByteArray(1024)
            var progress = -1
            while (ins.read(data).also { length = it } != -1) {
                ous.write(data, 0, length)
                write += length.toLong()
                (write * 100 / total).toInt().also {
                    if (progress != it) {
                        progress = it
                        handler.post { onProgress.invoke(progress, write, total, file.name) }
                    }
                }
            }
            return true
        } catch (e: IOException) {
            return false
        }
    }


    class UploadRequestBody(private val fileName: String, private val file: File, private val onProgress: (progress: Int, curSize: Long, total: Long, fileName: String) -> Unit) : RequestBody(){

        private val requestBody by lazy {
            // val mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.absolutePath))?.toMediaTypeOrNull()
            // file.asRequestBody(mediaType)
            file.asRequestBody("multipart/form-data".toMediaType())
        }

        private fun Sink(sink: Sink): Sink = object : ForwardingSink(sink) {
            private var bytesWritten = 0L
            private var contentLength = 0L
            private var progress = -1
            private var progressLock = -1

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) contentLength = contentLength()
                bytesWritten += byteCount
                (bytesWritten * 100 / contentLength).toInt().also {
                    if (progress != it) {
                        progress = it
                        handler.post {
                            if (progressLock == progress) return@post
                            progressLock = progress
                            onProgress.invoke(progressLock, bytesWritten, contentLength, fileName)
                        }
                    }
                }
            }
        }

        override fun contentType(): MediaType? = requestBody.contentType()
        override fun contentLength(): Long = requestBody.contentLength()

        override fun writeTo(sink: BufferedSink) {
            val bufferedSink = Sink(sink).buffer()
            requestBody.writeTo(bufferedSink)
            bufferedSink.flush()
        }

    }

    class Optional(
        val request: Request.Builder,
        val params: HashMap<Any, Any?> = hashMapOf(),
        val data: HashMap<Any, Any?> = hashMapOf(),
        var json: JSONObject? = null,
    ) {

        /*
        params	字典或字节序列，作为参数增加到url中
        data	字典、字节序列或文件对象，作为Request的内容
        json	JSON格式的数据，作为Request的内容
        headers	字典，HTTP定制头

        以下未实现
        cookies	字典或CookieJar，Request中的cookie
        auth	元组，支持HTTP认证功能
        files	字典类型，传输文件
        timeout	设定超时时间，秒为单位
        proxies	字典类型，设定访问代理服务器，可以增加登录认证
        allow_redirects	True/False，默认为True，重定向开关
        stream	True/False，默认为True，获取内容立即下载开关
        verify	True/False，默认为True，认证SSL证书开关
        cert	本地SSL证书路径
         */
        var multipartBody: MultipartBody.Builder? = null
        fun initMultipartBody() { multipartBody = MultipartBody.Builder(); multipartBody!!.setType(MultipartBody.FORM) }
        fun initMultipartBodyIsFile(name: String, file: File, onProgress: (progress: Int, curSize: Long, total: Long, fileName: String) -> Unit) {
            initMultipartBody()
            addRequestBodyOfFile(name, file, onProgress)
        }
        fun addRequestBodyOfFile(name: String, file: File, onProgress: (progress: Int, curSize: Long, total: Long, fileName: String) -> Unit){
            val fileName = file.name.safeName()
            multipartBody?.addFormDataPart(name, fileName, UploadRequestBody(fileName, file, onProgress))
        }

        fun addParams(key: Any, value: Any?) { params[key] = value }
        fun removeParams(vararg keys: Any) { keys.forEach { params.remove(it) } }

        fun addData(key: Any, value: Any?) { data[key] = value }
        fun removeData(vararg keys: Any) { keys.forEach { data.remove(it) } }

        fun addJson(key: String, value: Any?) { if (null == json) { json = JSONObject() }; json?.put(key, value) }
        fun removeJson(vararg keys: String) { keys.forEach { json?.remove(it) } }

        fun setHeader(name: String, value: String) { request.header(name, value) }
        fun addHeader(name: String, value: String) { request.addHeader(name, value) }
        fun removeHeader(vararg names: String) { names.forEach { request.removeHeader(it) } }

        // fun addCookie 使用CookieJar自动管理
    }

    data class Response2(val isSuccessful: Boolean,
                         val call: Call,
                         val response: ResponseBody? = null, val responseStr: String? = null, val responseOb: JSONObject? = null,
                         val msg: String = "数据错误!")

    inline fun Response2.isSuccessful2Safe(): Boolean = isSuccessful && !responseStr.isNullOrBlank()

    inline fun String.toJSONObject(): JSONObject? = try { JSONObject(this) } catch (e: Exception) { null }
    inline fun String.toJSONArray(): JSONArray? = try { JSONArray(this) } catch (e: Exception) { null }
    inline fun RequestBody.toJSONObject(): JSONObject? { return try {
        val copy = this; val buffer = Buffer(); copy.writeTo(buffer)
        return JSONObject(buffer.readUtf8())
    } catch (e: Exception) { null }
    }

    /* ================================= */

    private suspend fun Call._execute(): Response2  {
        return suspendCancellableCoroutine {
            it.invokeOnCancellation { cancel() }

            try {
                val response = execute()
                if (response.isSuccessful) {
                    val str = response.body?.string()
                    Response2(true, this, response.body, str, str?.toJSONObject())
                } else {
                    Response2(false, this, msg = "请求失败-${response.code}")
                }
            }catch (e: IOException) {
                val msg = when(e) {
                    is SocketTimeoutException -> "连接超时"
                    is ConnectException -> "连接服务器失败"
                    is UnknownHostException -> "网络异常"
                    else -> "数据错误"
                }
                Response2(false, this, msg = msg)
            }.also { response2 -> it.resume(response2) }
        }
    }

    private fun LifecycleOwner._bindLifecycle(once: AtomicBoolean, onEnd: () -> Unit, call: Call) {
        lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    if (once.get()) return
                    once.set(true); onEnd.invoke(); call.cancel()
                }
            }
        })
    }

    private fun _onResponse(call: Call, response: Response, once: AtomicBoolean, onResponse: (Call, Response, String) -> Unit, onFailure: (Call, String) -> Unit, onEnd: () -> Unit) {
        if (once.get()) return
        once.set(true)
        if (response.isSuccessful && response.body != null) {
            val str = response.body!!.string()
            try {
                handler.post { onResponse.invoke(call, response, str); onEnd() }
            } catch (e: Exception) {
                handler.post { onFailure.invoke(call, "数据错误"); onEnd.invoke() }
            }
        } else {
            handler.post { onFailure.invoke(call, "请求失败-${response.code}"); onEnd.invoke() }
        }
        /* response.body!!.string() 过程异步, 不能放在主线程中读取
        handler.post {
            try {
                if (response.isSuccessful) onResponse.invoke(call, response)
                else onFailure.invoke(call, "请求失败-${response.code}")
                onEnd.invoke()
            } catch (e: Exception) {
                onFailure.invoke(call, "数据错误"); onEnd.invoke()
            }
        }
         */
    }

    private fun _onFinish(file: File, call: Call, response: Response, once: AtomicBoolean, onFinish: (Call, Response, filePath: String) -> Unit, onFailure: (Call, String) -> Unit, onEnd: () -> Unit) {
        if (once.get()) return
        once.set(true)
        handler.post {
            try {
                onFinish.invoke(call, response, file.absolutePath); onEnd.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure.invoke(call, "数据错误"); onEnd.invoke()
            }
        }
    }

    private fun _onFailure(call: Call, e: IOException?, once: AtomicBoolean, onFailure: (Call, String) -> Unit, onEnd: () -> Unit) {
        if (once.get()) return
        once.set(true)
        e?.printStackTrace()
        val msg = when(e) {
            is SocketTimeoutException -> "连接超时"
            is ConnectException -> "连接服务器失败"
            is UnknownHostException -> "网络异常"
            null -> "数据错误!"
            else -> "未知错误"
        }
        handler.post { onFailure.invoke(call, msg); onEnd.invoke() }
    }

    private fun Request.Builder._method(method: String, url: String,
                                        kwargs: Optional.() -> Unit,
                                        _params: HashMap<Any, Any?>? = null,
                                        _data: HashMap<Any, Any?>? = null,
                                        _json: JSONObject? = null): Request.Builder{
        val optional = Optional(this)
        if (null != _params) optional.params.putAll(_params)
        if (null != _data) optional.data.putAll(_data)
        if (null != _json) optional.json = _json
        kwargs.invoke(optional)

        url(url.toHttpUrl().newBuilder().apply { optional.params.forEach{ addQueryParameter(it.key.toString(), it.value?.toString())} }.build())
        val requestBody = when { // 注意表单类型的优先级
            null != optional.multipartBody -> optional.multipartBody?.apply { optional.data.forEach{ addFormDataPart(it.key.toString(), it.value?.toString() ?: "")} }?.build()
            null != optional.json -> optional.json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            0 != optional.data.size -> FormBody.Builder().apply { optional.data.forEach{ add(it.key.toString(), it.value?.toString() ?: "") } }.build()
            else -> null
        }
        method(method, requestBody)
        return this
    }

    inline fun String.safeName() = try { URLEncoder.encode(this, "UTF-8") } catch (e: Exception) { "${System.currentTimeMillis()}_unName" }
    inline fun Context?.isAppDebug() = this?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE) ?: 1 != 0
}

package com.zwping.alibx

/* com.squareup.retrofit2:retrofit:2.9.0 */
/**
 * retrofit 简易扩展
 *  - 上传/下载/断点续传复杂功能未扩展开
 *  - [RetrofitFactory.init]初始化配置[RetrofitFactory.retrofit] 实例, DebugApp默认打印简单日志[LogInterceptor]
 *  - [enqueue2] Call<ResponseBody>.enqueue 扩展, 且具备生命周期感知
 *  - [write2FileProgressOnThread] byte写入文件进度监听, 写入需在线程中完成
 *  - [UploadRequestBody] 文件上传进度读取RequestBody封装, 整个请求需在线程中完成
 *
 * zwping @ 12/29/20
 */
//class RetrofitFactory {
//    companion object {
//
//        private val retrofitBuilder by lazy { Retrofit.Builder() }
//        private val okhttpBuilder by lazy { OkHttpClient.Builder() }
//
//        /*** retrofit实例 ***/
//        val retrofit: Retrofit by lazy { retrofitBuilder.client(okhttpBuilder.build()).build()}
//
//        /**
//         * init
//         * @param dsl dsl风格配置 this->Retrofit.Builder it->OkHttpClient.Builder
//         */
//        fun init(dsl: Retrofit.Builder.(OkHttpClient.Builder) -> Unit) {
//            okhttpBuilder.apply {
//                // it.addInterceptor(LogInterceptor(ctx.isAppDebug()))
//                connectTimeout(60 * 1000, TimeUnit.MILLISECONDS)
//                readTimeout(60 * 1000, TimeUnit.MILLISECONDS)
//                writeTimeout(60 * 1000, TimeUnit.MILLISECONDS)
//            }
//            dsl.invoke(retrofitBuilder, okhttpBuilder)
//        }
//    }
//}
//
///**
// * retrofit 异步请求
// * @param owner 绑定生命周期, onDestroy主动取消
// * @param onSuccess 成功
// * @param onError 失败
// * @param onStart 请求开始 loading show
// * @param onEnd 请求结束 loading hide
// */
//inline fun Call<ResponseBody>.enqueue2(owner: LifecycleOwner?,
//                                       crossinline onSuccess: Call<ResponseBody>.(Response<ResponseBody>) -> Unit,
//                                       crossinline onError: Call<ResponseBody>.(Throwable) -> Unit,
//                                       crossinline onStart: Call<ResponseBody>.() -> Unit = { },
//                                       crossinline onEnd: Call<ResponseBody>.() -> Unit = { }) {
//    if (owner?.lifecycle?.currentState == Lifecycle.State.DESTROYED) return // destroyed
//    val once = AtomicBoolean() // 加锁, 结果互不干扰
//    owner?.lifecycle?.addObserver(object : LifecycleEventObserver {
//        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) { // 绑定生命周期
//            if (event == Lifecycle.Event.ON_DESTROY) {
//                if (once.get()) return
//                once.set(true)
//                onEnd.invoke(this@enqueue2);this@enqueue2.cancel()
//            }
//        }
//    })
//    onStart.invoke(this)
//    enqueue(object : Callback<ResponseBody> {
//        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//            if (once.get()) return
//            once.set(true)
//            try {
//                println(call.request())
//                onSuccess.invoke(call, response);onEnd.invoke(call)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                onError.invoke(call, e);onEnd.invoke(call)
//            }
//        }
//
//        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//            t.printStackTrace()
//            if (once.get()) return
//            once.set(true)
//            onError.invoke(call, t);onEnd.invoke(call)
//        }
//    })
//}
//
//
///**
// * 下载文件且监听进度
// * @param path
// * @param onProgress (当前下载的进度, 总长度, 完成)
// * @param onError
// */
//inline fun Response<ResponseBody>.write2FileProgressOnThread(path: String?, onProgress: (progress: Long, total: Long, done: Boolean) -> Unit, onError: (Throwable) -> Unit) {
//    if (!isSuccessful) {
//        onError.invoke(RuntimeException("response error !!!"));return
//    }
//    try {
//        val body = body()
//        val total = body!!.contentLength()
//        var write = 0L
//        var length: Int
//        val ins = body.byteStream()
//        val file = File(path!!)
//        val ous = FileOutputStream(file)
//        val data = ByteArray(1024)
//        while (ins.read(data).also { length = it } != -1) {
//            ous.write(data, 0, length)
//            write += length.toLong()
//            onProgress.invoke(write, total, write == total)
//        }
//    } catch (e: IOException) {
//        e.printStackTrace()
//        onError.invoke(e)
//    }
//}
//
///**
// * 上传文件进度
// */
//class UploadRequestBody(private val requestBody:RequestBody, private val onProgress: (progress:Long, total:Long)->Unit): RequestBody() {
//
//    private var bufferedSink: BufferedSink? = null
//    private fun sink(sink: Sink): Sink = object : ForwardingSink(sink) {
//            var bytesWritten = 0L
//            val contentLength by lazy { contentLength() }
//
//            override fun write(source: Buffer, byteCount: Long) {
//                super.write(source, byteCount)
//                bytesWritten += byteCount
//                onProgress.invoke(bytesWritten, contentLength)
//            }
//        }
//
//    override fun contentType(): MediaType? = requestBody.contentType()
//    override fun contentLength(): Long = requestBody.contentLength()
//
//    override fun writeTo(sink: BufferedSink) {
//        // if (null == bufferedSink) bufferedSink = sink(sink).buffer()
//        if (null == bufferedSink) bufferedSink = Okio.buffer(sink(sink))
//        bufferedSink?.also { requestBody.writeTo(it); it.flush() }
//    }
//
//}
//
///**
// * retrofit 日志拦截器
// *  - @Headers("LogEnable: true") // 日志开关1, @streaming时必须关闭日志打印
// *  - @Headers("LogHeader: false") // 请求/响应头开关
// * @param logEnable 日志总开关, 建议非debug模式关闭
// * @param logLis 日志打印实现方式
// */
//class LogInterceptor(var logEnable: Boolean = false, var logLis: (String?) -> Unit = { Log.e("Retrofit", "$it") }) : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
//        val request = chain.request()
//        if (!logEnable) return chain.proceed(request)
//        val _method = request.method()
//        val _reqUrl = request.url()
//        if (request.header("LogEnable")?.toLowerCase(Locale.ROOT) == "false") {
//            logLis.invoke("<-- $_method $_reqUrl")
//            val startNs = System.nanoTime()
//            val response = chain.proceed(request)
//            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
//            logLis.invoke("<-- $_method - ${response.code()} ${tookMs}ms $_reqUrl")
//            return response
//        }
//        val UTF8 = Charset.forName("UTF-8")
//        val _logHeaderEnable = request.header("LogHeader")?.toLowerCase(Locale.ROOT) == "true" // @Headers("LogHeader: true")
//        // log for request
//        var _reqBody = ""
//        try {
//            request.body()?.also {
//                val buffer = Buffer();it.writeTo(buffer)
//                val r = buffer.readString(it.contentType()?.charset(UTF8) ?: UTF8)
//                _reqBody = "$r"
//            }
//        } catch (e: Exception) {
//            _reqBody = "Body fail!! ${e.message}"
//        }
//        logLis.invoke("<-- $_method " +
//                "$_reqUrl " +
//                "$_reqBody " +
//                "${if (_logHeaderEnable) "\nrequest\n${request.headers()}" else ""}" +
//                "")
//        // log for response
//        val startNs = System.nanoTime()
//        var response: okhttp3.Response? = null
//        try {
//            response = chain.proceed(request)
//        } catch (e: Exception) {
//            logLis.invoke("<-- http request fail!! ${e.message}") // 请求失败
//        }
//        val _resCode = response?.code()
//        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
//        var _resBody = ""
//        try {
//            response?.body()?.also { resBody ->
//                val source = resBody.source();source.request(Long.MAX_VALUE)
//                val buffer = source.buffer
//                _resBody = buffer.clone().readString(UTF8)
//            }
//        } catch (e: Exception) {
//            _resBody = "Body fail!! ${e.message}"
//        }
//        _resBody = if (_resBody != "") "\n${_resBody.substring(0, if (_resBody.length > 1001) 1001 else _resBody.length)}" else "Body is empty!!"
//        logLis.invoke("<-- $_method - $_resCode ${tookMs}ms " +
//                "$_reqUrl " +
//                "$_reqBody " +
//                "${if (_logHeaderEnable) "\nrequest\n${request.headers()}" else ""}" +
//                "${if (_logHeaderEnable) "\nresponse\n${response?.headers()}" else ""}" +
//                "\n$_resBody " +
//                "")
//        return response ?: chain.proceed(request)
//    }
//}

//inline fun Context?.isAppDebug() = this?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE) ?: 1 != 0

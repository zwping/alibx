package com.zwping.alibx

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import java.io.File
import java.security.MessageDigest

/**
 * 图片加载常用功能平铺: [IImgLoader]
 * 图片加载过程中图片常用操作功能平铺: [IImgLoaderOpt]
 * zwping @ 2021/10/28
 */
private interface IImgLoader {

    // 全局配置 级别最低
    var globalPlaceHolder: Int?
    var globalError: Int?
    var globalAnimType: AnimType?

    /**
     * 加载图片
     * @param opt 加载图片过程中的配置项 [IImgLoaderOpt]
     */
    fun glide(iv: ImageView?, url: String?, opt: ImgLoaderOpt.()->Unit = {})
    fun glide(iv: ImageView?, url: String?, ctx: Context?=null, opt: ImgLoaderOpt.()->Unit = {})

    /**
     * 清理图片缓存 disk & memory
     * @param viewOrTarget 图片的key由url+其它操作共同构成, 根据view或加载对象可成功清理
     */
    fun clear(ctx: Context?, viewOrTarget: Any?)

    /**
     * 下载图片
     * @param sucLis 下载成功[Bitmap]
     * @param errLis 下载失败
     * @param wPx 下载成功后图片宽/px
     * @param hPx 下载成功后图片高/px
     */
    fun <T> down(url: String?, ctx: Context?,
                 sucLis: (Bitmap)->Unit, errLis: ()->Unit,
                 wPx: Int=Target.SIZE_ORIGINAL, hPx: Int=Target.SIZE_ORIGINAL): T?
    /*** 同步下载图片, 需要在io线程中 ***/
    suspend fun <T> downSync(url: String?, ctx: Context?,
                             wPx: Int=Target.SIZE_ORIGINAL, hPx: Int=Target.SIZE_ORIGINAL): T?

    /*** 预加载 ***/
    fun preload(ctx: Context?, vararg urls: String, opt: ImgLoaderOpt.() -> Unit={})

    /*** 获取磁盘缓存大小 ***/
    fun getDiskCacheSize(ctx: Context?): Long
    suspend fun clearDiskCache(ctx: Context?) // 建议MemoryCache交由系统完成

    fun isDestroy(ctx: Context?): Boolean {
        ctx ?: return true
        if (ctx is Activity) return ctx.isFinishing || ctx.isDestroyed
        return false
    }
}

interface IImgLoaderOpt {
    // 占位图
    var placeHolderDrw: Drawable?
    var errorDrw: Drawable?
    var placeHolder: Int?
    var error: Int?

    // 对图片的转码类型
    var transcodeType: TranscodeType
    fun asDrawable() { transcodeType = TranscodeType.Drawable }
    fun asBitmap() { transcodeType = TranscodeType.Bitmap }
    fun asGif() { transcodeType = TranscodeType.Gif }
    fun asFile() { transcodeType = TranscodeType.File }

    // 图片展示类型, 使用ImageView.ScaleType管理
    var scaleType: ImageView.ScaleType?

    // 图片形状 支持圆形 & 正方形
    var shapeType: ShapeType
    fun circleCrop() { shapeType = ShapeType.Circle }
    fun squareCrop() { shapeType = ShapeType.Square }

    // 图片边框
    data class Stroke(val wDp: Float, @ColorInt val color: Int)
    var stroke: Stroke?
    fun setStroke(wDp: Float, @ColorInt color: Int) { stroke = Stroke(wDp, color) }

    // 图片圆角, 指定圆角半径
    var radii: FloatArray?
    fun setRadius(rPx: Float) { setRadius(rPx, rPx, rPx, rPx) }
    fun setRadius(tl: Float=0F, tr: Float=0F, br: Float=0F, bl: Float=0F) { radii = floatArrayOf(tl,tl, tr,tr, br,br, bl,bl) }

    // 过渡动画
    var animType: AnimType?

    // 缓存
    var cacheType: CacheType
    fun skipMemoryCache() { cacheType = CacheType.Disk }
    fun skipDiskCache() { cacheType = CacheType.Memory }
    fun skipCache() { cacheType = CacheType.None }

    // 本地模式, 只读取缓存图片
    var onlyReadCache: Boolean
    fun setOnlyRead() { onlyReadCache=true }

    // 缩略图
    var thumbnailUrl: String?

    // 转换大小
    var targetWidth: Float?
    var targetHeight: Float?

    // 监听
    var lis: ((readyOrFailed: Boolean, resource: Any?, e: GlideException?) -> Unit)?
    /**
     * 资源加载监听, 可控制资源渲染过程
     *  - 当resource为[GifDrawable]时
     *   - [GifDrawable.setLoopCount]可控制gif播放次数
     *   - [GifDrawable.registerAnimationCallback]可监听gif播放过程[播放结束]
     */
    fun setOnLoaderListener(lis: (readyOrFailed: Boolean, resource: Any?, e: GlideException?) -> Unit)

    // gif只播放一次
    var gifOncePlay: Boolean
}
enum class AnimType{ WithCrossFade }
enum class ShapeType{ Default, Circle, Square }
enum class TranscodeType { Drawable, Bitmap, Gif, File }
enum class CacheType{ All, Memory, Disk, None }

class ImgLoaderOpt: IImgLoaderOpt {
    override var placeHolderDrw: Drawable?=null
    override var errorDrw: Drawable?=null
    override var placeHolder: Int?=null
        get() = field ?: ImageLoader.globalPlaceHolder
    override var error: Int?=null
        get() = field ?: ImageLoader.globalError
    override var transcodeType = TranscodeType.Drawable
    override var scaleType: ImageView.ScaleType? = null
    override var shapeType = ShapeType.Default
    override var stroke: IImgLoaderOpt.Stroke?=null
    override var radii: FloatArray?=null
    override var animType: AnimType?=null
        get() = field ?: ImageLoader.globalAnimType
    override var cacheType = CacheType.All
    override var onlyReadCache: Boolean=false
    override var thumbnailUrl: String?=null
    override var targetWidth: Float?=null
    override var targetHeight: Float?=null
    override var gifOncePlay: Boolean=false
    override var lis: ((readyOrFailed: Boolean, resource: Any?, e: GlideException?) -> Unit)?=null
    override fun setOnLoaderListener(lis: (readyOrFailed: Boolean, resource: Any?, e: GlideException?) -> Unit) {
        this.lis = lis
    }
}

/**
 * 基于glide4.0加载图片
 */
object ImageLoader: IImgLoader {
    override var globalPlaceHolder: Int? = null
    override var globalError: Int? = null
    override var globalAnimType: AnimType? = null

    override fun glide(iv: ImageView?, url: String?, opt: ImgLoaderOpt.()->Unit) { glide(iv, url, null, opt) }
    override fun glide(iv: ImageView?, url: String?, ctx: Context?, opt: ImgLoaderOpt.()->Unit) { iv?.also { builder(it, url, ctx, opt)?.into(it) } }

    override fun <T> down(url: String?, ctx: Context?,
                          sucLis: (Bitmap) -> Unit, errLis: () -> Unit,
                          wPx: Int, hPx: Int): T? {
        ctx ?: return null
        val res: CustomTarget<Bitmap> = Glide.with(ctx).asBitmap().load(url).into(object: CustomTarget<Bitmap>(wPx, hPx){
            override fun onStart() { }
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) { sucLis(resource) }
            override fun onLoadFailed(errorDrawable: Drawable?) { errLis() }
            override fun onLoadCleared(placeholder: Drawable?) { }
        }) // res使用完后需要清除避免crash
        return res as T
    }

    override suspend fun <T> downSync(url: String?, ctx: Context?, wPx: Int, hPx: Int): T? {
        ctx ?: return null
        val res: FutureTarget<Bitmap> = Glide.with(ctx).asBitmap().load(url).submit(wPx, hPx)
        // res.isDone判断结果, res.get()获取Bitmap, res使用完后需要清除避免crash
        return res as T
    }

    override fun preload(ctx: Context?, vararg urls: String, opt: ImgLoaderOpt.() -> Unit) {
        ctx ?: return; urls.forEach { builder(null, it, ctx)?.preload() }
    }


    override fun clear(ctx: Context?, viewOrTarget: Any?) {
        ctx ?: return
        when(viewOrTarget) {
            is View -> Glide.with(ctx).clear(viewOrTarget)
            is Target<*> -> Glide.with(ctx).clear(viewOrTarget)
        }
    }

    override fun getDiskCacheSize(ctx: Context?): Long {
        ctx ?: return 0L
        return getFolderSize(File(ctx.cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR))
    }
    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val files = file.listFiles() ?: return size
            for (i in files.indices) {
                size = if (files[i].isDirectory) size + getFolderSize(files[i]) else size + files[i].length()
            }
        } catch (e: Exception) { e.printStackTrace() }
        return size
    }

    override suspend fun clearDiskCache(ctx: Context?) { ctx ?: return; Glide.get(ctx).clearDiskCache() }

    // --------------

    private fun builder(iv: ImageView?, url: String?,
                       ctx: Context?=null,
                       option: ImgLoaderOpt.()->Unit={ }): RequestBuilder<*>? {
        if (iv == null && ctx == null) return null
        val context = ctx ?: iv!!.context
        if (isDestroy(context)) return null // 避免 You cannot start a load for a destroyed activity com.bumptech.glide.i.l.a(RequestManagerRetriever.java:2)
        val opt = ImgLoaderOpt()
        option.invoke(opt)
        val reqManager = Glide.with(context)

        val reqBuilder = when(opt.transcodeType){
            TranscodeType.Drawable -> reqManager.asDrawable()
            TranscodeType.Bitmap -> reqManager.asBitmap()
            TranscodeType.Gif -> reqManager.asGif()
            TranscodeType.File -> reqManager.asFile()
        }

        if (opt.placeHolderDrw != null) reqBuilder.placeholder(opt.placeHolderDrw)
        else if (opt.placeHolder != null) reqBuilder.placeholder(opt.placeHolder!!)
        if (opt.errorDrw != null) reqBuilder.error(opt.errorDrw)
        else if (opt.error != null) reqBuilder.error(opt.error!!)

        if (opt.scaleType != null) iv?.scaleType = opt.scaleType

        if (opt.shapeType != ShapeType.Default || opt.radii != null || opt.stroke != null) {
            reqBuilder.transform(
                RoundTransformation(
                    opt.radii ?: floatArrayOf(0F,0F, 0F,0F, 0F,0F, 0F,0F),
                    opt.stroke?.color,
                    opt.stroke?.wDp,
                    opt.shapeType == ShapeType.Circle,
                    opt.shapeType == ShapeType.Square
                )
            )
        }

        if (opt.cacheType == CacheType.None || opt.cacheType == CacheType.Memory)
            reqBuilder.diskCacheStrategy(DiskCacheStrategy.NONE)
        if (opt.cacheType == CacheType.None || opt.cacheType == CacheType.Disk)
            reqBuilder.skipMemoryCache(true)

        if (opt.onlyReadCache) reqBuilder.onlyRetrieveFromCache(true)

        when(opt.transcodeType){
            TranscodeType.Drawable -> listener(reqBuilder as RequestBuilder<Drawable>, opt.lis)
            TranscodeType.Bitmap -> listener(reqBuilder as RequestBuilder<Bitmap>, opt.lis)
            TranscodeType.Gif -> listener(reqBuilder as RequestBuilder<GifDrawable>, opt.lis)
            TranscodeType.File -> listener(reqBuilder as RequestBuilder<File>, opt.lis)
        }
        return reqBuilder.load(url)
    }

    private fun <T> listener(req: RequestBuilder<T>, lis: ((readyOrFailed: Boolean, resource: Any?, e: GlideException?) -> Unit)?) {
        req.listener(object :RequestListener<T>{
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>?, isFirstResource: Boolean): Boolean {
                lis?.invoke(false, null, e); return false
            }
            override fun onResourceReady(resource: T, model: Any?, target: Target<T>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                lis?.invoke(true, resource, null); return false
            }
        })
    }

    /**
     * 为bitmap增加类似[MaterialButton]的strokeColor / strokeWidth / cornerRadius
     * 更多bitmap转换可见github glide-transformations
     * @param cornerRadii floatArray(8) 对应的8个角度半径
     * @param strokeColor 边框颜色
     * @param strokeWDp 边框宽度
     * @param isCircle 圆形 radii失效
     * @param isSquare 正方形
     */
    class RoundTransformation(
        private val cornerRadii: FloatArray,
        @ColorInt private val strokeColor: Int?=null,
        private val strokeWDp: Float?=null,
        private val isCircle: Boolean=false,
        private val isSquare: Boolean=true) : BitmapTransformation() {

        private val ID = "com.zwping.alibx.RoundTransformation$cornerRadii$strokeColor$strokeWDp$isCircle$isSquare"
        private val ID_BYTES = ID.toByteArray()

        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            val inBmp = if (isCircle or isSquare) {
                Math.max(outWidth, outHeight).let { TransformationUtils.centerCrop(pool, toTransform, it, it) } // 多绘制一次
            } else {
                toTransform
            }
            // 取值
            val w = inBmp.width; val h = inBmp.height
            val strokeW = if (strokeWDp!=null) 0.5F+strokeWDp* Resources.getSystem().displayMetrics.density else 0F
            val radii = if (!isCircle) cornerRadii else w.toFloat().let { floatArrayOf(it, it, it, it, it, it, it, it) }
            // 计算
            val halfStrokeW = if (strokeW==0F) 0F else strokeW/2
            val rectf = RectF(halfStrokeW, halfStrokeW, w-halfStrokeW, h-halfStrokeW)
            val path = Path().apply { addRoundRect(rectf, radii, Path.Direction.CW) }
            // 待绘制bitmap
            val bmp = pool.get(w, h, Bitmap.Config.ARGB_8888).apply { setHasAlpha(true); density = inBmp.density }
            // 添加画布 绘制原图
            val canvas = Canvas(bmp)
            val paint = Paint().apply { isAntiAlias=true; shader = BitmapShader(inBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
            canvas.drawPath(path, paint)
            if (strokeColor == null) return bmp
            // 绘制圆环
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
            strokePaint.color = strokeColor; strokePaint.strokeWidth = strokeW
            canvas.drawPath(path, strokePaint)
            return bmp
        }

        override fun equals(other: Any?): Boolean { return other is RoundTransformation
        }
        override fun hashCode(): Int { return ID.hashCode() }
        override fun updateDiskCacheKey(messageDigest: MessageDigest) { messageDigest.update(ID_BYTES) }

    }
}

/* ----------KTX----------- */

/**
 * 加载图片
 * @param opt 加载图片过程中的配置项 [IImgLoaderOpt]
 */
fun ImageView?.glide(url: String?, opt: ImgLoaderOpt.()->Unit = {}) { glide(url, null, opt) }
fun ImageView?.glide(url: String?, ctx: Context?=null, opt: ImgLoaderOpt.()->Unit = {}) {
    ImageLoader.glide(this, url, ctx, opt)
}
fun Context?.isDestroy(): Boolean = ImageLoader.isDestroy(this)
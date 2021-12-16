package com.zwping.alibx

import android.util.Log
import androidx.annotation.IntRange

/**
 * 精简工具
 * zwping @ 2021/11/30
 */
object Util {

    var DEBUG = true

    /**
     * 以System.out通用TAG打印日志, 记录logd方法调用位置
     * @param stackInfo 是否打印关键堆栈信息
     * @param stackOffset 堆栈偏移量, 定位调用位置
     */
    @JvmStatic
    @JvmOverloads
    fun logd(vararg msgs: Any?, stackInfo: Boolean=true, stackOffset: Int=1) {
        if (!DEBUG) return
        var element : Any? = null
        if (stackInfo) {
            Thread.currentThread().stackTrace.also {
                if (it.size >= (4+stackOffset)) { element = it[3+stackOffset]; return@also }
            }
        }
        element = if (element != null) "$element -> " else ""
        Log.d("System.out", "$element${msgs.map { "$it" }.toString().let { it.substring(1, it.length-1) }}")
    }


    /**
     * 文件大小格式化
     * @param size
     * @param unitIndex [B, KB, MB, GB, TB]下标 -1->自动判断单位
     * @param sig 有效位数 0->没有小数位
     */
    @JvmStatic
    @JvmOverloads
    fun formatBytes(size: Long?,
                   @IntRange(from=-1, to=4) unitIndex: Int=-1,
                   @IntRange(from=0, to=3) sig: Int=1): String? {
        size ?: return null
        val units = arrayOf("bytes", "KB", "MB", "GB", "TB")
        if (size <= 0) return String.format("%.${sig}f%s", 0F, units[if (unitIndex==-1) 0 else unitIndex])
        val index = when{
            unitIndex!=-1 -> unitIndex
            size in 0 until 1024 -> 0
            size in 1024 until 1024*1024 -> 1
            size in 1024*1024 until 1024*1024*1024 -> 2
            size in 1024*1024*1024 until 1024*1024*1024*1024L -> 3
            else -> 4
        }
        return when(index){
            0 -> String.format("%.${sig}f%s", size*1F, units[index])
            1 -> String.format("%.${sig}f%s", size/1024F, units[index])
            2 -> String.format("%.${sig}f%s", size/(1024*1024F), units[index])
            3 -> String.format("%.${sig}f%s", size/(1024*1024*1024F), units[index])
            else -> String.format("%.${sig}f%s", size/(1024*1024*1024*1024F), units[index])
        }
    }

    object KTX {
        fun logd(vararg msg: Any?, stackInfo: Boolean=true) {
            Util.logd(*msg, stackInfo=stackInfo, stackOffset=2)
        }
    }

}

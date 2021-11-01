package com.zwping.alibx

import androidx.recyclerview.widget.DiffUtil

/**
 * DiffUtil封装扩展
 *  -[IDiffUtil]类对象管理oldData, 包含计算差异[calculateDiff]/监听data集合数据是否已产生差异[setOnDataDiffListener]
 *
 * zwping @ 12/3/20
 *
 * @param areItemsTheSame
 * @param areContentsTheSame
 * @param getChangePayload
 *
 * @lastTime 2021年11月01日17:18:22
 */
class IDiffUtil<B>(
        private val areItemsTheSame: (od: B?, nd: B?) -> Boolean,
        private val areContentsTheSame: (od: B?, nd: B?) -> Boolean = { _, _ -> true },
        private val getChangePayload: ((od: B?, nd: B?) -> Any?)? = { _, _ -> null }
) {

    var oldData: List<B>? = null // 老数据
    private var dataDiffListener: (IDiffUtil<B>.(List<B>?) -> Unit)? = null


    /*** 类对象管理oldData ***/
    fun calculateDiff(data: List<B>?, detectMoves: Boolean = true): DiffUtil.DiffResult {
        // println("需要论证类对象管理oldData中oldData和data深拷贝问题: $oldData $data")
        val result = Callback(oldData, data, { dataDiffListener?.invoke(this, it);this.oldData = it }, areItemsTheSame, areContentsTheSame, getChangePayload)
        return DiffUtil.calculateDiff(result, detectMoves)
    }

    /**
     * 监听data数据是否已产生差异
     * 借助(DiffUtil差量算法)实现List高效比对
     */
    fun setOnDataDiffListener(lis: IDiffUtil<B>.(List<B>?) -> Unit): IDiffUtil<B> = apply { this.dataDiffListener = lis }

    /* ------------------------------ */

    /**
     * DiffUtil Callback 封装
     * @param oldData 老数据
     * @param newData 新数据
     * @param dataDiffListener 数据有差异的结果监听
     * @param areItemsTheSame 新老数据Item是否相同(业务ID比对) - 粗粒度
     * @param areContentsTheSame 新老数据Item内容是否相同(关键对象比对) - 细粒度
     * @param getChangePayload 新老数据Item内容如何不同(返回值会作为payloads传入onBindViewHolder())
     */
    private class Callback<B>(
            private val oldData: List<B>?,
            private val newData: List<B>?,
            private val dataDiffListener: (newData: List<B>?) -> Unit,
            private val areItemsTheSame: (oldData: B?, newData: B?) -> Boolean?,
            private val areContentsTheSame: (oldData: B?, newData: B?) -> Boolean?,
            private val getChangePayload: ((oldData: B?, newData: B?) -> Any?)? = null) : DiffUtil.Callback() {

        private var oneResponse = true

        init {
            if (oldListSize != newListSize) { invoke() } // 对象判断 - 粗粒度
        }

        override fun getOldListSize(): Int = oldData?.size ?: 0
        override fun getNewListSize(): Int = newData?.size ?: 0

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val result = areItemsTheSame(oldData?.get(oldItemPosition), newData?.get(newItemPosition))
            if (result == false) { invoke() }
            return result ?: true
        }


        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val result = areContentsTheSame(oldData?.get(oldItemPosition), newData?.get(newItemPosition))
            if (result == false) { invoke() }
            return result ?: true
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return getChangePayload?.invoke(oldData?.get(oldItemPosition), newData?.get(newItemPosition))
        }



        private fun invoke() {
            if (oneResponse) { dataDiffListener.invoke(newData); oneResponse = false }
        }
    }
}
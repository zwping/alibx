package com.zwping.alibx

/**
 * 事件总线
 * zwping @ 2021/11/18
 */
interface EventBusInterface {
    fun post(tag: String, msg: Any?=null)
    fun subscribe(tag: String, block: (msg: Any?) -> Unit)
    fun unSubscribe()
}
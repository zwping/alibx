package com.zwping.alibx

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息总线
 * zwping @ 2021/11/18
 */
object Bus {

    private val buses by lazy { ConcurrentHashMap<TagKey, ((msg: Any?) -> Unit)>() }
    @Synchronized private fun busFilter(filter: TagKey.() -> Boolean) = buses.filter { it.key.filter() }

    /**
     * 发送消息
     * @param key 消息key
     * @param msg 消息内容
     */
    fun post(key: String, msg: Any?=null) { busFilter { isKey(key) }.values.forEach { it.invoke(msg) } }

    /**
     * 订阅消息
     * @param tag 订阅标识
     * @param key 消息key
     * @param observer 消息回执
     */
    fun subscribe(tag: ComponentActivity, key: String, observer: (msg: Any?) -> Unit) { _subscribe(tag, key, observer) }
    fun subscribe(tag: Fragment, key: String, observer: (msg: Any?) -> Unit) { _subscribe(tag, key, observer) }
    // 需要手动取消
    fun subscribe(tag: String, key: String, observer: (msg: Any?) -> Unit) { _subscribe(tag, key, observer) }

    /**
     * 手动取消订阅消息
     * @param tag 订阅标识
     */
    fun unsubscribe(tag: ComponentActivity) { _unsubscribe(tag) }
    fun unsubscribe(tag: Fragment) { _unsubscribe(tag) }
    fun unsubscribe(tag: String) { _unsubscribe(tag) }

    data class TagKey(val tag: Any, val key: String) {
        fun isTk(t: Any, k: String) = isTag(t) && isKey(k)
        fun isTag(t: Any) = t==tag
        fun isKey(k: String) = k==key

        override fun toString(): String {
            return "TagKey(tag=$tag, key='$key')"
        }
    }

    /* ---------------------- */

    private fun _subscribe(tag: Any, key: String, observer: (msg: Any?) -> Unit) {
        busFilter { isTk(tag, key) }.also {
            if (it.isEmpty()) return@also
            // set效果, 最后subscribe者获得订阅权限
            it.keys.forEach { buses[it] = observer }; return
        }
        buses[TagKey(tag, key)] = observer
        if(tag is LifecycleOwner) {
            tag.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) _unsubscribe(tag)
            })
        }
    }
    private fun _unsubscribe(tag: Any) {
        busFilter { isTag(tag) }.keys.forEach { buses.remove(it) }
    }

}


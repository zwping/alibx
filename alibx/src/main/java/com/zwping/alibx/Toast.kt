package com.zwping.alibx

import android.content.Context

/**
 *
 * zwping @ 2021/11/21
 */
interface ToastInterface {

    fun Context?.showToast(msg: CharSequence?)

}

class ToastUtil : ToastInterface {
    override fun Context?.showToast(msg: CharSequence?) {
    }

    private fun showToast(){
        try {

        } catch (e: Exception) { e.printStackTrace() }
    }

}
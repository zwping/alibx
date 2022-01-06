package com.zwping.alibx.demo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.zwping.alibx.*
import com.zwping.alibx.demo.AcMain.Type.*
import com.zwping.alibx.demo.databinding.AcMainBinding

class AcMain : BaseAc<AcMainBinding>() {

    // AcMain页面复用
    private enum class Type{
        Default,
        Dialog,
    }

    private fun Context.openAcMain(enum: Enum<*>){
        open(AcMain::class.java) { extra { putString("type", enum.name) } }
    }

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private fun btn(name: String, click: (v: View)->Unit) = Button(this).apply {
        layoutParams = ViewGroup.LayoutParams(-2, -2)
        minWidth = 200F.dp2px()
        isAllCaps = false
        text = name
        setOnClickListener { click(it) }
    }

    override fun initView() {
        val type = intent.getStringExtra("type") ?: Default.name
        when(Type.valueOf(type)){
            Default -> {
                Type.values().filter { it != Default }.forEach {
                    vb.lyContainer.addView(btn(it.name) { v-> v.context.openAcMain((it)) })
                }
            }
            Dialog -> {
                vb.lyContainer.addView(btn("链式调用"){
                    IDialog.Dialog(this).show()
                })
                vb.lyContainer.addView(btn("IOS风格空样式"){
                    IDialog.DialogIOS(this).show()
                })
                vb.lyContainer.addView(btn("IOS风格默认使用"){
                    IDialog.DialogIOS(this)
                        .setTitleIOS("Title")
                        .setMessageIOS("Message")
                        .setBtnCancelIOS {  }
                        .setBtnConfirmIOS { showToast("confirm") }
                        .show()
                })
                vb.lyContainer.addView(btn("IOS风格正常使用1"){
                    IDialog.DialogIOS(this)
                        .setTitleIOS("说好的~")
                        .setBtnConfirmIOS("好的") { showToast("好的好的") }
                        .show()
                })
                vb.lyContainer.addView(btn("IOS风格正常使用2"){
                    IDialog.DialogIOS(this) { setCanceledOnTouchOutside(false); setCancelable(false) }
                        .setMessageIOS("只能通过按钮关闭")
                        .setBtnCancelIOS {  }
                        .show()
                })
            }
        }
    }
}
package com.zwping.alibx.demo

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private fun line(name: String) = LinearLayout(this).apply {
        layoutParams = ViewGroup.LayoutParams(-1, 20F.dp2px())
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1F.dp2px()).apply { weight = 1F }
            setBackgroundColor(Color.GREEN)
        })
        addView(TextView(context).apply {
            text = "$name"
            setPadding(5F.dp2px(), 0, 5F.dp2px(), 0)
        })
        addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1F.dp2px()).apply { weight = 1F }
            setBackgroundColor(Color.GREEN)
        })
    }

    class CommRecyclerView(context: Context) : RecyclerView(context) {
        fun datas(size: Int=10): MutableList<Int> {
            val data = mutableListOf<Int>()
            repeat(size) { data.add(it+1) }
            return data
        }
        val adp by lazy { AdapterQuick<Int>{ BaseViewHolder(
            TextView(it.context).apply { gravity = Gravity.CENTER; layoutParams = ViewGroup.LayoutParams(-1, 40F.dp2px()) },
            { view, entity -> view.text = "$entity" }
        ) } }
        init {
            layoutManager = LinearLayoutManager(context)
            setBackgroundColor(Color.WHITE)
            addItemDecorationLine(Color.DKGRAY, 1F.dp2px())
            adapter = adp
            adp.setData(datas())
        }
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
                vb.lyContainer.addView(line("自定义dialog"))
                vb.lyContainer.addView(btn("AlertDialog"){
                    IDialog.DialogAlert(this)
                        .setCanceledOnTouchOutside(true)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Title")
                        .setMessage("Message")
                        .setPositiveButton { showToast("拦截了默认关闭") }
                        .setNegativeButton {  }
                        .setNeutralButton { showToast("只保留取消的默认关闭"); it.dismiss() }
                        .show()
                })
                vb.lyContainer.addView(btn("IOS风格空调用"){
                    IDialog.DialogIOS(this).show()
                })
                vb.lyContainer.addView(btn("底部上升"){
                    IDialog.Dialog(this)
                        .setContentView { CommRecyclerView(it.context) }
                        .apply {
//                            disableHideAnim = true
                        }
                        .setGravity(Gravity.BOTTOM)
                        .setShowAnimator { AnimHelper.slideBottomTopIn(it) }
                        .setHideAnimator { AnimHelper.slideTopBottomOut(it) }
                        .show()
                })
                vb.lyContainer.addView(btn("链式调用"){
                    IDialog.Dialog(this).show()
                })
                vb.lyContainer.addView(line("IOS风格"))
                vb.lyContainer.addView(btn("IOS风格空调用"){
                    IDialog.DialogIOS(this).show()
                })
                vb.lyContainer.addView(btn("IOS风格"){
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
                vb.lyContainer.addView(line("原生dialog"))
                vb.lyContainer.addView(btn("AlertDialog"){
                    IDialog.DialogAlert(this)
                        .setCanceledOnTouchOutside(true)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Title")
                        .setMessage("Message")
                        .setPositiveButton { showToast("拦截了默认关闭") }
                        .setNegativeButton {  }
                        .setNeutralButton { showToast("只保留取消的默认关闭"); it.dismiss() }
                        .show()
                })
                vb.lyContainer.addView(btn("DatePicker"){
                    IDialog.DialogDatePicker(this,
                        { year, month, dayOfMonth -> showToast("$year-$month-$dayOfMonth") })
                        .also { it.setCanceledOnTouchOutside(false) }
                        .show()
                })
                vb.lyContainer.addView(btn("TimePicker"){
                    IDialog.DialogTimePicker(this,
                        { hourOfDay, minute -> showToast("$hourOfDay-$minute") })
                        .also { it.setCanceledOnTouchOutside(false) }
                        .show()
                })
                vb.lyContainer.addView(btn("Progress"){
                    IDialog.DialogProgress(this, "下载...", false)
                        .setProgressBarColor(Color.DKGRAY, Color.RED, Color.BLUE, 3F.dpToPx(), 3F.dp2px())
                        .also {
                            it.max = 100
                            it.incrementSecondaryProgressBy(60)
                            it.incrementProgressBy(10)
                        }
                        .show()
                })
                vb.lyContainer.addView(btn("Progress-Loading"){
                    IDialog.DialogProgress(this, "Loading...")
                        .setIndeterminateDrawable(Color.RED)
                        .show()
                })
                vb.lyContainer.addView(btn("ListItems"){
                    IDialog.DialogItems(
                        this,
                        mutableListOf(1, 2, 3, 4),
                        {dialog, index, item ->  showToast("$index, $item") }
                    )
                        .setTitle("列表")
                        .setCanceledOnTouchOutside(false)
                        .setCancelable(true)
                        .setOnDismissListener { showToast("dismiss") }
                        .show()
                })
                vb.lyContainer.addView(btn("单选"){
                    IDialog
                        .DialogSingleChoiceBuilder(
                            this,
                            mutableListOf(1, 2, 3, 4),
                            { dialog, index, item ->
                                showToast("$index $item")
                            })
                        .setCheckItem(0)
                        .setTitle("单选")
                        .setCanceledOnTouchOutside(false)
                        .setCancelable(true)
                        .setOnDismissListener { showToast("dismiss") }
                        .show()
                })
                vb.lyContainer.addView(btn("多选"){
                    IDialog
                        .DialogMultiChoiceBuilder(
                            this,
                            mutableListOf(1, 2, 3, 4),
                            { dialog, items ->
                                showToast("$items")
                            })
                        .setCheckItems(booleanArrayOf(true, false, false, false))
                        .setTitle("多选")
                        .setIcon(R.mipmap.ic_launcher)
                        .setCanceledOnTouchOutside(false)
                        .setCancelable(true)
                        .setOnDismissListener { showToast("dismiss") }
                        .show()
                })
            }
        }
    }
}
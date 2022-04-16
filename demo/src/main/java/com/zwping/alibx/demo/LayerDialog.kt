package com.zwping.alibx.demo

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import com.zwping.alibx.*
import com.zwping.alibx.demo.databinding.DialogCommentBinding
import com.zwping.alibx.demo.databinding.DialogLoginBinding

/**
 *
 * zwping @ 2022/4/15
 */
class LayerDialog(val acMain: AcMain) {
    init {
        acMain.apply {
            vb.lyContainer.addView(line("自定义dialog"))
            vb.lyContainer.addView(btn("输入框-发评论"){
                IDialog.Dialog(this, {
                    val vb = DialogCommentBinding.inflate(rootLayout.getLayoutInflater(), rootLayout, false)
                    // vb.root.layoutParams.width = getScreenWidth()-(30F*2).dp2px()
                    setContentView(vb.root)
                    setLightBar(false)
                    Bar.setStatusBarDarkMode(window, false)
                    Bar.setNavBarDarkMode(window, true)
                    Bar.setNavBarColor(window, Color.WHITE)
                    Bar.addMarginBottomNavBarHeight(rootLayout)

                    onOutSideClickListener {
                        if (Bar.isKeyboardVisible(rootLayout)) Bar.hideKeyboard(rootLayout)
                        else dismiss()
                    }

                    showAnimator = { AnimHelper.slideBottomTopIn(it) }
                    hideAnimator = { AnimHelper.slideTopBottomOut(it) }

                    setInputFocus(vb.etComment)
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                    Bar.registerKeyboardChangeListener(window, progress = { show, high ->
                        rootLayout.translationY = high*1F
                    })
                })
                    .show()
            })
            vb.lyContainer.addView(btn("输入框-登录") {
                IDialog.Dialog(this, {
                    val vb = DialogLoginBinding.inflate(rootLayout.getLayoutInflater(), rootLayout, false)
                    setContentView(vb.root)

                    setInputFocus(vb.etAcc)
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                    Bar.registerKeyboardChangeListener(window, progress = { show, high ->
                        rootLayout.translationY = high/2F
                    })
                })
                    .show()
            })
            vb.lyContainer.addView(btn("右侧滑入"){
                IDialog.Dialog(this)
                    .setContentView { AcMain.CommRecyclerView(it.context).apply {
                        layoutParams = ViewGroup.LayoutParams(300F.dp2px(), -1)
                        adp.setData(datas(30))
                    } }
                    .setLightBar(false)
                    .setGravity(Gravity.RIGHT)
                    .setShowAnimator { AnimHelper.slideRightLeftIn(it) }
                    .setHideAnimator { AnimHelper.slideLeftRightOut(it) }
                    .show()
            })
            vb.lyContainer.addView(btn("底部上升"){
                IDialog.Dialog(this, {
                    rootLayout.addMarginBottomNavBarHeight()
                    Bar.setNavBarColor(window, Color.WHITE)
                    setLightBar(false)
                    Bar.setStatusBarDarkMode(window, false)
                    Bar.setNavBarDarkMode(window, true)
                })
                    .setContentView { AcMain.CommRecyclerView(it.context) }
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
                    .setTitleIOS("电池电量不足")
                    .setMessageIOS("还剩20%电量")
                    .setBtnCancelIOS("关闭") {  }
                    .setBtnConfirmIOS("低电量模式") { showToast("confirm") }
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
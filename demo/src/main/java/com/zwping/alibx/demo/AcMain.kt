package com.zwping.alibx.demo

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zwping.alibx.*
import com.zwping.alibx.Bar
import com.zwping.alibx.demo.AcMain.Type.*
import com.zwping.alibx.demo.databinding.AcMainBinding
import com.zwping.alibx.demo.databinding.DialogCommentBinding
import com.zwping.alibx.demo.databinding.DialogLoginBinding

class AcMain : BaseAc<AcMainBinding>() {

    // AcMain页面复用
    private enum class Type{
        Default,
        Dialog,
        FormInput,
        Bar,
    }

    private fun Context.openAcMain(enum: Enum<*>){
        open(AcMain::class.java) { extra { putString("type", enum.name) } }
    }

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private fun btn(name: CharSequence, click: (v: Button)->Unit) = Button(this).apply {
        layoutParams = ViewGroup.LayoutParams(-2, -2)
        minWidth = 200F.dp2px()
        isAllCaps = false
        text = name
        setOnClickListener { click(this) }
        backgroundTintList = ColorStateList2((0xffdddddd).toInt()) { unEnabled((0xff999999).toInt()) }.create()
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
            addItemDecorationLine(Color.DKGRAY, 0.5F.dp2px())
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
                        .setContentView { CommRecyclerView(it.context).apply {
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
                        .setContentView { CommRecyclerView(it.context) }
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
            FormInput -> {
                // 表单输入良好的用户体验:
                // 外层滚动布局
                // window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                // isSingleLine = true; imeOptions = EditorInfo.IME_ACTION_NEXT // 连续输入
                val p1_1 = vb.lyContainer.parent.parent as ViewGroup
                (vb.lyContainer.parent as NestedScrollView).removeAllViews()
                p1_1.addView(vb.lyContainer)
                vb.lyContainer.addView(line("最外层布局不同"))
                val container = LinearLayout(vb.lyContainer.context).apply { setBackgroundColor(Color.DKGRAY);layoutParams = ViewGroup.LayoutParams(-1,-1); orientation = LinearLayout.VERTICAL }
                vb.lyContainer.addView(btn(SpanUtils().append("LinearLayout").setStrikethrough().create()) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    container.removeAllViews()
                    repeat(20) { i-> container.addView(HolderEt(container).let { it.view.hint="输入框${i}"; it.itemView }) }
                })
                vb.lyContainer.addView(btn("NestedScrollView") {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    container.removeAllViews()
                    val sv = NestedScrollView(container.context)
                    val cont =  LinearLayout(vb.lyContainer.context).apply { layoutParams = ViewGroup.LayoutParams(-1,-1); orientation = LinearLayout.VERTICAL }
                    sv.addView(cont)
                    repeat(20) { i-> cont.addView(HolderEt(container).let { it.view.hint="输入框${i}"; it.itemView }) }
                    container.addView(sv)
                })
                vb.lyContainer.addView(btn("RecyclerView") {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    container.removeAllViews()
                    val rv = RecyclerView(container.context).apply { layoutManager = LinearLayoutManager(container.context) }
                    rv.adapter = AdapterQuick { HolderEt(it) }.apply { setData(mutableListOf<Int>().apply { repeat(20) { add(it) } }) }
                    container.addView(rv)
                })
                vb.lyContainer.addView(container)
            }
            Type.Bar -> {
                vb.lyContainer.addView(line("常见bar控制"))
                vb.lyContainer.addView(btn("全屏[×]") {
                    var state = it.text.toString().contains("√")
                    state = !state
                    it.text = "全屏[${if (state) "√" else "×"}]"
                    setFullScreen(state)
                })
                vb.lyContainer.addView(btn("StatusBar DarkMode[默认]") {
                    var state = it.text.toString().contains("√")
                    state = !state
                    it.text = "StatusBar DarkMode[${if (state) "√" else "×"}]"
                    setStatusBarDarkMode(state)
                })
                vb.lyContainer.addView(btn("NavBar DarkMode[默认]") {
                    var state = it.text.toString().contains("√")
                    state = !state
                    it.text = "NavBar DarkMode[${if (state) "√" else "×"}]"
                    setNavBarDarkMode(state)
                })
                vb.lyContainer.addView(btn("隐藏状态栏[×]") {
                    var state = it.text.toString().contains("√")
                    state = !state
                    it.text = "隐藏状态栏[${if (state) "√" else "×"}]"
                    setStatusBarHide(state)
                })
                vb.lyContainer.addView(line("只可activity设置沉浸式, fm只能操控bar"))
                vb.lyContainer.addView(btn("immersive") { immersive(); it.isEnabled = false })
            }
        }
    }

    private class HolderEt(parent: ViewGroup): BaseViewHolder<Int, EditText>(
        EditText(parent.context).apply {
            /*
            actionNone：[没有动作]（有下个输入框则跳入，否则收起软键盘）
            actionUnspecified：[未指定]/下一项（有下个输入框则跳入，否则收起软键盘）
            actionPrevious：上一项（光标跳到上一个输入框，如果已是第一个则跳到最后一个输入框）
            actionNext：下一项（光标跳到下一个输入框，如果已是最后一个则跳到第一个输入框）
            actionDone：完成（收起软键盘）
            actionGo：前往（有下个输入框则跳入，否则收起软键盘）
            actionSearch：搜索（有下个输入框则跳入，否则收起软键盘）
            actionSend：发送（有下个输入框则跳入，否则收起软键盘）
             */
            isSingleLine = true; imeOptions = EditorInfo.IME_ACTION_NEXT // 连续输入
            setOnEditorActionListener { textView, i, keyEvent ->
                if (textView.hint.toString().contains("19")) {
                    showToast("最后一个了, 手动提交表单")
                    return@setOnEditorActionListener true
                }
                false
            }
        },
        { view, entity ->
            view.hint = "输入框${entity}"
        }
    )
}
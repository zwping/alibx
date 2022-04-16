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
import kotlin.random.Random

class AcMain : BaseAc<AcMainBinding>() {

    // AcMain页面复用
    private enum class Type{
        Default,
        Dialog,
        FormInput,
        Bar,
        TabLayout,
        RecyclerView,
        ImageLoader,
        ProgressBar,
    }

    private fun Context.openAcMain(enum: Enum<*>){
        open(AcMain::class.java) { extra { putString("type", enum.name) } }
    }

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    override fun initView() {
        val type = intent.getStringExtra("type") ?: Default.name
        when(Type.valueOf(type)){
            Default -> {
                Type.values().filter { it != Default }.forEach {
                    vb.lyContainer.addView(btn(it.name) { v-> v.context.openAcMain((it)) })
                }
            }
            Dialog -> LayerDialog(this)
            FormInput -> LayerFormInput(this)
            Type.Bar -> LayerBar(this)
            TabLayout -> LayerTabLayout(this)
            RecyclerView -> LayerRecyclerView(this)
            Type.ImageLoader -> LayerImageLoader(this)
            Type.ProgressBar -> LayerProgressBar(this)
        }
    }

    fun btn(name: CharSequence, click: (v: Button)->Unit) = Button(this).apply {
        layoutParams = ViewGroup.LayoutParams(-2, -2)
        minWidth = 200F.dp2px()
        isAllCaps = false
        text = name
        setOnClickListener { click(this) }
        backgroundTintList = ColorStateList2((0xffdddddd).toInt()) { unEnabled((0xff999999).toInt()) }.create()
    }

    fun line(name: String) = LinearLayout(this).apply {
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

    fun randomColor() = Color.argb(255, Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255))
    fun randomData(size: Int = 10) = mutableListOf<Int>().apply { for (i in 0 until size) add(i) }

    class CommRecyclerView(context: Context) : RecyclerView(context) {
        fun datas(size: Int=10): MutableList<Int> {
            val data = mutableListOf<Int>()
            repeat(size) { data.add(it+1) }
            return data
        }
        val adp by lazy { BaseAdapterQuick<Int>{ BaseViewHolder(
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

    class HolderEt(parent: ViewGroup): BaseViewHolder<Int, EditText>(
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
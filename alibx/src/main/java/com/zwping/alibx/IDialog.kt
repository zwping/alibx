package com.zwping.alibx

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.*
import android.view.animation.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.*


/**
 *
 * zwping @ 2021/12/8
 */

class IDialog(private val alertDialog: AppCompatDialog?=null): AppCompatDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        return alertDialog ?: super.onCreateDialog(savedInstanceState)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try { super.show(manager, tag) } // 暂时catch住
        catch (e: IllegalStateException) { e.printStackTrace() }
    }

    /**
     * AppCompatDialog复写, 支持继承/快速调用使用
     * @param block 快速调用具体实现操作
     */
    open class Dialog(context: Context,
                      block: Dialog.() -> Unit = { },
                      themeResId: Int = DialogDefaultTheme) :
            AppCompatDialog(context, themeResId), LifecycleEventObserver {

        private val decorView: View?
            get() = window?.decorView
        private val wlp: WindowManager.LayoutParams?
            get() = window?.attributes
        private val animView: View?
            get() = customView ?: decorView

        var customView: View? = null
            private set
        var cancelabled = true              // 返回键 & 空白区域是否可以关闭
            private set(value) { field = value; canceledOnTouchOutSide = value }
        var canceledOnTouchOutSide = true   // 空白区域是否可以关闭
            private set(value) { logd(value, cancelabled); if (cancelabled) field = value }
        var canceledOnTouchOutSideSuper = false // 更强空白区域是否可以关闭, decorView两侧空白点击也可以关闭
            private set
        /*** 显示动画, 辅助类快捷调用[AnimHelper] ***/
        var showAnimator: ((View)->AnimatorSet)? = null
        var hideAnimator: ((View)->AnimatorSet)? = null

        init {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setGravity(Gravity.CENTER)
            block.invoke(this)
            // fixCancelabled()
        }


        private fun fixCanceledOnTouchOutSide() {
            var last = 0L
            decorView?.also {
                if (it is ViewGroup)   // 屏幕点击事件, 转移dialog原有canceledOnTouchOutSide功能
                    repeat(it.childCount) { i -> it.getChildAt(i).setOnClickListener {  } }
                it.setOnTouchListener { _, event ->
                    if (!canceledOnTouchOutSideSuper) false
                    when(event?.action) {
                        MotionEvent.ACTION_DOWN -> last = System.currentTimeMillis()
                        MotionEvent.ACTION_UP -> if (System.currentTimeMillis() - last < 300) cancel()
                    }
                    true
                }
            }
        }
        private fun fixCancelabled() {
            setOnKeyListener { _, keyCode, _ ->
                keyCode == KeyEvent.KEYCODE_BACK && !cancelabled
            }
        }



        /*** 自定义view
         *  [setContentView]在show之后调用
         * ***/
        override fun setContentView(view: View) {
            super.setContentView(view)
            customView = view
            resetThemeCfg()
        }

        fun setGravity(gravity: Int) {
            window?.setGravity(gravity)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }
        override fun onStart() {
            super.onStart()
            animView?.post { showAnimator?.invoke(animView!!)?.start() }
        }

        private var lockDismiss = false
        override fun dismiss() {
            if (lockDismiss) return
            if (animView != null && hideAnimator != null) {
                hideAnimator!!(animView!!).also {
                    it.addListener(onEnd = { lockDismiss = false; super.dismiss() })
                }.start()
                lockDismiss = true
                return
            }
            super.dismiss()
        }

        /*** 自定义view时, 恢复部分theme设置 ***/
        private fun resetThemeCfg() {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.CENTER)
            wlp?.also {
                it.width = WindowManager.LayoutParams.MATCH_PARENT
                it.height = WindowManager.LayoutParams.WRAP_CONTENT
                window?.attributes = it
            }
            decorView?.setPadding(0, 0, 0, 0)
        }

        /*** 返回键 & 空白区域是否可以关闭, show之前调用无效 ***/
        override fun setCancelable(flag: Boolean) {
            super.setCancelable(flag); cancelabled = flag
        }
        /*** 空白区域是否可以关闭, show之前调用无效 ***/
        override fun setCanceledOnTouchOutside(cancel: Boolean) {
            super.setCanceledOnTouchOutside(cancel); canceledOnTouchOutSide = cancel
        }
        override fun show() { create(); super.show() }
        /**
         * 显示DialogFragment, 支持[onStateChanged], 同时支持[IDialogImpl.onStateChanged]
         */
        @Deprecated("未调试")
        fun show(fragmentManager: FragmentManager): IDialog {
            create()
            return IDialog(this).also {
                it.show(fragmentManager, javaClass.simpleName)
                // it.isCancelable = false
                it.lifecycle.removeObserver(this)
                it.lifecycle.addObserver(this)
            }
        }
        /*** 生命周期感知, 继承使用 ***/
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {  }
    }

//    final class DialogIOS(
//        context: Context,
//        block: DialogIOS.() -> Unit = {},
//    ) : Dialog(context) {
//
//        private val view by lazy { IOS(context) }
//
//        init {
//            setView(view)
//            val dplr = (50*Resources.getSystem().displayMetrics.density+0.5F).toInt()
////            impl.setViewMarginLR(dplr, dplr)
//            view.title.visibility = View.GONE
//            view.message.visibility = View.GONE
//            view.btnConfirm.visibility = View.GONE
//            view.btnCancel.visibility = View.GONE
//            view.line.visibility = View.GONE
//        }
//
//        fun setIOSTitle(txt: CharSequence): DialogIOS {
//            view.title.visibility = View.VISIBLE; view.title.text = txt; return this
//        }
//        fun setIOSMessage(txt: CharSequence): DialogIOS {
//            view.message.visibility = View.VISIBLE; view.message.text = txt; return this
//        }
//        fun setIOSBtnCancel(txt: CharSequence="取消", lis: (DialogIOS)->Unit={}): DialogIOS {
//            view.line.visibility = View.VISIBLE
//            view.btnCancel.also {
//                it.visibility = View.VISIBLE; it.text = txt
//                it.setOnClickListener { dismiss(); lis(this) }
//            }
//            return this
//        }
//        fun setIOSBtnConfirm(txt: CharSequence="确认", lis: (DialogIOS)->Unit): DialogIOS {
//            view.btnConfirm.also {
//                it.visibility = View.VISIBLE; it.text = txt
//                it.setOnClickListener { lis(this) }
//            }
//            return this
//        }
//
//        private class IOS @JvmOverloads constructor(
//            context: Context, attrs: AttributeSet? = null
//        ) : CardView(context, attrs) {
//
//            val title by lazy { TextView(context).apply {
//                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//                dp05*40.also{ setPadding(it, 0, it, 0) }
//                setTextColor(Color.BLACK); textSize = 18F
//                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
//            } }
//            val message by lazy { TextView(context).apply {
//                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { it.topMargin = dp05*5 }
//                dp05*40.also{ setPadding(it, 0, it, 0) }
//                setTextColor(Color.BLACK); textSize = 15F
//            } }
//            val btnConfirm by lazy { TextView(context).apply {
//                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT).apply { weight=1F }
//                background = StateListDrawable().apply {
//                    addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable((0x1a000000).toInt()))
//                    addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
//                }
//                setTextColor((0xff2e7cf7).toInt()); textSize = 16F
//                gravity=Gravity.CENTER; text = "确认"
//            }  }
//            val btnCancel by lazy { TextView(context).apply {
//                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT).apply { weight=1F }
//                background = StateListDrawable().apply {
//                    addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable((0x1a000000).toInt()))
//                    addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
//                }
//                setTextColor((0xff2e7cf7).toInt()); textSize = 16F
//                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
//                gravity=Gravity.CENTER; text = "取消"
//            }  }
//            val line by lazy { View(context).apply {
//                setBackgroundColor((0xffb3b3b3).toInt())
//                layoutParams = LinearLayout.LayoutParams(dp05, LayoutParams.MATCH_PARENT)
//            } }
//
//            val dp05 = (0.5*Resources.getSystem().displayMetrics.density+0.5F).toInt()
//            val dpRadius = 16*Resources.getSystem().displayMetrics.density+0.5F
//            val dpPadding = (26*Resources.getSystem().displayMetrics.density+0.5F).toInt()
//            val dpBottomHeight = (50*Resources.getSystem().displayMetrics.density+0.5F).toInt()
//
//            init {
//                setCardBackgroundColor((0xe6f9f9f9).toInt())
//                radius = dpRadius; elevation = 0F
//                val lyRoot = LinearLayout(context).apply {
//                    orientation = LinearLayout.VERTICAL
//                    gravity = Gravity.CENTER
//                    setPadding(0, dpPadding, 0, 0)
//                    addView(title)
//                    addView(message)
//                    addView(View(context).also {
//                        it.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp05).also { it.topMargin = dpPadding }
//                        it.setBackgroundColor((0xffb3b3b3).toInt())
//                    })
//                    addView(LinearLayout(context).also {
//                        it.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpBottomHeight)
//                        it.orientation = LinearLayout.HORIZONTAL
//                        it.addView(btnCancel)
//                        it.addView(line)
//                        it.addView(btnConfirm)
//                    })
//                }
//                addView(lyRoot)
//            }
//        }
//
//    }

    open class DialogBottomSheet(context: Context,
                                 theme: Int = 0) :
            BottomSheetDialog(context, theme) {
        // http://blog.csdn.net/yanzhenjie1003/article/details/51938400
    }


    /**
     * 日期选择器
     */
    open class DialogDatePicker(context: Context,
                                lis: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
                                year: Int = Calendar.getInstance().get(Calendar.YEAR),
                                monthOfYear: Int = Calendar.getInstance().get(Calendar.MONTH),
                                dayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                                themeResId: Int = DialogDefaultTheme) :
            DatePickerDialog(context, themeResId,
                OnDateSetListener { _, y, month, dm -> lis(y, month+1, dm) },
                year, monthOfYear, dayOfMonth), LifecycleEventObserver {

        override fun updateDate(year: Int, month: Int, dayOfMonth: Int) { // 修改当前选中日期, month-1
            super.updateDate(year, month, dayOfMonth)
        }
        override fun getDatePicker(): android.widget.DatePicker {
            return super.getDatePicker()
        }
        fun setMaxDate(date: Long): DialogDatePicker {
            datePicker.maxDate = date; return this
        }
        fun setMinDate(date: Long): DialogDatePicker {
            datePicker.minDate = date; return this
        }
//        override fun show() { super.show() }
//        private var idialog: IDialog? = null
//        fun show(fragmentManager: FragmentManager): IDialog {
//            val idialog = IDialog(this)
//            idialog.show(fragmentManager, javaClass.simpleName)
//            idialog.lifecycle.addObserver(this)
//            return idialog
//        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            if (event == Lifecycle.Event.ON_DESTROY) idialog?.lifecycle?.removeObserver(this)
        }
    }

    /**
     * 时间选择器
     * @param is24HourView [DateFormat.is24HourFormat]
     */
    open class DialogTimePicker(context: Context?,
                                lis: (hourOfDay: Int, minute: Int) -> Unit,
                                hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                                minute: Int = Calendar.getInstance().get(Calendar.MINUTE),
                                is24HourView: Boolean = true,
                                themeResId: Int = DialogDefaultTheme):
            TimePickerDialog(context, themeResId,
                OnTimeSetListener { _, hd, m -> lis(hd, m)  },
                hourOfDay, minute, is24HourView), LifecycleEventObserver {

        override fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
            super.updateTime(hourOfDay, minuteOfHour)
        }

//        override fun show() { super.show() }
//        private var idialog: IDialog? = null
//        fun show(fragmentManager: FragmentManager): IDialog {
//            val idialog = IDialog(this)
//            idialog.show(fragmentManager, javaClass.simpleName)
//            idialog.lifecycle.addObserver(this)
//            return idialog
//        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            if (event == Lifecycle.Event.ON_DESTROY) idialog?.lifecycle?.removeObserver(this)
        }
    }

    /**
     * 进度
     * @param loadingMode 转圈 / 进度条
     */
    open class DialogProgress(context: Context?,
                              message: CharSequence,
                              loadingMode: Boolean = true,
                              theme: Int = DialogDefaultTheme) :
            ProgressDialog(context, theme), LifecycleEventObserver {

        init {
            setMessage(message)
            setProgressStyle(if (loadingMode) STYLE_SPINNER else STYLE_HORIZONTAL)
            setCanceledOnTouchOutside(false)
        }

        // loading mode 背景
        override fun setIndeterminateDrawable(d: Drawable?) { super.setIndeterminateDrawable(d) }
        // 进度mode背景
        override fun setProgressDrawable(d: Drawable?) { super.setProgressDrawable(d) }
        // 进度最大值
        override fun setMax(max: Int) { super.setMax(max) }
        // 进度值
        override fun incrementProgressBy(diff: Int) { super.incrementProgressBy(diff) }
        // 第二进度值
        override fun incrementSecondaryProgressBy(diff: Int) { super.incrementSecondaryProgressBy(diff) }
        // 进度值的格式, 默认 %1d/%2d / 直接输出值 String.format("%.2fkb/%.2fM, cur, total)
        override fun setProgressNumberFormat(format: String?) { super.setProgressNumberFormat(format) }
        // 进度百分比 null->隐藏
        override fun setProgressPercentFormat(format: NumberFormat?) { super.setProgressPercentFormat(format) }

//        override fun show() { super.show() }
//        private var idialog: IDialog? = null
//        fun show(fragmentManager: FragmentManager): IDialog {
//            val idialog = IDialog(this)
//            idialog.show(fragmentManager, javaClass.simpleName)
//            idialog.lifecycle.addObserver(this)
//            return idialog
//        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            if (event == Lifecycle.Event.ON_DESTROY) idialog?.lifecycle?.removeObserver(this)
        }
    }

    /**
     * 简单列表, 支持T Entity & bindView
     */
    open class DialogItemsBuilder<T>(context: Context,
                                     val datas: MutableList<T?>,
                                     themeResId: Int = DialogDefaultTheme) :
            AlertDialog.Builder(context, themeResId), LifecycleEventObserver {

        private val opt by lazy { IDialogImpl(create2()) }
        fun setimpl(block: (IDialogImpl<AlertDialog>) -> Unit): DialogItemsBuilder<T>{
            block(opt); return this
        }

        private var itemsClickLis: (dialog: DialogInterface, index: Int, item: T?) -> Unit = {_,_,_->}
        fun setItemsClickLis(lis: (dialog: DialogInterface, index: Int, item: T?) -> Unit): DialogItemsBuilder<T> {
            this.itemsClickLis = lis; return this
        }
        private var bindView: (tv: TextView?, i: Int, it: T?) -> Unit = { tv, _, it -> tv?.text = "$it"; tv?.setTextColor(Color.BLACK) }
        fun setBindView(bindView: (tv: TextView?, i: Int, it: T?) -> Unit): DialogItemsBuilder<T> {
            this.bindView = bindView; return this
        }

        private fun create2(): AlertDialog{
            val lis = { dialog: DialogInterface, which: Int ->
                itemsClickLis.invoke(dialog, which, datas[which])
            }
            val dialog = setItems(datas.map { "$it" }.toTypedArray(), lis)
                .create()
            dialog.listView.post {
                repeat(dialog.listView.childCount) { i -> // 适配DialogDefaultTheme
                    bindView(dialog.listView.getChildAt(i).findViewById(android.R.id.text1), i, datas[i])
                }
            }
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

        private var idialog: IDialog? = null
        override fun show(): AlertDialog { return opt.init().also { it.show(); opt.resetCLickListener() } }
        fun show(fragmentManager: FragmentManager): IDialog {
            val idialog = IDialog(opt.init())
            idialog.show(fragmentManager, javaClass.simpleName)
            idialog.lifecycle.addObserver(this)
            return idialog
        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when(event) {
                Lifecycle.Event.ON_RESUME -> opt.resetCLickListener()
                Lifecycle.Event.ON_DESTROY -> idialog?.lifecycle?.removeObserver(this)
            }
        }
    }

    /**
     * 简单单选列表, 支持T Entity & bindView
     */
    open class DialogSingleChoiceBuilder<T>(context: Context,
                                            val datas: MutableList<T?>,
                                            themeResId: Int = DialogDefaultTheme) :
            AlertDialog.Builder(context, themeResId), LifecycleEventObserver {

        var datasIsCheck: T? = null
            private set

        private val opt by lazy { IDialogImpl(create2()) }
        fun setimpl(block: (IDialogImpl<AlertDialog>) -> Unit): DialogSingleChoiceBuilder<T>{
            block(opt); return this
        }

        private var checkItem: Int = -1
            set(value) { if (value > 0 && value < datas.size) { field=value; datasIsCheck=datas[value] } }
        fun setCheckItem(checkItem: Int): DialogSingleChoiceBuilder<T> {
            this.checkItem = checkItem; return this
        }
        private var singleChoiceClickLis: (dialog: DialogInterface, index: Int, item: T?) -> Unit = {_,_,_->}
        fun setSingleChoiceClickLis(lis: (dialog: DialogInterface, index: Int, item: T?) -> Unit): DialogSingleChoiceBuilder<T> {
            this.singleChoiceClickLis = lis; return this
        }
        private var bindView: (tv: TextView?, i: Int, it: T?) -> Unit = { tv, _, it -> tv?.text = "$it"; tv?.setTextColor(Color.BLACK) }
        fun setBindView(bindView: (tv: TextView?, i: Int, it: T?) -> Unit): DialogSingleChoiceBuilder<T> {
            this.bindView = bindView; return this
        }
        fun setPositiveButton(txt: CharSequence="确认", lis: (dialog: AlertDialog, builder: DialogSingleChoiceBuilder<T>) -> Unit): DialogSingleChoiceBuilder<T> {
            opt.setPositiveButton(txt) { lis(it, this) }; return this
        }

        private fun create2(): AlertDialog{
            val lis = { dialog: DialogInterface, which: Int ->
                datasIsCheck = datas[which]
                singleChoiceClickLis.invoke(dialog, which, datas[which])
            }
            val dialog = setSingleChoiceItems(datas.map { "$it" }.toTypedArray(), checkItem, lis)
                .create()
            dialog.listView.post {
                repeat(dialog.listView.childCount) { i -> // 适配DialogDefaultTheme
                    bindView(dialog.listView.getChildAt(i).findViewById(android.R.id.text1), i, datas[i])
                }
            }
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

//        private var idialog: IDialog? = null
//        override fun show(): AlertDialog { return opt.init().also { it.show(); opt.resetCLickListener() } }
//        fun show(fragmentManager: FragmentManager): IDialog {
//            val idialog = IDialog(opt.init())
//            idialog.show(fragmentManager, javaClass.simpleName)
//            idialog.lifecycle.addObserver(this)
//            return idialog
//        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            when(event) {
//                Lifecycle.Event.ON_RESUME -> opt.resetCLickListener()
//                Lifecycle.Event.ON_DESTROY -> idialog?.lifecycle?.removeObserver(this)
//            }
        }
    }

    /**
     * 简单多选列表, 支持T Entity & bindView
     */
    open class DialogMultiChoiceBuilder<T>(context: Context,
                                           val datas: MutableList<T?>,
                                           themeResId: Int = DialogDefaultTheme) :
            AlertDialog.Builder(context, themeResId), LifecycleEventObserver {

        /*** 选中的数据 ***/
        var datasIsCheck: MutableList<T?> = mutableListOf()
            private set

        private val impl by lazy { IDialogImpl(create2()) }
        fun setImpl(implBlock: (IDialogImpl<AlertDialog>) -> Unit): DialogMultiChoiceBuilder<T>{
            implBlock(impl); return this
        }

        private var checkItems: BooleanArray? = null
            set(value) {
                if (value?.size ?: 0 == datas.size) {
                    field = value; value?.forEachIndexed { index, it -> if (it) datasIsCheck.add(datas[index]) }
                }
            }
        fun setCheckItems(checkItems: BooleanArray): DialogMultiChoiceBuilder<T>{
            this.checkItems = checkItems; return this
        }
        private var multiChoiceClickLis: (dialog: DialogInterface, index: Int, isChecked: Boolean, item: T?) -> Unit = {_,_,_,_->}
        fun setMultiChoiceClickLis(lis: (dialog: DialogInterface, index: Int, isChecked: Boolean, item: T?) -> Unit): DialogMultiChoiceBuilder<T> {
            this.multiChoiceClickLis = lis; return this
        }
        private var bindView: (tv: TextView?, i: Int, it: T?) -> Unit = { tv, _, it -> tv?.text = "$it"; tv?.setTextColor(Color.BLACK) }
        fun setBindView(bindView: (tv: TextView?, i: Int, it: T?) -> Unit): DialogMultiChoiceBuilder<T> {
            this.bindView = bindView; return this
        }
        fun setPositiveButton(txt: CharSequence="确认", lis: (dialog: AlertDialog, builder: DialogMultiChoiceBuilder<T>) -> Unit): DialogMultiChoiceBuilder<T> {
            impl.setPositiveButton(txt) { lis(it, this) }; return this
        }

        private fun create2(): AlertDialog{
            val lis = { dialog: DialogInterface, which: Int, isChecked: Boolean ->
                if (isChecked) datasIsCheck.add(datas[which])
                else datasIsCheck.remove(datas[which])
                multiChoiceClickLis.invoke(dialog, which, isChecked, datas[which])
            }
            val dialog = setMultiChoiceItems(datas.map { "$it" }.toTypedArray(), checkItems, lis)
                .create()
            dialog.listView.post {
                repeat(dialog.listView.childCount) { i -> // 适配DialogDefaultTheme
                    bindView(dialog.listView.getChildAt(i).findViewById(android.R.id.text1), i, datas[i])
                }
            }
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

//        private var idialog: IDialog? = null
//        override fun show(): AlertDialog { return impl.init().also { it.show(); impl.resetCLickListener() } }
//        fun show(fragmentManager: FragmentManager): IDialog {
//            val idialog = IDialog(impl.init())
//            idialog.show(fragmentManager, javaClass.simpleName)
//            idialog.lifecycle.addObserver(this)
//            return idialog
//        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            when(event) {
//                Lifecycle.Event.ON_RESUME -> impl.resetCLickListener()
//                Lifecycle.Event.ON_DESTROY -> idialog?.lifecycle?.removeObserver(this)
//            }
        }
    }

    companion object {
        /**
         *  [android.app.AlertDialog.THEME_TRADITIONAL] -> pre-Holo, android1.0风格, 点击切换&输入
         *  [android.app.AlertDialog.THEME_HOLO_DARK] -> dark 滚轮切换&输入
         *  [android.app.AlertDialog.THEME_HOLO_LIGHT] ->
         *  [android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK] -> 圆角卡片式
         *  [android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT]
         *  [android.R.style.Theme_Material_Dialog_Alert] -> 直角卡片式
         *  [android.R.style.Theme_Material_Light_Dialog_Alert]
         *  [android.R.style.Theme_DeviceDefault_Dialog_Alert] -> 圆角卡片式
         *  [android.R.style.Theme_DeviceDefault_Light_Dialog_Alert]
         */
        private val DialogDefaultTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
            else android.R.style.Theme_Material_Light_Dialog_Alert
    }
}

/**
 * [IDialog]实现类
 */
class IDialogImpl<T: AlertDialog>(private val dialog: T): LifecycleEventObserver {

    private val window: Window? by lazy { dialog.window }
    private val decorView: View? by lazy { window?.decorView }
    private val layoutParams: WindowManager.LayoutParams? by lazy { window?.attributes }

    val context by lazy { dialog.context }
    // 声明周期感知上传至父类
    private var onInitLifecycleStateChanged: ((source: LifecycleOwner, event: Lifecycle.Event)->Unit)? = null
    var canceledOnTouchOutSide = true   // 空白区域是否可以关闭
        set(value) { if (!isShowLock && cancelabled) field = value } // isShowLock 在dialogFragment中失效
    var cancelabled = true              // 返回键 & 空白区域是否可以关闭
        set(value) { if (!isShowLock) { field = value; canceledOnTouchOutSide = value} }
    var animView: View? = null                      // 记录custom view, 在显示隐藏动画中使用
        private set
        get() = field ?: decorView
    var showAnimator: ((View)->AnimatorSet)? = null
    var hideAnimator: ((View)->AnimatorSet)? = null

    /**
     * @param changed [IDialogImpl.show]套入DialogFragment显示, dialog支持Lifecycle, [changed]透传至use类
     */
    fun init(changed: ((source: LifecycleOwner, event: Lifecycle.Event)->Unit)?=null): T {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        onInitLifecycleStateChanged = changed
        var last = 0L
        val dv = decorView
        if (dv is ViewGroup) { // 屏幕点击事件, 转移dialog原有canceledOnTouchOutSide功能
            repeat(dv.childCount) { dv.getChildAt(it).setOnClickListener {  } }
        }
        decorView?.setOnTouchListener { _, event ->
            when(event?.action) {
                MotionEvent.ACTION_DOWN -> last = System.currentTimeMillis()
                MotionEvent.ACTION_UP ->
                    if (canceledOnTouchOutSide && System.currentTimeMillis() - last < 300) dialog.cancel()
            }
            true
        }
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK && !cancelabled
        }
        return dialog
    }


    fun setView(view: View) {
        dialog.setView(view)
    }
    fun initOfCustomView(view: View?) {
        this.animView = view
        setViewBg(android.R.color.transparent)
        decorView?.setPadding(0, 0, 0, 0)
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams
    }

    /**
     * 重置PositiveButton点击默认关闭事件
     */
    fun resetCLickListener() {
        getPositiveButton()?.setOnClickListener { positiveClickLis(dialog) } // 禁用掉主动dismiss
    }

    var title: CharSequence? = null
        set(value) { field=value; dialog.setTitle(field) }
    var message: CharSequence? = null
        set(value) { field=value; dialog.setMessage(field) }
    @DrawableRes var icon: Int? = null
        set(value) { field=value; field?.also { dialog.setIcon(it) } }
    var customTitleView: View? = null
        set(value) { field=value; dialog.setCustomTitle(field) }

    private var positiveClickLis: (dialog: T) -> Unit = {}
    fun setPositiveButton(txt: CharSequence="确认", clickLis: (dialog: T) -> Unit) {
        positiveClickLis = clickLis // 复制重置点击即dismiss功能
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, txt) { _, _ -> positiveClickLis(dialog) }
    }
    fun getPositiveButton(): Button? = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
    fun setNegativeButton(txt: CharSequence="取消", clickLis: (dialog: T) -> Unit) {
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, txt) { _, _ -> clickLis(dialog) }
    }
    fun getNegativeButton(): Button? = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
    fun setNeutralButton(txt: CharSequence="不知道", clickLis: (dialog: T) -> Unit) {
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, txt) { _, _ -> clickLis(dialog) }
    }
    fun getNeutralButton(): Button? = dialog.getButton(DialogInterface.BUTTON_NEUTRAL)

    fun setViewBg(@DrawableRes resId: Int) {
        window?.setBackgroundDrawableResource(resId)
    }
    fun setViewBg(bg: Drawable) {
        window?.setBackgroundDrawable(bg)
    }
    fun setViewMarginLR(l: Int, r: Int) {
        decorView?.setPadding(l, decorView?.paddingTop ?: 0, r, decorView?.paddingBottom ?: 0)
    }

    fun setDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float) {
        window?.setDimAmount(dimAmount)
    }

    fun dismiss() {
        dialog.dismiss()
    }

    var isShowLock = false // show是个异步过程, 禁止循环执行show
    fun show() {
        isShowLock = true
        dialog.create(); dialog.show()
        resetCLickListener()
    }
    var idialog: IDialog? = null
    fun show(fragmentManager: FragmentManager): IDialog {
        isShowLock = true
        dialog.create()
        val idialog = IDialog(dialog)
        idialog.show(fragmentManager, javaClass.simpleName)
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutSide)
        // idialog.isCancelable = cancelabled
        // DialogFragment.prepareDialog 重置了setCancelable
        idialog.lifecycle.addObserver(this)
        return idialog
    }
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onInitLifecycleStateChanged?.invoke(source, event)
        onStateChanged?.invoke(event)
        when(event) {
            Lifecycle.Event.ON_RESUME -> resetCLickListener()
            Lifecycle.Event.ON_DESTROY -> idialog?.lifecycle?.removeObserver(this)
        }
    }
    var onStateChanged: ((event: Lifecycle.Event)->Unit)? = null
}

/**
 * 动画辅助类
 * 插值器
 *  [AccelerateDecelerateInterpolator]  在动画开始与结束的地方速率改变比较慢，在中间的时候加速
 *  [AccelerateInterpolator]            在动画开始的地方速率改变比较慢，然后开始加速
 *  [AnticipateInterpolator]            开始的时候向后然后向前甩
 *  [AnticipateOvershootInterpolator]   开始的时候向后然后向前甩一定值后返回最后的值
 *  [BounceInterpolator]                动画结束的时候弹起
 *  [CycleInterpolator]                 动画循环播放特定的次数，速率改变沿着正弦曲线
 *  [DecelerateInterpolator]            在动画开始的地方快然后慢
 *  [LinearInterpolator]                以常量速率改变
 *  [OvershootInterpolator]             向前甩一定值后再回到原来位置
 */
object AnimHelper {

    /*** 淡入 ***/
    fun fadeIn(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "alpha", 0F, 1F))
        interpolator = DecelerateInterpolator()
        duration = 300L
    }

    /*** 淡出 ***/
    fun fadeOut(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "alpha", 1F, 0F))
        interpolator = AccelerateInterpolator()
        duration = 300L
    }
    /*** 滑动 从左往右推入 ***/
    fun slideLeftRightIn(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationX", -view.width.toFloat(), 0F))
        duration = 300L
    }
    /*** 滑动 从左往右推出 ***/
    fun slideLeftRightOut(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationX", 0F, view.width.toFloat()))
        duration = 300L
    }
    /*** 滑动 右推向左推入 ***/
    fun slideRightLeftIn(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationX", view.width.toFloat(), 0F))
        duration = 300L
    }
    /*** 滑动 右推向左推出 ***/
    fun slideRightLeftOut(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationX", 0F, -view.width.toFloat()))
        duration = 300L
    }
    /*** 滑动 底部向上滑动弹入 ***/
    fun slideBottomTopIn(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat(), 0F))
        duration = 300L
    }
    /*** 滑动 底部向上滑动弹出 ***/
    fun slideBottomTopOut(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationY", 0F, -view.height.toFloat()))
        duration = 300L
    }
    /*** 滑动 顶部向下滑动弹入 ***/
    fun slideTopBottomIn(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationY", -view.height.toFloat(), 0F))
        duration = 300L
    }
    /*** 滑动 顶部向下滑动弹出 ***/
    fun slideTopBottomOut(view: View) = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view, "translationY", 0F, view.height.toFloat()))
        duration = 300L
    }
}


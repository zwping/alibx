package com.zwping.alibx

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
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


    open class AbsDialog<D: AppCompatDialog> : AppCompatDialog, LifecycleEventObserver {

        constructor(context: Context?, themeResId: Int) : super(context, themeResId) {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.attributes?.also {
                it.width = -1; it.height = -1
                it.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND; it.dimAmount=0.5F
            }
            decorView?.setPadding(0, 0, 0, 0)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            immersive()
        }

        val decorView: View?
            get() = window?.decorView
        val rootLayout: FrameLayout = initRootLayout(context)     // 根布局, 改写outSide方式
        var customView: View? = null                              // 自定义布局
        val animView: View?                                       // 动画视图, 改写anim方式
            get() = customView ?: decorView

        var cancelabled = true                                    // 返回键 & 空白区域是否可以关闭
        var canceledOnTouchOutSide = true                         // 空白区域是否可以关闭
            get() = if (!cancelabled) false else field

        var showAnimator = { it: View -> AnimHelper.fadeIn(it) }  // 显示动画
        var hideAnimator: ((View)->AnimatorSet)? = null           // 隐藏动画
        var disableHideAnim = false                               // 禁用隐藏动画

        var gravity: Int? = null                                  // customView位置, 使用rootLayout.inflater可直接使用布局的gravity
        var useLightBar = true                                    // 使用light bar, 背景默认0.5黑, 默认高亮bar, false使用默认

        override fun setContentView(view: View) {
            setContentView { view }
        }

        fun setContentView(inflater: (root: FrameLayout) -> View): D {
            rootLayout.removeAllViews()
            val view = inflater.invoke(rootLayout)
            view.setOnClickListener {  }                                // 拦截outSide事件
            if (view.layoutParams == null)
                view.layoutParams = FrameLayout.LayoutParams(-1, -2)    // 常用布局lp
            if (view.parent == null) rootLayout.addView(view)
            gravity?.also { (view.layoutParams as FrameLayout.LayoutParams).gravity = it }
            super.setContentView(rootLayout)
            customView = view
            return this as D
        }

        fun setGravity(gravity: Int): D {
            this.gravity = gravity
            val lp = customView?.layoutParams ?: return this as D
            if (lp is FrameLayout.LayoutParams) lp.gravity = gravity
            return this as D
        }

        fun setShowAnimator(anim: (View)->AnimatorSet): D {
            this.showAnimator = anim
            return this as D
        }
        fun setHideAnimator(anim: (View)->AnimatorSet): D {
            this.hideAnimator = anim
            return this as D
        }

        fun setDimAmount(@FloatRange(from=0.0, to=1.0) amount: Float): D {
            // rootLayout.setBackgroundColor(blackAlpha(amount))
            window?.attributes?.dimAmount = amount              // window控制透明度更自然
            return this as D
        }

        fun setLightBar(use: Boolean): D {
            this.useLightBar = use
            return this as D
        }

        override fun setCanceledOnTouchOutside(cancel: Boolean) {
            super.setCanceledOnTouchOutside(cancel)
            canceledOnTouchOutSide = cancel
        }

        override fun setCancelable(flag: Boolean) {
            super.setCancelable(flag)
            cancelabled = flag
        }

        override fun show() {
            if (null == customView) {
                Toast.makeText(context, "只支持自定义view使用", Toast.LENGTH_SHORT).show()
                return
            }
            animView?.visibility = View.INVISIBLE
            super.show()
            val view = animView ?: return
            view.post {
                view.visibility = View.VISIBLE
                showAnimator.invoke(view).start()
            }
            if (useLightBar) immersiveLightBar()                 // 细节处理
        }

        override fun dismiss() {
            if (disableHideAnim) { super.dismiss(); return }
            if (animView == null) { super.dismiss(); return }
            hideAnimator?.invoke(animView!!)?.also {
                it.doOnEnd { super.dismiss() }
                it.start()
                return
            }
            super.dismiss()
        }

        fun dismiss(disableAnim: Boolean): D {
            this.disableHideAnim = disableAnim
            dismiss()
            return this as D
        }

        private fun initRootLayout(context: Context) = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            // setBackgroundColor(blackAlpha(0.6F))
            setOnClickListener { if (canceledOnTouchOutSide) dismiss() }
        }

        /**
         * 透明度黑色
         * @param alpha 透明度 0f～1f
         */
//        private fun blackAlpha(alpha: Float): Int {
//            val a = Math.min(255, Math.max(0, (alpha * 255).toInt())) shl 24
//            val rgb = 0x00ffffff and Color.BLACK
//            return a + rgb
//        }

        private fun immersive() {
            val window = window ?: return
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        private fun immersiveLightBar() {
            val window = window ?: return
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val wic = WindowInsetsControllerCompat(window, window.decorView)
                wic.isAppearanceLightStatusBars = false
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    wic.isAppearanceLightNavigationBars = false
            }
        }
//        private fun ViewPropertyAnimator.doOnEnd(lis: (animation: Animator?)->Unit):ViewPropertyAnimator{
//            setListener(object: Animator.AnimatorListener{
//                override fun onAnimationStart(animation: Animator?) {}
//                override fun onAnimationEnd(animation: Animator?) {
//                    lis.invoke(animation)
//                }
//                override fun onAnimationCancel(animation: Animator?) {}
//                override fun onAnimationRepeat(animation: Animator?) {}
//            })
//            return this
//        }

        protected fun Float.dpToPx() = this*Resources.getSystem().displayMetrics.density+0.5F

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        }

    }

    /**
     * AppCompatDialog复写, 支持继承/快速调用使用
     * @param block 快速调用具体实现操作
     */
    open class Dialog : AbsDialog<Dialog>, LifecycleEventObserver {

        constructor(context: Context?) : super(context, -1)
        constructor(
            context: Context?,
            block: Dialog.() -> Unit = { },
            themeResId: Int = -1
        ) : super(context, themeResId) { block(this) }

        /**
         * 显示DialogFragment, 支持[onStateChanged]
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
    }

    /**
     * Ios风格常用对话Dialog
     */
    class DialogIOS(
        context: Context,
        block: DialogIOS.() -> Unit = {},
    ) : Dialog(context) {

        private val lyMargin = 50F.dpToPx().toInt() // 布局两侧排挤

        private val view by lazy { IOSLayout(rootLayout.context).apply {
            layoutParams = FrameLayout.LayoutParams(-1, -2).also {
                it.gravity = Gravity.CENTER
                it.leftMargin = lyMargin; it.rightMargin = lyMargin
            }
        } }

        init {
            block(this)
            setContentView(view)
            view.title.visibility = View.GONE
            view.message.visibility = View.GONE
            view.btnConfirm.visibility = View.GONE
            view.btnCancel.visibility = View.GONE
            view.line.visibility = View.GONE
        }

        override fun setTitle(title: CharSequence?) {
            super.setTitle(title)
        }
        fun setTitleIOS(txt: CharSequence): DialogIOS {
            view.title.visibility = View.VISIBLE; view.title.text = txt; return this
        }
        fun setMessageIOS(txt: CharSequence): DialogIOS {
            view.message.visibility = View.VISIBLE; view.message.text = txt; return this
        }
        fun setBtnCancelIOS(txt: CharSequence="取消", lis: (DialogIOS)->Unit={}): DialogIOS {
            view.line.visibility = view.btnConfirm.visibility
            view.btnCancel.also {
                it.visibility = View.VISIBLE; it.text = txt
                it.setOnClickListener { dismiss(); lis(this) }
            }
            return this
        }
        fun setBtnConfirmIOS(txt: CharSequence="确认", lis: (DialogIOS)->Unit): DialogIOS {
            view.line.visibility = view.btnCancel.visibility
            view.btnConfirm.also {
                it.visibility = View.VISIBLE; it.text = txt
                it.setOnClickListener { lis(this) }
            }
            return this
        }

        private class IOSLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CardView(context, attrs) {

            private fun Float.dpToPx() = this*Resources.getSystem().displayMetrics.density+0.5F

            private val btnHigh = 44F.dpToPx().toInt()      // btn高度
            private val lineHigh = 0.5F.dpToPx().toInt()    // line高度
            val lyRadius = 10F.dpToPx()                     // 布局圆角


            val title by lazy { TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                setPadding(btnHigh, 0, btnHigh, 0)
                setTextColor(Color.BLACK); textSize = 16F
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            } }
            val message by lazy { TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { it.topMargin = lineHigh }
                setPadding(btnHigh, 0, btnHigh, 0)
                setTextColor(Color.BLACK); textSize = 15F
            } }
            val btnConfirm by lazy { TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT).apply { weight=1F }
                background = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable((0x1a000000).toInt()))
                    addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
                }
                setTextColor((0xff2e7cf7).toInt()); textSize = 15F
                gravity=Gravity.CENTER; text = "确认"
            }  }
            val btnCancel by lazy { TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT).apply { weight=1F }
                background = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable((0x1a000000).toInt()))
                    addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
                }
                setTextColor((0xff2e7cf7).toInt()); textSize = 15F
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                gravity=Gravity.CENTER; text = "取消"
            }  }
            val line by lazy { View(context).apply {
                setBackgroundColor((0xffb3b3b3).toInt())
                layoutParams = LinearLayout.LayoutParams(lineHigh, LayoutParams.MATCH_PARENT)
            } }

            init {
                setCardBackgroundColor((0xe6f9f9f9).toInt())
                radius = lyRadius; elevation = 0F
                val lyRoot = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(0, btnHigh/2, 0, 0)
                    addView(title)
                    addView(message)
                    addView(View(context).also {
                        it.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, lineHigh).also { it.topMargin = btnHigh/2 }
                        it.setBackgroundColor((0xffb3b3b3).toInt())
                    })
                    addView(LinearLayout(context).also {
                        it.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, btnHigh)
                        it.orientation = LinearLayout.HORIZONTAL
                        it.addView(btnCancel)
                        it.addView(line)
                        it.addView(btnConfirm)
                    })
                }
                addView(lyRoot)
            }
        }

    }

    open class DialogBottomSheet(context: Context,
                                 theme: Int = 0) :
            BottomSheetDialog(context, theme) {
        // http://blog.csdn.net/yanzhenjie1003/article/details/51938400
    }

    /**
     * AlertDialog
     *  优化链式调用, 增加链式[setCanceledOnTouchOutside]
     *  复写Button点击事件, 默认不关闭
     */
    open class DialogAlert(
        context: Context,
        themeResId: Int = android.R.style.Theme_Material_Light_Dialog_Alert
    ) : AlertDialog.Builder(context, themeResId) {

        override fun setIcon(iconId: Int): DialogAlert {
            super.setIcon(iconId); return this
        }
        override fun setTitle(title: CharSequence?): DialogAlert {
            super.setTitle(title); return this
        }
        override fun setMessage(message: CharSequence?): DialogAlert {
            super.setMessage(message); return this
        }

        // 点击事件重置
        private var positiveClickListener: ((DialogAlert) -> Unit)? = null
        private var negativeClickListener: ((DialogAlert) -> Unit)? = null
        private var neutralClickListener: ((DialogAlert) -> Unit)? = null
        fun setPositiveButton(text: CharSequence = "确认", listener: (DialogAlert) -> Unit): DialogAlert {
            positiveClickListener = listener; setPositiveButton(text) { _, _ -> }
            return this
        }
        fun setNegativeButton(text: CharSequence = "取消", listener: (DialogAlert) -> Unit): DialogAlert {
            negativeClickListener = listener; setNegativeButton(text) { _, _ -> }
            return this
        }
        fun setNeutralButton(text: CharSequence = "不确定", listener: (DialogAlert) -> Unit): DialogAlert {
            neutralClickListener = listener; setNeutralButton(text) { _, _ -> }
            return this
        }

        private var canceledOnTouchOutside: Boolean? = null
        fun setCanceledOnTouchOutside(cancel: Boolean): DialogAlert {
            canceledOnTouchOutside = cancel; _dialog?.setCanceledOnTouchOutside(cancel); return this;
        }
        override fun setCancelable(cancelable: Boolean): DialogAlert {
            super.setCancelable(cancelable); return this
        }

        fun dismiss() {
            _dialog?.dismiss()
            _dialog?.setCanceledOnTouchOutside(true)
        }

        private var _dialog: AlertDialog? = null

        override fun create(): AlertDialog {
            return super.create().also { dialog ->
                _dialog = dialog
                canceledOnTouchOutside?.also { dialog.setCanceledOnTouchOutside(it) }
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            }
        }
        override fun show(): AlertDialog {
            return super.show().also { dialog ->
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.also {
                    it.setOnClickListener { positiveClickListener?.invoke(this) }
                }
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.also {
                    it.setOnClickListener { dialog.dismiss(); negativeClickListener?.invoke(this) }
                }
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.also {
                    it.setOnClickListener { neutralClickListener?.invoke(this) }
                }
            }
        }
    }

    /**
     * 日期选择器
     * @param callback 选中回执, 已处理月份-1
     * @param month 注意月份起始下标为0
     */
    open class DialogDatePicker(
        context: Context,
        callback: (year: Int, month: Int, day: Int) -> Unit,
        year: Int = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH),
        day: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
        themeResId: Int = DialogDefaultTheme
    ) : DatePickerDialog(
        context,
        themeResId,
        OnDateSetListener { _, y, m, d -> callback(y, m+1, d) },
        year,
        month,
        day
    ), LifecycleEventObserver {

        /*** 修改当前选中日期, month-1 ***/
        override fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
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

        private val btnColor by lazy {
            val attr = intArrayOf(R.attr.colorPrimary)
            val typed = context.theme.obtainStyledAttributes(attr)
            typed.getColor(0, Color.BLACK).also { typed.recycle() }
        }

        override fun show() {
            super.show().also {
                getButton(DialogInterface.BUTTON_POSITIVE)?.also {
                    it.setTextColor(btnColor)
                }
                getButton(DialogInterface.BUTTON_NEGATIVE)?.also {
                    it.setTextColor(btnColor)
                }
            }
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        }

    }

    /**
     * 时间选择器
     * @param is24HourView [DateFormat.is24HourFormat]
     */
    open class DialogTimePicker(
        context: Context?,
        lis: (hourOfDay: Int, minute: Int) -> Unit,
        hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        minute: Int = Calendar.getInstance().get(Calendar.MINUTE),
        is24HourView: Boolean = true,
        themeResId: Int = DialogDefaultTheme
    ): TimePickerDialog(
        context,
        themeResId,
        OnTimeSetListener { _, hd, m -> lis(hd, m)  },
        hourOfDay, minute, is24HourView
    ), LifecycleEventObserver {

        override fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
            super.updateTime(hourOfDay, minuteOfHour)
        }

        private val btnColor by lazy {
            val attr = intArrayOf(R.attr.colorPrimary)
            val typed = getContext().theme.obtainStyledAttributes(attr)
            typed.getColor(0, Color.BLACK).also { typed.recycle() }
        }

        override fun show() {
            super.show().also {
                getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(btnColor)
                getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(btnColor)
            }
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        }
    }

    /**
     * 进度
     * @param loadingMode 转圈 / 进度条
     */
    open class DialogProgress(
        context: Context?,
        message: CharSequence,
        private val loadingMode: Boolean = true,
        theme: Int = DialogDefaultTheme
    ) : ProgressDialog(context, theme),
        LifecycleEventObserver {

        init {
            setMessage(message)
            setProgressStyle(if (loadingMode) STYLE_SPINNER else STYLE_HORIZONTAL)
            setCanceledOnTouchOutside(false)
        }

        // loading mode 圆圈颜色
        override fun setIndeterminateDrawable(d: Drawable?) { super.setIndeterminateDrawable(d) }
        private var indeterminateTint: Int? = null
        fun setIndeterminateDrawable(@ColorInt color: Int): DialogProgress {
            this.indeterminateTint = color; return this
        }
        // 进度mode 三重颜色
        override fun setProgressDrawable(d: Drawable?) { super.setProgressDrawable(d) }
        fun setProgressBarColor(bgColor: Int,
                                progressColor: Int,
                                secondaryColor: Int = -1,
                                @Px radius: Float = 0F,
                                @Px height: Int = -1): DialogProgress {
            val bgClipDrawable = ClipDrawable(
                GradientDrawable().apply { setColor(bgColor); cornerRadius = radius },
                Gravity.LEFT, ClipDrawable.HORIZONTAL)
            bgClipDrawable.level = 10000
            val progressClip = ClipDrawable(
                GradientDrawable().apply { setColor(progressColor); cornerRadius = radius },
                Gravity.LEFT, ClipDrawable.HORIZONTAL)
            var secondaryClip: ClipDrawable? = null
            if (secondaryColor != -1)
                secondaryClip = ClipDrawable(
                    GradientDrawable().apply { setColor(secondaryColor); cornerRadius = radius },
                    Gravity.LEFT, ClipDrawable.HORIZONTAL)
            val progressDrawables = arrayOf<Drawable>(bgClipDrawable, secondaryClip ?: progressClip, progressClip)
            val progressLayerDrawable = LayerDrawable(progressDrawables)
            progressLayerDrawable.setId(0, android.R.id.background)
            progressLayerDrawable.setId(1, android.R.id.secondaryProgress)
            progressLayerDrawable.setId(2, android.R.id.progress)
            if (height != -1 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                progressLayerDrawable.setLayerHeight(0, height)
                progressLayerDrawable.setLayerHeight(1, height)
                progressLayerDrawable.setLayerHeight(2, height)
            }
            setProgressDrawable(progressLayerDrawable)
            return this
        }
        // 进度最大值
        override fun setMax(max: Int) { super.setMax(max) }
        // 进度值
        override fun incrementProgressBy(diff: Int) { super.incrementProgressBy(diff) }
        // 第二进度值
        override fun incrementSecondaryProgressBy(diff: Int) { super.incrementSecondaryProgressBy(diff) }
        // 进度值的格式, 默认 %1d/%2d / 直接输出值 String.format("%.2fkb/%.2fM, cur, total)
        override fun setProgressNumberFormat(format: String?) { super.setProgressNumberFormat(format) }
        // 进度百分比 null=>隐藏
        override fun setProgressPercentFormat(format: NumberFormat?) { super.setProgressPercentFormat(format) }

        override fun show() {
            super.show().also {
                if (loadingMode && indeterminateTint != null) {
                    findViewById<ProgressBar>(android.R.id.progress)
                        .indeterminateTintList = ColorStateList.valueOf(indeterminateTint!!)
                }
            }
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
     * 改写AlertDialog.Builder, 支持子类链式调用
     */
    open class AbsAlertDialogBuilder<T, B: AlertDialog.Builder> : AlertDialog.Builder, LifecycleEventObserver {
        constructor(context: Context, themeResId: Int) : super(context, themeResId)

        protected var _dialog: AlertDialog? = null

        val positiveButton: Button? = _dialog?.getButton(DialogInterface.BUTTON_POSITIVE)
        val negativeButton: Button? = _dialog?.getButton(DialogInterface.BUTTON_NEGATIVE)
        val neutralButton: Button? = _dialog?.getButton(DialogInterface.BUTTON_NEUTRAL)

        var positiveButtonTxt: CharSequence = "确认"
            set(value) { field = value; positiveButton?.text = value }
        var negativeButtonTxt: CharSequence = "取消"
            set(value) { field = value; positiveButton?.text = value }
        var neutralButtonTxt: CharSequence = "不确定"
            set(value) { field = value; positiveButton?.text = value }
        private var canceledOnTouchOutside: Boolean? = null
        // private var cancelable: Boolean? = null

        protected var bindView: (tv: TextView?, i: Int, it: T?) -> Unit = { tv, _, it -> tv?.text = "$it"; tv?.setTextColor(Color.BLACK) }
        fun setBindView(bindView: (tv: TextView?, i: Int, it: T?) -> Unit): B {
            this.bindView = bindView; return this as B
        }

        override fun setIcon(iconId: Int): B {
            super.setIcon(iconId); return this as B
        }
        override fun setTitle(title: CharSequence?): B {
            super.setTitle(title); return this as B
        }
        override fun setMessage(message: CharSequence?): B {
            super.setMessage(message); return this as B
        }

        override fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): B {
            super.setPositiveButton(text, listener); return this as B
        }
        override fun setNegativeButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): B {
            super.setNegativeButton(text, listener); return this as B
        }
        override fun setNeutralButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): B {
            super.setNeutralButton(text, listener); return this as B
        }

        fun setCanceledOnTouchOutside(cancel: Boolean): B {
            _dialog?.setCanceledOnTouchOutside(cancel)
            canceledOnTouchOutside = cancel
            return this as B
        }
        override fun setCancelable(cancelable: Boolean): B {
            // this.cancelable = cancelable
            super.setCancelable(cancelable); return this as B
        }
        override fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?): B {
            super.setOnDismissListener(onDismissListener); return this as B
        }

        fun dismiss(): B {
            _dialog?.dismiss(); return this as B
        }

        override fun create(): AlertDialog {
            return super.create().also {
                it.requestWindowFeature(Window.FEATURE_NO_TITLE)
                _dialog = it
                canceledOnTouchOutside?.also { _dialog?.setCanceledOnTouchOutside(it) }
            }
        }

        override fun show(): AlertDialog {
            return super.show()
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        }

    }

    /**
     * 简单列表, 支持T Entity & bindView
     */
    open class DialogItems<T>(
        context: Context,
        private val datas: MutableList<T>,
        private val itemClickListener: (dialog: DialogItems<T>, index: Int, item: T)-> Unit,
        themeResId: Int = DialogDefaultTheme
    ) : AbsAlertDialogBuilder<T, DialogItems<T>>(context, themeResId) {

        override fun create(): AlertDialog {
            setItems(datas.map { "$it" }.toTypedArray()) { _, which ->
                itemClickListener.invoke(this, which, datas[which])
            }
            return super.create().apply {
                listView.post {
                    repeat(listView.childCount) { i ->
                        bindView(listView.getChildAt(i).findViewById(android.R.id.text1), i, datas[i])
                    }
                }
            }
        }

    }

    /**
     * 单选列表, 支持T Entity & bindView
     */
    open class DialogSingleChoiceBuilder<T>(
        context: Context,
        private val datas: MutableList<T>,
        private var choiceListener: (dialog: DialogSingleChoiceBuilder<T>, index: Int, item: T)-> Unit,
        themeResId: Int = DialogDefaultTheme
    ) : AbsAlertDialogBuilder<T, DialogSingleChoiceBuilder<T>>(context, themeResId) {

        var chechData: T? = null
            private set

        private var checkItem: Int = -1
            set(value) { if (value >= 0 && value < datas.size) { field=value; chechData=datas[value] } }
        fun setCheckItem(checkItem: Int): DialogSingleChoiceBuilder<T> {
            this.checkItem = checkItem; return this
        }

        override fun create(): AlertDialog {
            setSingleChoiceItems(datas.map { "$it" }.toTypedArray(), checkItem) { _, which ->
                _dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = true
                checkItem = which
            }
            setPositiveButton(positiveButtonTxt) { _, _ ->
                choiceListener(this, checkItem, datas[checkItem])
            }
            setNegativeButton(negativeButtonTxt){ _, _ -> }
            return super.create().apply {
                listView.post {
                    repeat(listView.childCount) { i ->
                        bindView(listView.getChildAt(i).findViewById(android.R.id.text1), i, datas[i])
                    }
                }
            }
        }

        override fun show(): AlertDialog {
            return super.show().apply {
                getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = chechData != null
            }
        }

    }

    /**
     * 简单多选列表, 支持T Entity & bindView
     */
    open class DialogMultiChoiceBuilder<T>(
        context: Context,
        private val datas: MutableList<T>,
        private val choiceListener: (dialog: DialogMultiChoiceBuilder<T>, items: MutableList<T>) -> Unit,
        themeResId: Int = DialogDefaultTheme
    ) : AbsAlertDialogBuilder<T, DialogMultiChoiceBuilder<T>>(context, themeResId) {

        /*** 选中的数据 ***/
        var checkDatas: MutableList<T> = mutableListOf()
            private set

        private var checkItems: BooleanArray? = null
            set(value) {
                if (value?.size ?: 0 == datas.size) {
                    field = value; value?.forEachIndexed { index, it -> if (it) checkDatas.add(datas[index]) }
                }
            }
        fun setCheckItems(checkItems: BooleanArray): DialogMultiChoiceBuilder<T>{
            this.checkItems = checkItems; return this
        }

        override fun create(): AlertDialog {
            setMultiChoiceItems(datas.map { "$it" }.toTypedArray(), checkItems) { _, which, isChecked ->
                if (isChecked) checkDatas.add(datas[which]) else checkDatas.remove(datas[which])
                _dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = !checkDatas.isNullOrEmpty()
            }
            setPositiveButton(positiveButtonTxt) { _, _ ->
                choiceListener(this, checkDatas)
            }
            setNegativeButton(negativeButtonTxt){ _, _ -> }
            return super.create().apply {
                listView.post {
                    repeat(listView.childCount) { i ->
                        bindView(listView.getChildAt(i).findViewById(android.R.id.text1), i, datas[i])
                    }
                }
            }
        }

        override fun show(): AlertDialog {
            return super.show().apply {
                positiveButton?.isEnabled = checkItems?.isNotEmpty() == true
            }
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
 *  [TimeInterpolator]                  该接口用于实现您自己的插值器
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


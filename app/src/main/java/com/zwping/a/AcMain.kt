package com.zwping.a

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.a.databinding.TestBinding
import com.zwping.a.fm.FmHome
import com.zwping.alibx.*
import com.zwping.alibx.Util.logd
import org.json.JSONObject
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec


class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }


    private val fmHome by lazy { FmHome() }
    override fun initView() {
        vb.iv.glide("https://p.qqan.com/up/20211-11/20211123931516611.jpg") {
            circleCrop()
            stroke = Stroke(1F, (0xffE1EBF5).toInt())
        }
        vb.layerFindTeacher.start(this, mutableListOf(
            "https://img2.woyaogexing.com/2021/12/04/c48b8378a714452fb878e96fb0927afb!400x400.jpg",
            "https://img2.woyaogexing.com/2021/12/04/c3f26419f3284de79b4d293090568e46!400x400.jpeg",
            "https://img2.woyaogexing.com/2021/12/04/fb461641da2e49cfb0291a4736c3b475!400x400.jpeg",
        ))
        handler.postDelayed({
            vb.layerFindTeacher.visibility = View.GONE
        }, 5000)


        vb.viewPager2.initBannerOfImg<String>({iv, data, position ->
            iv.glide(data[position])
        })
        vb.viewPager2.setBannerMultiItem(6F.dp2px(), 15F.dp2px())
        vb.viewPager2.setBannerData(mutableListOf(
            "https://img2.woyaogexing.com/2021/12/04/c3f26419f3284de79b4d293090568e46!400x400.jpeg",
            "https://img2.woyaogexing.com/2021/12/04/c48b8378a714452fb878e96fb0927afb!400x400.jpg",
            "https://img2.woyaogexing.com/2021/12/04/fb461641da2e49cfb0291a4736c3b475!400x400.jpeg",
        ))
//        vb.viewPager2.scrollTo(10, 0)
        ValueAnimator.ofFloat(0F, 1.0F, 0F).also {
            it.duration = 1000
            it.interpolator = DecelerateInterpolator() // 减速度插值器
            it.addUpdateListener {
                val rv = vb.viewPager2.getChildAt(0) as RecyclerView
                val lm = rv.layoutManager as LinearLayoutManager
                val v = "${it.animatedValue}".toFloat() * 300
                lm.scrollToPositionWithOffset(2, v.toInt()*-1)
            }
//            it.start()
            handler.postDelayed({ it.start() }, 500)
        }

        handler.postDelayed({
//            val rv = vb.viewPager2.getChildAt(0) as RecyclerView
//            val lm = rv.layoutManager as LinearLayoutManager
//            lm.scrollToPositionWithOffset(3, 100)

//            IDialog.DialogBottomSheet(this).also {
//                it.setContentView(R.layout.test)
//            }.show()
        }, 2000)

        open("alibx://ac/sec")

        vb.child.setOnClickListener {
             Dlg(this).show()

//            IDialog.Dialog(this,
//                {
//                    showAnimator = { AnimHelper.slideBottomTopIn(it) }
//                    hideAnimator = { AnimHelper.slideTopBottomOut(it) }
//                    val vb by lazy { Test1Binding.inflate(LayoutInflater.from(context)) }
//                    setContentView(vb.root)
////                    setCancelable(false)
////                    setCanceledOnTouchOutside(false)
//                }).show()
        }
//        IDialog.DialogItemsBuilder(this, mutableListOf( "me")).show(supportFragmentManager)

        // Dlg(this).show(supportFragmentManager)


        val ob by lazy {
            JSONObject("{\n" +
                    "\"bg\": \"https://img.yikaoapp.com/icon/yks_dialog/toast_1.png\",\n" +
                    "\"width\": \"800\",\n" +
                    "\"height\": \"800\",\n" +
                    "\"button\": {\n" +
                    "\"bg\": \"#0ff000\",\n" +
                    "\"color\": \"white\",\n" +
                    "\"title\": \"立即参加\",\n" +
                    "\"left\": \"20\",\n" +
                    "\"bottom\": \"33\",\n" +
                    "\"url\": \"yikao://test/scanning?fr=app&prefix=http%3A%2F%2Fimg.yikaoapp.com%2Ficon%2Ftest%2FscanningAvatar%2F&item=1.jpg,2.jpg,3.jpg,4.jpg,5.jpg\"\n" +
                    "},\n" +
                    "\"tips\": {\n" +
                    "\"title\": \"已有<font color='#0A84FF'>1186797</font>人参加测评\",\n" +
                    "\"bottom\": \"10\",\n" +
                    "\"color\": \"#999999\"\n" +
                    "},\n" +
                    "\"closeBtn\": \"https://img.yikaoapp.com/icon/yks_dialog/closeBtn.png\",\n" +
                    "\"clickBgHide\": false,\n" +
                    "\"nextShowTime\": 1639670399\n" +
                    "}")
        }

//        Dg().show(supportFragmentManager, "123")

//        val c = "n0riEaUU4IeCw6Qz9YAfWmWj0e41HNW/ohHCH05kSZEhMza0RxwX2Jyn1xbDpUclSIAU+qSYGaKHUr4pIyavdtdQFDgDUYrZLQc50q7qWpLYlTDRPAoouBmQqkdmKjiOKJx14js965d0T34iGH81JgNoIVBwmfbkGshpcxyzlkQp6GGY/K3EaHb+L0K8OHDjnnIgOhdGjF2c1KCgoo8/Wgkr640aVv/llrYyGe6dzqKfWjMOcpZ8o4eCNIHcmF3SUorAxo1PsAy/WOjD5DaPUBtfBhA8qi+YTwpHutWM5mDwWCoK9UzdUG8XgDyJ5zw2"
        val c = "LL1AoK2mA/3lSPnZXZ/kpOA5+H3nLrGMmePXRlKFH8yyCay+evauG4TVe1utqYYAokKg5ABYWJZTQf2eeVYj7m/LvvqC4yo89qyFNWfr/EZUgtVWtr38Qu+xDcdiQxQfcI0MGVqzfxGikEE4dQqgms1NGVyFez8QFHdQTzp/WimikEE4dQqgmu95YFNFvmZRTLxYFPp/KGjHOXRBhC3R4Gps0sF7oZGZ9Eo5W2Jm4qDLhMttHqQn9ehKnheGUGcEx36Zr6p2+O3xGVX7NYx3WPFbjLpLvqyIxyb2b1NUnzM9SJXvryok203+KCqOPICBWiJdlDWditoY32gIBvKux7/wwdAZjqkHQvpWuzsps6tCGwa3EiUSnd2SuuiHlY5lGB8gUyoOH+Dob9DiQDeISQTVhzalpkuIq3uC8VG1mqp07/uSfCDJkNQJk6bH2oRjsKNrtgEqKWhQ/mopGeP8Z81hdPzSzJ593BhfIEmD5hcwc5+oHJ3A+0703GUKW2Q1WQHQ8DCS7kiNJ+EvPMcDaNEnfjB/9BJi5TGtIaS+HryGRQjXelQ1VuQ4y3H49JtF+W4rdwrgPgp47i8o5LMEjIdp6RT4d56+t25ZCuLoljaeGzZEKYKMAQF7oYHbsL+qPR6umibcSmKvKksm6jJCFfrkrcDmD6/UuZpUIvPPqXoj4faTJ4PkLsOz+2yrRYPGPR6umibcSmIj5OdWJx5CiJAYLE30RT8tdMxIKGTEyToiq4uVYvenPxD9ut/KO8eEPR6umibcSmKvKksm6jJCFePJ4bt4OdbJd67knm4UsOvKAZ3ZfWEW6uoVufDtukjvTgCX2I+3q4TtAK3WATrHyRd9KW4F9jghGpClg/bYeM14SA8qqIS6awQ9UDPnIEOHRzzqnKYxkvyOaHEaqxwcarV4oqqgBa4FVbx21XIiDaa4eJXiczOrA0KqrVbAtA5R1zQPY2CbMY9Q/mopGeP8ZxTuf+LrveSewEEevUb3R3T6JZ+LMqNTGYI63PJifd8pMaDJf6scbrS0HZUKHFh2hy9vfQLYt5hOLcxYIIzl9Y9iFG+yLbjxxjc5am6Zg6wV3TbVFrXqbdJIIAwiENv6XMmjbfWWkf5nokrrcgNO+n/u0iYgdY2j8zGpAOTWiZ+Jru0beQo9wtTtgrr6djDV+qPP7D62mhdAHnQr46AKWoQ="
        logd(11, Des.decodeForPapaer("558ac4cf1fc70845", c))

    }

    class Dg: DialogFragment() {


        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val inflater = LayoutInflater.from(context)
            val view: View = inflater.inflate(R.layout.test1, null)
            val dialog = AlertDialog.Builder(requireContext())
            // 关闭标题栏，setContentView() 之前调用
            dialog.setView(view)
            dialog.create().also {
                it.requestWindowFeature(Window.FEATURE_NO_TITLE)
                it.setCanceledOnTouchOutside(false)
                it.setCancelable(false)
                return it
            }
        }

    }

    class Dlg(context: Context) : IDialog.Dialog(context, { }) {

        private val ob by lazy {
            JSONObject("{\n" +
                    "\"bg\": \"https://p3.img.cctvpic.com/photoworkspace/contentimg/2021/08/31/2021083113465985068.jpg\",\n" +
                    "\"width\": \"1080\",\n" +
                    "\"height\": \"810\",\n" +
                    "\"button\": {\n" +
                    "\"bg\": \"#0ff000\",\n" +
                    "\"color\": \"white\",\n" +
                    "\"title\": \"立即参加\",\n" +
                    "\"left\": \"20\",\n" +
                    "\"bottom\": \"33\",\n" +
                    "\"url\": \"yikao://test/scanning?fr=app&prefix=http%3A%2F%2Fimg.yikaoapp.com%2Ficon%2Ftest%2FscanningAvatar%2F&item=1.jpg,2.jpg,3.jpg,4.jpg,5.jpg\"\n" +
                    "},\n" +
                    "\"tips\": {\n" +
                    "\"title\": \"已有<font color='#0A84FF'>1186797</font>人参加测评\",\n" +
                    "\"bottom\": \"10\",\n" +
                    "\"color\": \"#999999\"\n" +
                    "},\n" +
                    "\"closeBtn\": \"https://img.yikaoapp.com/icon/yks_dialog/closeBtn.png\",\n" +
                    "\"clickBgHide\": true,\n" +
                    "\"nextShowTime\": 1639670399\n" +
                    "}")
        }

        private val vb by lazy { Test1Binding.inflate(LayoutInflater.from(context)) }

        init {

            showAnimator = { AnimHelper.slideBottomTopIn(it) }
            hideAnimator = { AnimHelper.slideTopBottomOut(it) }
            setContentView(vb.root)

            window?.decorView?.setOnClickListener { showToast("123") }
            setCanceledOnTouchOutside(ob.optBoolean("clickBgHide", true))
            val w = context.getScreenWidth() * 0.78F
            var h = w * 427F / 290 // 原本比例宽高
            val w1 = ob.optInt("width")
            val h1 = ob.optInt("height")
            if (w1 > 0 && h1 > 0) {
                h = w * h1 / w1
            }
            logd(vb.lyRoot.layoutParams)
            vb.lyRoot.updateLayoutParams<FrameLayout.LayoutParams> {
                width = w.toInt()
                gravity = Gravity.CENTER
            }
            vb.lyContainer.updateLayoutParams {
                width = w.toInt(); height = h.toInt()
            }
            vb.ivBg.glide(ob.optString("bg").also { logd(it) })
            vb.btn.visibility = View.GONE
            ob.optJSONObject("button")?.also { btn ->
                vb.btn.visibility = View.VISIBLE
                vb.btn.apply {
                    val bg = try { Color.parseColor("${btn.optString("bg") ?: "#0a84ff"}") } catch (e: Exception) { (0xff0a84ff).toInt() }
                    backgroundTintList = ColorStateList.valueOf(bg)
                    val tc = try { Color.parseColor("${btn.optString("color") ?: "#ffffff"}") } catch (e: Exception) { (0xffffffff).toInt() }
                    setTextColor(tc)
                    text = btn.optString("title")
                    updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        btn.optInt("left").toFloat().dp2px().also { leftMargin=it; rightMargin=it }
                        bottomMargin = btn.optInt("bottom").toFloat().dp2px()
                    }
                    setOnClickListener {
                        showToast(btn.optString("url"))
                    }
                }
            }
            vb.tvTips.visibility = View.GONE
            ob.optJSONObject("tips")?.also { tip ->
                vb.tvTips.visibility = View.VISIBLE
                val tc = try { Color.parseColor("${tip.optString("color") ?: "#999999"}") } catch (e: Exception) { (0xff999999).toInt() }
                vb.tvTips.setTextColor(tc)
                vb.tvTips.setText(Html.fromHtml(tip.optString("title")))
                vb.tvTips.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = tip.optInt("bottom").toFloat().dp2px()
                }
            }
            vb.ivClose.apply {
                visibility = View.GONE
                val icon = ob.optString("closeBtn")
                if (!icon.isNullOrBlank()) {
                    visibility = View.VISIBLE
                    glide(icon)
                    setOnClickListener { dismiss() }
                }
            }
        }
    }

    enum class Style: IEnumViewHolder{
        Title {
            override fun holder(parent: ViewGroup) = BaseViewHolderVB<Entity, TestBinding>(
                TestBinding.inflate(parent.getLayoutInflater(), parent, false),
                {view, entity ->
                    itemView.background = createGradientDrawable() {
                        setColor(Color.WHITE)
                        setStroke(1F.dp2px(), Color.RED) }
                }
            )
        },
        Title2 {
            override fun holder(parent: ViewGroup) = BaseViewHolderVB<Entity2, Test1Binding>(
                Test1Binding.inflate(parent.getLayoutInflater(), parent, false),
                {view, entity ->  }
            )
        }
    }
    class Entity:ItemViewType{
        override val itemViewType: Enum<*> = Style.Title
    }
    class Entity2:ItemViewType{
        override val itemViewType: Enum<*> = Style.Title2
    }




}
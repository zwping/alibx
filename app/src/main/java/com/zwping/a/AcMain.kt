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
import android.widget.ImageView
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
        val u = "https://pic2.zhimg.com/80/v2-df7fd62e3fffcd9c14c9f82db55a9a3d_1440w.jpg"
        vb.layerFindTeacher.start(this, u,  mutableListOf(
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

        Dlg(this).show()

//        IDialog.Dialog(this, {
//            showAnimator = { AnimHelper.slideRightLeftIn(it) }
//            hideAnimator = { AnimHelper.slideLeftRightOut(it) }
//             val vb = TestBinding.inflate(rootLayout.getLayoutInflater(), rootLayout, true)
//            setContentView(vb.root)
//        }).show()

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
                    "\"clickBgHide\": false,\n" +
                    "\"nextShowTime\": 1639670399\n" +
                    "}")
        }

        private val vb by lazy { Test1Binding.inflate(LayoutInflater.from(context)) }

        init {
            setContentView(vb.root)

            setCanceledOnTouchOutside(ob.optBoolean("clickBgHide", true))
            val w = context.getScreenWidth() * 0.78F
            var h = w * 427F / 290 // 原本比例宽高
            val w1 = ob.optInt("width")
            val h1 = ob.optInt("height")
            if (w1 > 0 && h1 > 0) {
                h = w * h1 / w1
            }
            vb.lyRoot.updateLayoutParams<FrameLayout.LayoutParams> {
                width = w.toInt()
                gravity = Gravity.CENTER
            }
            vb.lyContainer.updateLayoutParams {
                width = w.toInt(); height = h.toInt()
            }
            vb.ivBg.glide(ob.optString("bg").also {  }) { scaleType = ImageView.ScaleType.CENTER_CROP }
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
                        dismiss()
                        it.context.open(btn.optString("url"))
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

}
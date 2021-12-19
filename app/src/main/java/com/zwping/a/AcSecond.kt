package com.zwping.a

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.zwping.a.databinding.AcMainBinding
import com.zwping.alibx.*
import org.json.JSONObject


/**
 *
 * zwping @ 2021/11/22
 */
class AcSecond: BaseAc<AcMainBinding>() {
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    val prefix by lazy { intent?.getStringExtra("prefix") }
    val item by lazy { intent?.getStringExtra("item") }

    override fun initView() {
        val us = mutableListOf<String>()

        item?.split(",")?.forEach { us.add("$prefix$it") }

        val u = "https://pic2.zhimg.com/80/v2-df7fd62e3fffcd9c14c9f82db55a9a3d_1440w.jpg"
        vb.layerFindTeacher.start(this, u, us)
        val htime = 1000
        ITimer({ vb.lyFindTeacher.visibility = View.GONE; vb.layerFindTeacher.stop() }, 5000L-htime).schedule(this)

        vb.stateLayout.init { getData() }
        getData()
    }

    private fun getData() {
        val ob = JSONObject("{\n" +
                "\"name\": \"邀请你参加名师测评\",\n" +
                "\"desc\": \"要艺考？先测评！\",\n" +
                "\"remark\": \"名师测评功能通过AI智能算法，帮你找到适合你的艺考名师，预约测评后，名师能够帮你：\",\n" +
                "\"enum\": \"1.对你的专业能力进行评估；\\n2.测试你适合的艺考方向；\\n3.解答你的艺考相关困惑；\\n4.获取专业能力提升建议。\",\n" +
                "\"image\": \"https://img.yikaoapp.com/icon/test/718_test_bg.png\",\n" +
                "\"button\": {\n" +
                "\"title\": \"立即参加\",\n" +
                "\"bg\": \"#0A84FF\",\n" +
                "\"color\": \"white\",\n" +
                "\"url\": \"yikao://test/scanning?fr=my_user&prefix=http%3A%2F%2Fimg.yikaoapp.com%2Ficon%2Ftest%2FscanningAvatar%2F&item=1.jpg,2.jpg,3.jpg,4.jpg,5.jpg\"\n" +
                "},\n" +
                "\"tips\": \"已有<font color='#0A84FF'>2496732</font>人参加测评\"\n" +
                "}")
        val entity = Entity(ob)

        vb.tvTitle.text = entity.name
        vb.tvTitle2.text = entity.desc
        vb.tvContent.text = entity.remark
        vb.tvContent2.text = entity.enum
        vb.ivCover.glide(entity.image)
        entity.button?.also {
            vb.btnStart.text = it.title
            val bg = try { Color.parseColor(it.bg) } catch (e: Exception) { (0xff0a84ff).toInt() }
            vb.btnStart.backgroundTintList = ColorStateList.valueOf(bg)
            val tc = try { Color.parseColor(it.color) } catch (e: Exception) { Color.WHITE }
            vb.btnStart.setTextColor(tc)
            vb.btnStart.setOnClickThrottleListener {
                it.context.open(AcThree::class.java)
            }
        }
        vb.tvTips.text = Html.fromHtml("${entity.tips}")
        vb.stateLayout.showContentView()
    }

    private class Entity(ob: JSONObject?=null): IJson(ob, true) {
        var name: String? = null
        var desc: String? = null
        var remark: String? = null
        var enum: String? = null
        var image: String? = null
        var tips: String? = null
        var button: Button? = null

        class Button(ob: JSONObject? = null): IJson(ob, true) {
            var title: String? = null
            var bg: String? = null
            var color: String? = null
            var url: String? = null
        }
    }
}
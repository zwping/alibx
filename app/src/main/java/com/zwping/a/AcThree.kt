package com.zwping.a

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.a.databinding.Test3Binding
import com.zwping.a.databinding.Test4Binding
import com.zwping.alibx.*
import com.zwping.alibx.IJson.Companion.optJSONArrayOrNull
import org.json.JSONArray
import org.json.JSONObject

/**
 *
 * zwping @ 2021/11/24
 */
class AcThree: BaseAc<Test3Binding>() {

    override fun initVB(inflater: LayoutInflater): Test3Binding? {
        return Test3Binding.inflate(layoutInflater)
    }
    override fun initView() {
        immersive(darkMode = true)
        vb.toolbar.addMarginTopStatusBarHeight()

//        vb.lySwitch.visibility = View.GONE

        vb.viewPager2.initBannerOfVB(
            { Test4Binding.inflate(it.getLayoutInflater(), it, false) },
            { vb, data : MutableList<Entity.Content>, position ->
                val entity = data[position]
                vb.ivCover.glide(entity.avatar)
                vb.tvName.text = entity.name
                vb.tvNum.background = createGradientDrawable {
                    setColor((0xff666666).toInt())
                    12F.dpToPx().also { cornerRadii = floatArrayOf(it, it, 0F, 0F, 0F, 0F, it, it) }
                }
                vb.tvNum.text = "预约${entity.number}人"
                entity.members?.forEachIndexed { index, s ->
                    when(index) {
                        0 -> vb.ivAvatar3.glide(s) { setStroke(0.5F, Color.WHITE); circleCrop() }
                        1 -> vb.ivAvatar2.glide(s) { setStroke(0.5F, Color.WHITE); circleCrop() }
                        2 -> vb.ivAvatar1.glide(s) { setStroke(0.5F, Color.WHITE); circleCrop() }
                    }
                }
                vb.lyContainer.removeAllViews()
                val dp15 = 15F.dp2px()
                entity.items?.forEach { item ->
                    FrameLayout(this).also { ly ->
                        ly.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                            it.bottomMargin = dp15
                        }
                        ImageView(ly.context).also {
                            it.layoutParams = FrameLayout.LayoutParams(dp15, dp15)
                            it.glide(item.icon)
                            ly.addView(it)
                        }
                        TextView(ly.context).also {
                            it.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).also {
                                it.leftMargin = 25F.dp2px()
                            }
                            it.textSize = 12F
                            it.setTextColor((0xff333333).toInt())
                            it.text = Html.fromHtml("${item.title}: ${item.value}")
                            ly.addView(it)
                        }
                        vb.lyContainer.addView(ly)
                    }
                }
            })
        vb.viewPager2.setBannerMultiItem(6F.dp2px(), 15F.dp2px())

        vb.btnSwitch.setOnClickListener {
            vb.btnSwitch.showLoading(enabled = true)
            DialogMingshicpSwitchMajor(it.context).show()
        }
        vb.stateLayout.init { getData() }
        getData()
    }

    private fun getData() {
        val cdata = JSONArray("[\n" +
                "{\n" +
                "\"name\": \"王振旭\",\n" +
                "\"avatar\": \"http://img.yikaoapp.com/upload/memberteacher/2651/200709130110146.jpg\",\n" +
                "\"url\": \"yikao://member/?id=28313922\",\n" +
                "\"test_url\": \"yikao://course/auditionClass_v2/?fr=&source_type=1&source_id=1&id=28313922\",\n" +
                "\"is_subscribe\": \"1\",\n" +
                "\"local\": \"\",\n" +
                "\"number\": \"96\",\n" +
                "\"members\": [\n" +
                "{\n" +
                "\"avatar\": \"http://img.yikaoapp.com/upload/member/28317087/2021070910073582024.jpg\"\n" +
                "},\n" +
                "{\n" +
                "\"avatar\": \"http://img.yikaoapp.com/upload/member/28725758/2021072320552890051.jpg\"\n" +
                "},\n" +
                "{\n" +
                "\"avatar\": \"http://img.yikaoapp.com/upload/member/28136847/2019122313032079057.jpg\"\n" +
                "}\n" +
                "],\n" +
                "\"items\": [\n" +
                "{\n" +
                "\"style\": \"test_teacher_item\",\n" +
                "\"title\": \"认证\",\n" +
                "\"value\": \"南昌高新区雅言文化艺术学校 · 播音教师\",\n" +
                "\"icon\": \"https://img.yikaoapp.com/icon/card/auth.png\"\n" +
                "},\n" +
                "{\n" +
                "\"style\": \"test_teacher_item\",\n" +
                "\"title\": \"毕业院校\",\n" +
                "\"value\": \"厦门理工学院\",\n" +
                "\"icon\": \"https://img.yikaoapp.com/icon/card/school.png\"\n" +
                "},\n" +
                "{\n" +
                "\"style\": \"test_teacher_item\",\n" +
                "\"title\": \"所读专业\",\n" +
                "\"value\": \"播音与主持艺术\",\n" +
                "\"icon\": \"https://img.yikaoapp.com/icon/card/special.png\"\n" +
                "},\n" +
                "{\n" +
                "\"style\": \"test_teacher_item\",\n" +
                "\"title\": \"教&#12288;&#12288;授\",\n" +
                "\"value\": \"播音（播音与主持艺术）\",\n" +
                "\"icon\": \"https://img.yikaoapp.com/icon/card/professor.png\"\n" +
                "},\n" +
                "{\n" +
                "\"style\": \"test_teacher_item\",\n" +
                "\"title\": \"所在地区\",\n" +
                "\"value\": \"江西 · 南昌\",\n" +
                "\"icon\": \"https://img.yikaoapp.com/icon/card/address.png\"\n" +
                "},\n" +
                "{\n" +
                "\"style\": \"test_teacher_item\",\n" +
                "\"title\": \"个人简介\",\n" +
                "\"value\": \"全台湾主播风暴大赛第二名，福建省新主持人大赛第三名，业内高级别夏青杯朗诵大赛二等奖\",\n" +
                "\"icon\": \"https://img.yikaoapp.com/icon/card/desc.png\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]")

        val data = cdata.optJSONArrayOrNull { Entity.Content(it) }
        data?.add(data[0])
        data?.add(data[0])

        vb.viewPager2.setBannerData(data)
        ValueAnimator.ofFloat(0F, 1.0F, 0F).also {
            it.duration = 1000
            it.interpolator = DecelerateInterpolator() // 减速度插值器
            it.addUpdateListener {
                val rv = vb.viewPager2.getChildAt(0) as RecyclerView
                val lm = rv.layoutManager as LinearLayoutManager
                val v = "${it.animatedValue}".toFloat() * 300
                lm.scrollToPositionWithOffset(2, v.toInt()*-1)
            }
            it.start()
//            handler.postDelayed({ it.start() }, 500)
        }
        vb.stateLayout.showContentView()


        vb.stateLayout.showEmptyView("没有找到和你信息匹配的老师\n" +
                "请更换专业再试试吧～")
    }


    private class Entity {

        class Content(ob: JSONObject?=null): IJson(ob, true) {
            var name: String? = null
            var avatar: String? = null
            var url: String? = null
            var test_url: String? = null
            var is_subscribe: String? = null
            var local: String? = null
            var number: String? = null
            var members: MutableList<String?>? = null

            var items: MutableList<Items>? = null

            init {
                if (members == null) members = mutableListOf()
                members?.clear()
                ob?.optJSONArray("members")?.forEach {
                    members?.add(it?.optString("avatar"))
                }
                members = members?.reversed() as MutableList<String?>?
            }
        }

        class Items(ob: JSONObject?=null): IJson(ob, true) {
            var style: String? = null
            var title: String? = null
            var value: String? = null
            var icon: String? = null
        }
    }

}
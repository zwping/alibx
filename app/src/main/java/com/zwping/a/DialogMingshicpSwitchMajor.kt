package com.zwping.a

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zackratos.ultimatebarx.ultimatebarx.navigationBarHeight
import com.zwping.a.databinding.Test5Binding
import com.zwping.a.databinding.Test6Binding
import com.zwping.alibx.*
import com.zwping.alibx.IJson.Companion.forEach
import org.json.JSONArray
import org.json.JSONObject

/**
 * 名师测评 专业切换 ~ 7.1.8
 * zwping @ 2021/12/19
 */
class DialogMingshicpSwitchMajor(context: Context?) : IDialog.Dialog(context) {

    private val vb by lazy { Test5Binding.inflate(rootLayout.getLayoutInflater(), rootLayout, false) }

    private val adp = AdapterMulti(Style.values())

    init {
        setContentView(vb.root)

        Bar.immersive(this, darkMode = true)
        vb.recyclerView.addMarginTopStatusBarHeight()
        vb.lyRoot.background = createGradientDrawable {
            setColor(Color.WHITE)
            15F.dpToPx().also { cornerRadii = floatArrayOf(it, it, 0F,0F, 0F, 0F, it, it) }
        }
        showAnimator = { AnimHelper.slideRightLeftIn(it) }
        hideAnimator = { AnimHelper.slideLeftRightOut(it) }

        vb.btnCancel.setOnClickListener { dismiss() }
        vb.btnConfirm.setOnClickListener {  }

        vb.recyclerView.layoutManager = GridLayoutManager(context, 2).also {
            it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adp.datas[position].itemViewType == Style.Dir) 2 else 1
                }
            }
        }
        vb.recyclerView.adapter = adp
        adp.onItemClickListener = { view, entity, index ->
            when (entity.itemViewType) {
                Style.Dir -> {
                    entity as Entity.Dir
                    entity.selected = entity.selected != true
                    adp.datas.filter { it.itemViewType == Style.Major && (it as Entity.Major).dirid == entity.id }
                        .forEach { (it as Entity.Major).selected = entity.selected }
                    adp.notifyDataSetChanged()
                }
                Style.Major -> {
                    entity as Entity.Major
                    entity.selected = entity.selected != true
                    val data = adp.datas.filter { it.itemViewType == Style.Major && (it as Entity.Major).dirid == entity.dirid }
                    adp.datas.filter { it.itemViewType == Style.Dir && (it as Entity.Dir).id == entity.dirid }
                        .forEach { (it as Entity.Dir).selected = data.size == data.filter { (it as Entity.Major).selected == true }.size }
                    adp.notifyDataSetChanged()
                }
            }
        }
        setData()
    }

    private fun setData() {

        val arr = JSONArray("""[
{
"id": "001",
"name": "播音",
"selected": true,
"child": [
{
"id": "00101",
"name": "播音与主持艺术",
"selected": true
},
{
"id": "00102",
"name": "双语播音",
"selected": false
},
{
"id": "00103",
"name": "礼仪文化",
"selected": false
},
{
"id": "00104",
"name": "配音方向",
"selected": false
},
{
"id": "00105",
"name": "综艺主持",
"selected": false
},
{
"id": "00106",
"name": "电子竞技解说",
"selected": false
},
{
"id": "00107",
"name": "体育解说",
"selected": false
}
]
},
{
"id": "002",
"name": "编导",
"selected": false,
"child": [
{
"id": "00201",
"name": "戏剧影视导演",
"selected": false
},
{
"id": "00202",
"name": "艺术与科技",
"selected": false
},
{
"id": "00203",
"name": "电影学",
"selected": false
},
{
"id": "00204",
"name": "电子竞技",
"selected": false
},
{
"id": "00205",
"name": "数字媒体艺术",
"selected": false
},
{
"id": "00206",
"name": "戏剧影视文学",
"selected": false
},
{
"id": "00207",
"name": "戏剧学",
"selected": false
},
{
"id": "00208",
"name": "广播电视编导",
"selected": false
},
{
"id": "00209",
"name": "制片与管理",
"selected": false
}
]
},
{
"id": "003",
"name": "表演",
"selected": false,
"child": [
{
"id": "00301",
"name": "戏剧影视表演",
"selected": false
},
{
"id": "00302",
"name": "戏曲表演",
"selected": false
},
{
"id": "00303",
"name": "木偶剧表演",
"selected": false
},
{
"id": "00304",
"name": "舞蹈表演",
"selected": false
},
{
"id": "00305",
"name": "音乐剧表演",
"selected": false
}
]
},
{
"id": "004",
"name": "摄影",
"selected": false,
"child": [
{
"id": "00401",
"name": "图片摄影",
"selected": false
},
{
"id": "00402",
"name": "摄影",
"selected": false
},
{
"id": "00403",
"name": "影视摄影与制作",
"selected": false
}
]
},
{
"id": "005",
"name": "美术",
"selected": false,
"child": [
{
"id": "00501",
"name": "艺术设计学",
"selected": false
},
{
"id": "00502",
"name": "美术学",
"selected": false
},
{
"id": "00503",
"name": "公共艺术",
"selected": false
},
{
"id": "00504",
"name": "戏剧影视美术",
"selected": false
},
{
"id": "00505",
"name": "舞台美术",
"selected": false
},
{
"id": "00506",
"name": "数字媒体艺术",
"selected": false
},
{
"id": "00507",
"name": "新媒体艺术",
"selected": false
},
{
"id": "00508",
"name": "服装与服饰设计",
"selected": false
},
{
"id": "00509",
"name": "产品设计",
"selected": false
},
{
"id": "00510",
"name": "环境设计",
"selected": false
},
{
"id": "00511",
"name": "视觉传达设计",
"selected": false
},
{
"id": "00512",
"name": "动漫动画",
"selected": false
},
{
"id": "00513",
"name": "绘画",
"selected": false
},
{
"id": "00514",
"name": "工艺美术",
"selected": false
},
{
"id": "00515",
"name": "雕塑",
"selected": false
}
]
},
{
"id": "006",
"name": "舞蹈",
"selected": false,
"child": [
{
"id": "00601",
"name": "舞蹈学",
"selected": false
},
{
"id": "00602",
"name": "舞蹈编导",
"selected": false
},
{
"id": "00603",
"name": "舞蹈表演",
"selected": false
}
]
},
{
"id": "007",
"name": "音乐",
"selected": false,
"child": [
{
"id": "00701",
"name": "乐器表演",
"selected": false
},
{
"id": "00702",
"name": "作曲及作曲理论",
"selected": false
},
{
"id": "00703",
"name": "声乐表演",
"selected": false
},
{
"id": "00704",
"name": "录音艺术",
"selected": false
},
{
"id": "00705",
"name": "音乐学",
"selected": false
},
{
"id": "00706",
"name": "音乐表演",
"selected": false
}
]
},
{
"id": "008",
"name": "服表",
"selected": false,
"child": [
{
"id": "00801",
"name": "服饰设计与表演",
"selected": false
}
]
},
{
"id": "009",
"name": "书法",
"selected": false,
"child": [
{
"id": "00901",
"name": "书法学",
"selected": false
}
]
},
{
"id": "010",
"name": "空乘",
"selected": false,
"child": [
{
"id": "01001",
"name": "空中乘务",
"selected": false
}
]
}
]""")
        val data = mutableListOf<ItemViewType>()
        arr.forEach {
            val dir = Entity.Dir(it).also { it.child?.forEach { m -> m.dirid = it.id } }
            data.add(dir)
            dir.child?.forEach { data.add(it) }
        }

        adp.setData(data)
    }


    private class Entity {
        class Dir(ob: JSONObject?): IJson(ob, true), IItemViewType {
            override val itemViewType: Enum<*> = Style.Dir

            var id: String? = null
            var name: String? = null
            var selected: Boolean? = null
            var child: MutableList<Major>? = null
        }

        class Major(ob: JSONObject?): IJson(ob, true), IItemViewType {
            override val itemViewType: Enum<*> = Style.Major

            var id: String? = null
            var name: String? = null
            var selected: Boolean? = null
            var dirid: String? = null
        }
    }

    private enum class Style: IViewHolderEnum {
        Dir {
            override fun holder(parent: ViewGroup) = BaseViewHolderVB(
                Test6Binding.inflate(parent.getLayoutInflater(), parent, false),
                { vb, entity: Entity.Dir ->
                    vb.tvTitle.text = entity.name
                    vb.iv.setBackgroundColor(if (entity.selected == true) Color.RED else Color.YELLOW)
                }
            )
        },
        Major {
            override fun holder(parent: ViewGroup) = BaseViewHolder(
                TextView(parent.context).also {
                    it.layoutParams = RecyclerView.LayoutParams(-1, 32F.dp2px()).also {
                        it.topMargin = 15F.dp2px()
                        5F.dp2px().also { d -> it.leftMargin = d; it.rightMargin = d; }
                    }
                    it.setPadding(5F.dp2px(), 0, 5F.dp2px(), 0)
                    it.textSize = 12F; it.gravity = Gravity.CENTER
                    it.isSingleLine = true
                    it.setTextColor(ColorStateList2((0xff222222).toInt()) { selected((0xff0a84ff).toInt()) }.create())
                    it.background = createStateListDrawable(createGradientDrawable {
                        cornerRadius=4F.dpToPx()
                        setStroke(0.5F.dp2px(), (0xffF6F6F6).toInt())
                        setColor((0xffF6F6F6).toInt())
                    }) { it.selected(createGradientDrawable {
                        cornerRadius=4F.dpToPx()
                        setStroke(0.5F.dp2px(), (0xffCFE2FF).toInt())
                        setColor((0xffF0F6FF).toInt())
                    }) }
                }, { view, entity: Entity.Major ->
                    view.text = entity.name
                    view.isSelected = entity.selected == true
                }
            )
        }
    }
}
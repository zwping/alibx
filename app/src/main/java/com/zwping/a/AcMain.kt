package com.zwping.a

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.transition.Hold
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.render.GSYRenderView
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.zackratos.ultimatebarx.ultimatebarx.addStatusBarTopPadding
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.*
import com.zwping.alibx.IJson.Companion.optJSONArrayOrNull
import org.json.JSONArray
import org.json.JSONObject
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager


class AcMain : BaseAc<AcMainBinding>() {
//
    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    override fun initView() {
        vb.tv1.setOnClickListener {
            IDialog.DialogIOS(this).show()
        }

        val data = mutableListOf<D1>().apply { repeat(100) { add(D1(it, Style.s1)) } }
        vb.recyclerView1.layoutManager = LinearLayoutManager(this)
        vb.recyclerView2.layoutManager = LinearLayoutManager(this)
        vb.recyclerView1.adapter = BaseAdapterQuick{ Holder1(it) }.also { it.setData(data) }

        val data1 = mutableListOf<ItemViewType>()
        repeat(10) { data1.add(D1(it, Style.s1)) }
        repeat(10) { data1.add(D1(it, Style.s2)) }
        vb.recyclerView2.adapter = BaseAdapterMultiQuick(Style.values()).apply { setData(data1) }
    }

    data class D1(var s1: Int, override val itemViewType: Enum<*>): IItemViewType

    enum class Style: IEnumViewHolder {
        s1 {
            override fun holder(parent: ViewGroup): BaseViewHolder<out IItemViewType, View> {
                return Holder1(parent)
            }
        }, s2 {
            override fun holder(parent: ViewGroup): BaseViewHolder<out IItemViewType, View> {
                return Holder2(parent)
            }
        }
    }

    class Holder1(parent: ViewGroup): BaseViewHolder<D1, TextView>(
        TextView(parent.context),
        { view, entity -> view.text = "$entity" }
    ), ItemViewType {
        override val itemViewType: Enum<*> = Style.s1
    }
    class Holder2(parent: ViewGroup): BaseViewHolder<D1, TextView>(
        TextView(parent.context),
        { view, entity -> view.text = "--$entity" }
    ), IItemViewType {
        override val itemViewType: Enum<*> = Style.s2
    }
}

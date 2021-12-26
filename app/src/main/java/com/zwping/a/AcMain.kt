package com.zwping.a

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.*


class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    override fun initView() {
        vb.banner.scrollTime = 1600
        vb.banner.setAdapter(object: BannerAdapter<String, BaseViewHolderVB<String, Test1Binding>>(){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BaseViewHolderVB<String, Test1Binding> {
                return BaseViewHolderVB(
                    Test1Binding.inflate(parent.getLayoutInflater(), parent, false),
                    { vb, entity ->

                    }
                )
            }
            override fun onBindViewHolder2(
                holder: BaseViewHolderVB<String, Test1Binding>,
                position: Int
            ) {

            }
        })
        vb.banner.viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
        vb.banner.setDatas(mutableListOf("123", "123", "123"))

        vb.recyclerView.layoutManager = LinearLayoutManager(this)
        vb.recyclerView.adapter = AdapterMulti<Style>(Style.values()).also {
            it.setData(mutableListOf<ItemViewType>().also {
            })
        }
        logd(123)

    }

    enum class Style: IEnumViewHolder{
        t1 {
            override fun holder(parent: ViewGroup): BaseViewHolder<out ItemViewType, View> {
                return Holder1(parent)
            }
        },
        t2 {
            override fun holder(parent: ViewGroup): BaseViewHolder<out ItemViewType, View> {
                return Holder2(parent)
            }
        },
    }

    class Entity : ItemViewType{
        override val itemViewType: Enum<*>
            get() = Style.t1
    }
    class Entity2 : ItemViewType{
        override val itemViewType: Enum<*>
            get() = Style.t2
    }

    class Holder1(parent: ViewGroup) : BaseViewHolder<Entity, TextView>(TextView(parent.context).also {
        it.layoutParams = RecyclerView.LayoutParams(-1, 50F.dp2px())
        it.setBackgroundColor(Color.RED)
    }, { tv, entity -> }) {
        override fun onAttach() {
            logd(1111)
        }

        override fun onDetached() {
            logd(2222)
        }
    }

    class Holder2(parent: ViewGroup) : BaseViewHolder<Entity2, TextView>(TextView(parent.context).also {
        it.layoutParams = RecyclerView.LayoutParams(-1, 50F.dp2px())
        it.setBackgroundColor(Color.DKGRAY)
    }, { tv, entity -> }) {

        override fun onAttach() {
            logd(3333)
        }

        override fun onDetached() {
            logd(4444)
        }
    }

}

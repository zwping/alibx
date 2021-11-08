package com.zwping.a

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.zwping.a.MainActivity.Style.*
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.*

class MainActivity : BaseAc<ActivityMainBinding>() {

    enum class Style {
        title, content
    }

    class Title:IJson(), ItemViewType{
        override var itemViewType: Enum<*> = Style.title
        var title = "1212222"
    }
    class Content:IJson(), ItemViewType{
        override var itemViewType: Enum<*> = Style.content
        var content = "content"
    }

    private val adp by lazy {
        object: BaseAdapterMulti<ItemViewType, Style>(values()){
            override fun onCreateViewHolder(parent: ViewGroup, enum: Style): BaseViewHolder<ItemViewType, View> {
                return when(enum){
                    Style.title -> BaseViewHolder(parent.getLayoutInflater(R.layout.test1),
                        { view, entity ->  view.setBackgroundColor(Color.RED);
                            view.findViewById<TextView>(R.id.tv).text = (entity as Title).title})
                    content -> BaseViewHolderVB(Test1Binding.inflate(parent.getLayoutInflater(), parent, false),
                        {vb, entity ->
                            println("$this")
                            entity as Content
                            vb.tv.text = entity.content
                        })
                }
            }
        }
//        object: BaseAdapter<String>() {
//            override fun getItemViewType(position: Int): Int {
//                return position
//            }
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String, View> {
//                return when(viewType) {
//                    // 0 -> BaseViewHolder(TextView(parent.context).apply { layoutParams = parent.layoutParams }, { view, entity ->  view.setBackgroundColor(Color.RED); view.text = entity})
//                    0 -> BaseViewHolder(parent.getLayoutInflater(R.layout.test1), { view, entity ->  view.setBackgroundColor(Color.RED); view.findViewById<TextView>(R.id.tv).text = entity})
//                    else -> BaseViewHolderVB(Test1Binding.inflate(parent.getLayoutInflater(), parent, false),
//                        {vb, entity -> vb.tv.text = entity })
//                }
//            }
//
//        }
    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    class Entity: IJson(), ItemViewType{
        override var itemViewType: Enum<*> = title
    }

    override fun initView() {

        vb.recyclerView.layoutManager = LinearLayoutManager(this)
        vb.recyclerView.adapter = adp

        adp.setData(mutableListOf(Title(), Title(), Title()))
        handler.postDelayed({
            adp.addData(mutableListOf(Content(), Content(), Content()))
        }, 2000)
    }

}
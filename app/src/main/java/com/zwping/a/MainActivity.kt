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
import org.json.JSONObject

class MainActivity : BaseAc<ActivityMainBinding>() {

    enum class Style {
        title, content
    }
    interface  Test {}

    class Title:IJson(), ItemViewType{
        override var itemViewType: Enum<*> = Style.title
        var title = "1212222"
    }
    class Content:IJson(), ItemViewType{
        override var itemViewType: Enum<*> = Style.content
        var content = "content"
    }
    companion object {
    }
    class Items(ob:JSONObject?): Test, IJson(ob, true){
        var item: String?=null
        override fun toString(): String {
            return "Items(item=$item)"
        }
    }
    class Bean(ob:JSONObject?): Test, IJson(ob, true){
        var title: String?=null
        var items: MutableList<Items>?=null
      var item: Items?=null
        override fun toString(): String {
            return "Bean(title=$title, items=$items, item=$item)"
        }
        init {
            println(_log)
        }
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
                            println("${this.vb}")
                            entity as Content
                            vb.tv.text = entity.content
                        })
                }
            }
        }
    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    class Entity: IJson(), ItemViewType{
        override var itemViewType: Enum<*> = title
    }

    override fun initView() {
        println(Bean(JSONObject("{'title':111; 'item':{'item':444}; 'items':[{'item':222}, {'item':333}]}")))

        vb.recyclerView.layoutManager = LinearLayoutManager(this)
        vb.recyclerView.adapter = adp

        adp.setData(mutableListOf(Title(), Title(), Title()))
        handler.postDelayed({
            adp.addData(mutableListOf(Content(), Content(), Content()))
        }, 2000)
    }

}
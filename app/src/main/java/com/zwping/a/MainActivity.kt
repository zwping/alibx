package com.zwping.a

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.zwping.a.databinding.ActivityMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.alibx.*

class MainActivity : BaseAc<ActivityMainBinding>() {

    private val adp by lazy {
        object: BaseAdapter<String>() {
            override fun getItemViewType(position: Int): Int {
                return position
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String, View> {
                return when(viewType) {
                    // 0 -> BaseViewHolder(TextView(parent.context).apply { layoutParams = parent.layoutParams }, { view, entity ->  view.setBackgroundColor(Color.RED); view.text = entity})
                    0 -> BaseViewHolder(parent.getLayoutInflater(R.layout.test1), { view, entity ->  view.setBackgroundColor(Color.RED); view.findViewById<TextView>(R.id.tv).text = entity})
                    else -> BaseViewHolderVB(Test1Binding.inflate(parent.getLayoutInflater(), parent, false),
                        {vb, entity -> vb.tv.text = entity })
                }
            }

        }
    }

    override fun initVB(inflater: LayoutInflater): ActivityMainBinding? {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        vb.recyclerView.layoutManager = LinearLayoutManager(this)
        vb.recyclerView.adapter = adp

        adp.setData(mutableListOf("1", "2", "3"))
        handler.postDelayed({
            adp.addData(mutableListOf("1", "2", "3"))
        }, 2000)
    }

}
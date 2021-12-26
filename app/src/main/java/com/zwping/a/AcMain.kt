package com.zwping.a

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        // vb.banner.scrollTime = 1600
        vb.banner.setAdapterImage({ iv, entity ->
            (iv as ImageView).glide(entity.toString()) {
//                setRadius(12F.dpToPx());
                scaleType = ImageView.ScaleType.CENTER_CROP
//                placeHolder = -1
            }
        })
        vb.banner.setDatas(mutableListOf(
            "http://img.yikaoapp.com/upload/adfocus/1908/21121718210828.png",
            "http://img.yikaoapp.com/upload/adfocus/1569/21032615481859.png",
            "http://img.yikaoapp.com/upload/adfocus/1929/21122417093960.png",
            "http://img.yikaoapp.com/upload/adfocus/1468/21121617460180.jpg",
            "http://img.yikaoapp.com/upload/adfocus/1838/21111511391550.png",
        ))
    }

}

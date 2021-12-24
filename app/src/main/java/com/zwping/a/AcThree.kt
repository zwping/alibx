package com.zwping.a

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    }

}
package com.zwping.a

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.animation.AnimatorSetCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.a.databinding.TestBinding
import com.zwping.a.fm.FmHome
import com.zwping.alibx.*
import razerdp.util.animation.AnimationHelper

class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }


    private val fmHome by lazy { FmHome() }
    override fun initView() {
        vb.iv.glide("https://p.qqan.com/up/20211-11/20211123931516611.jpg") {
            circleCrop()
            stroke = IImgLoaderOpt.Stroke(1F, (0xffE1EBF5).toInt())
        }
        vb.layerFindTeacher.start(this, mutableListOf(
            "https://img2.woyaogexing.com/2021/12/04/c48b8378a714452fb878e96fb0927afb!400x400.jpg",
            "https://img2.woyaogexing.com/2021/12/04/c3f26419f3284de79b4d293090568e46!400x400.jpeg",
            "https://img2.woyaogexing.com/2021/12/04/fb461641da2e49cfb0291a4736c3b475!400x400.jpeg",
        ))
        handler.postDelayed({
            vb.layerFindTeacher.visibility = View.GONE
        }, 5000)


        vb.viewPager2.initBannerOfImg<String>({iv, data, position ->
            iv.glide(data[position])
        })
        vb.viewPager2.setBannerMultiItem(6F.dp2px(), 15F.dp2px())
        vb.viewPager2.setBannerData(mutableListOf(
            "https://img2.woyaogexing.com/2021/12/04/c3f26419f3284de79b4d293090568e46!400x400.jpeg",
            "https://img2.woyaogexing.com/2021/12/04/c48b8378a714452fb878e96fb0927afb!400x400.jpg",
            "https://img2.woyaogexing.com/2021/12/04/fb461641da2e49cfb0291a4736c3b475!400x400.jpeg",
        ))
//        vb.viewPager2.scrollTo(10, 0)
        ValueAnimator.ofFloat(0F, 1.0F, 0F).also {
            it.duration = 1000
            it.interpolator = DecelerateInterpolator() // 减速度插值器
            it.addUpdateListener {
                val rv = vb.viewPager2.getChildAt(0) as RecyclerView
                val lm = rv.layoutManager as LinearLayoutManager
                val v = "${it.animatedValue}".toFloat() * 300
                lm.scrollToPositionWithOffset(2, v.toInt()*-1)
            }
//            it.start()
            handler.postDelayed({ it.start() }, 500)
        }

        handler.postDelayed({
//            val rv = vb.viewPager2.getChildAt(0) as RecyclerView
//            val lm = rv.layoutManager as LinearLayoutManager
//            lm.scrollToPositionWithOffset(3, 100)

//            IDialog.DialogBottomSheet(this).also {
//                it.setContentView(R.layout.test)
//            }.show()
        }, 2000)

        open("alibx://ac/sec")

        vb.child.setOnClickListener {

            IDialog.DialogIOS(this, {
                it.canceledOnTouchOutSide = false
                it.showAnimator = { AnimHelper.slideBottomTopIn(it) }
                it.hideAnimator = { AnimHelper.slideTopBottomOut(it) }
            })
                .setIOSTitle("Title")
                .setIOSMessage("Message")
                .setIOSBtnCancel {  }
                .setIOSBtnConfirm { it.dismiss() }
                .show(supportFragmentManager)
        }
//        IDialog.DialogItemsBuilder(this, mutableListOf( "me")).show(supportFragmentManager)

    }


    enum class Style: IEnumViewHolder{
        Title {
            override fun holder(parent: ViewGroup) = BaseViewHolderVB<Entity, TestBinding>(
                TestBinding.inflate(parent.getLayoutInflater(), parent, false),
                {view, entity ->
                    itemView.background = createGradientDrawable {
                        setColor(Color.WHITE)
                        setStroke(1F.dp2px(), Color.RED) }
                }
            )
        },
        Title2 {
            override fun holder(parent: ViewGroup) = BaseViewHolderVB<Entity2, Test1Binding>(
                Test1Binding.inflate(parent.getLayoutInflater(), parent, false),
                {view, entity ->  }
            )
        }
    }
    class Entity:ItemViewType{
        override val itemViewType: Enum<*> = Style.Title
    }
    class Entity2:ItemViewType{
        override val itemViewType: Enum<*> = Style.Title2
    }




}
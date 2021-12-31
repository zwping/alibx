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


/**
 * 类似抖音的竖屏videoView, 适用于
 */
class PortraitVideoView(context: Context?, attrs: AttributeSet?) : StandardGSYVideoPlayer(context, attrs) {

    private val tvDesc: TextView by lazy { findViewById(R.id.tv_desc) }

    fun setTitle(title: String) {
        tvDesc.setOnClickListener {
            if (!isInPlayingState) return@setOnClickListener
            if (mBottomContainer?.visibility == View.VISIBLE)
                changeUiToPlayingClear()
            else
                changeUiToPlayingShow()
        }
        tvDesc.text = title
        var s1 = SpanUtils().append(title).create()
        val s2 = SpanUtils().append(title).append("收起").setClickSpan((0xffcccccc).toInt(), false){ tvDesc.text = s1 }.create()
        tvDesc.post {
            val lineCount = tvDesc.lineCount
            if (lineCount <= 2) return@post
            val layout = tvDesc.layout
            val line2 = "${title.subSequence(layout.getLineStart(0), layout.getLineEnd(1))}..." // 前二行内容
            val expand1 = "展开内容"
            val w1 = ViewUtil.measureTextWidth(expand1, 14F.dpToPx())[0]
            var measureW = 0
            for(index in line2.length-4 downTo 0) {
                measureW += ViewUtil.measureTextWidth("${line2[index]}", 14F.dpToPx())[0]
                if (measureW >= w1) {
                    s1 = SpanUtils().append(line2.subSequence(0, index)).append("...")
                        .append("展开内容").setClickSpan((0xffcccccc).toInt(), false){
                            tvDesc.text = s2
                        }.create()
                    tvDesc.text = s1
                    tvDesc.movementMethod = LinkMovementMethod.getInstance()
                    break
                }
            }
        }
    }

    init {
        isAutoFullWithSize = true
        isReleaseWhenLossAudio = false
        setIsTouchWiget(false)
        // GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL)
        titleTextView.visibility = View.GONE
        backButton.visibility = View.GONE
        fullscreenButton.visibility = View.GONE
        mTopContainer.alpha = 0F
        mBottomProgressBar.alpha = 0F
        mBottomContainer.setBackgroundColor(Color.TRANSPARENT)
        mBottomContainer.updateLayoutParams<MarginLayoutParams> { bottomMargin = 70F.dp2px() }
        mProgressBar.setColors((0x1affffff).toInt(), Color.WHITE, (0x4dffffff).toInt(), 3F.dpToPx(), 3F.dp2px())
        mProgressBar.thumb = createGradientDrawable { 12F.dp2px().also { this.setSize(it, it); setColor(Color.WHITE); cornerRadius = 6F.dpToPx()  } }
        mCurrentTimeTextView.textSize = 14f
        mTotalTimeTextView.textSize = 14f
    }

    override fun getLayoutId(): Int { return R.layout.video_view_portrait }
    override fun updateStartImage() {
        if (mStartButton is ImageView && mCurrentState != CURRENT_STATE_PLAYING) {
            (mStartButton as ImageView).setImageResource(R.mipmap.icon_video_player_720)
        } else super.updateStartImage()
    }

}

class AcMain : BaseAc<AcMainBinding>() {


    class Holder(parent: ViewGroup): BaseViewHolderVB<VideoItem, Test1Binding>(
        Test1Binding.inflate(parent.getLayoutInflater(), parent, false).also {
        },
        { vb, entity ->
            vb.videoPlayer.setTitle("${entity.title}")
            vb.ivAvatar.glide(entity.avatar) { circleCrop(); setStroke(1F, Color.WHITE) }

            vb.videoPlayer.playTag = TAG
            vb.videoPlayer.playPosition = adapterPosition
            vb.videoPlayer.setUp(entity.video, false, "")
            vb.videoPlayer.thumbImageView = ImageView(vb.videoPlayer.context).also {
                it.glide(entity.cover) { scaleType = ImageView.ScaleType.CENTER_CROP }
            }
        }
    ) {
        companion object {
            val TAG = "ViewPager2PlayerTag"
        }

    }

    private val adp by lazy { object: AdapterQuick<VideoItem>( { Holder(it) }) {
        override fun onBindViewHolder(
            holder: BaseViewHolder<VideoItem, View>,
            position: Int,
            payloads: MutableList<Any>
        ) {
            super.onBindViewHolder(holder, position, payloads)
            holder as Holder
            payloads.forEach {
                when("$it") {
                    "payload" -> holder.vb.tvCollectNum.text = "123"
                }
            }
        }
    }}

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    override fun initView() {
        immersive()
        logd(Bar.isNavBarVisible(this), Bar.isFullScreenGesture(this), resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", "android")))
        logd(Bar.getNavBarHeight(this), UltimateBarX.getNavigationBarHeight())
        vb.toolbar.addStatusBarTopPadding()
        vb.toolbar.setNavigationIcon(R.drawable.video_small_close)

        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)

        vb.viewPager2.offscreenPageLimit = 2
        vb.viewPager2.adapter = adp
        vb.viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
        vb.viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                if (GSYVideoManager.instance().playTag == Holder.TAG) {
                    playPosition(position)
                }
                //大于0说明有播放
//                val playPosition = GSYVideoManager.instance().playPosition
//                if (playPosition >= 0 &&
//                    GSYVideoManager.instance().playTag == Holder.TAG &&
//                    position != playPosition) {
//                        playPosition(position)
//                }
            }
        })

        adp.setDataOrNull(JSONArray(arr).optJSONArrayOrNull { VideoItem(it) })
        vb.viewPager2.post { playPosition(0) }
        handler.postDelayed({
                            adp.notifyItemChanged(0, "payload")
        }, 2000)
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) return
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    private fun playPosition(position: Int) {
        if (adp.datas.isNullOrEmpty()) return
        val holder = (vb.viewPager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(position) as Holder
        holder.entity?.also {
            val h = it.height ?: 0;
            val w = it.width ?: 0
            GSYVideoType.setShowType(if (h>w && h-w>200) GSYVideoType.SCREEN_TYPE_FULL else GSYVideoType.SCREEN_TYPE_DEFAULT)
        }
        holder.vb.videoPlayer.startPlayLogic()
    }

    class VideoItem(ob: JSONObject?=null): IJson(ob, true) {
        var id: String?=null
        var title: String?=null
        var cover: String?=null
        var video: String?=null
        var width: Int?=null
        var height: Int?=null
        var duration: String?=null
        var name: String?=null
        var avatar: String?=null
        var nice_number: String?=null
        var collect_number: String?=null
        var reply_number: String?=null
        var url: String?=null
        var user_url: String?=null
    }

    var arr = """[
{
"id": "1",
"title": "确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？确定学会这个健身操就可以成功摆脱脂肪吗？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/1/a24483afbea8d995cda0988035a4b9d5.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/1/a24483afbea8d995cda0988035a4b9d5.mp4",
"width": "2160",
"height": "3840",
"duration": "00:34",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=1&subject_id=660011",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "2",
"title": "老师“互动式”表演，引导学生台词更真挚的表达。",
"cover": "https://seopic.699pic.com/photo/50029/2175.jpg_wh1200.jpg",
"video": "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
"width": "850",
"height": "400",
"duration": "00:34",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=2&subject_id=660012",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "3",
"title": "考上大学之后就爱看这种失控场面哈哈哈哈",
"cover": "http://img.yikaoapp.com/upload/shortVideo/3/6459e80ae2693daacd4df94db3878238.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://vjs.zencdn.net/v/oceans.mp4",
"width": "860",
"height": "400",
"duration": "00:32",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=3&subject_id=660013",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "4",
"title": "吉林艺考生说台词东北味频发，你也被口音困扰过吗？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/4/5174806dda56d0df561a2c1addcc3f7a.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/4/5174806dda56d0df561a2c1addcc3f7a.mp4",
"width": "2160",
"height": "3840",
"duration": "00:31",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=4&subject_id=660014",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "5",
"title": "老师的教导对于专业应该是锦上添花而不是扭转乾坤，同学们别偷懒啦！",
"cover": "http://img.yikaoapp.com/upload/shortVideo/5/545ae11a990de9f38d4adbf485f7217d.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/5/545ae11a990de9f38d4adbf485f7217d.mp4",
"width": "2160",
"height": "3840",
"duration": "00:29",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=5&subject_id=660015",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "6",
"title": "艺考证件照之营业式微笑~",
"cover": "http://img.yikaoapp.com/upload/shortVideo/6/4bedf4813bb9ea9d73442e77c3209bb7.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/6/4bedf4813bb9ea9d73442e77c3209bb7.mp4",
"width": "2160",
"height": "3840",
"duration": "00:12",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=6&subject_id=660016",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "7",
"title": "艺考老师表演约会前踩了狗屎是什么反应，你笑了吗？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/7/15196f09a9e6c715ba407152eff21035.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/7/15196f09a9e6c715ba407152eff21035.mp4",
"width": "2160",
"height": "3840",
"duration": "00:34",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=7&subject_id=660017",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "8",
"title": "来自“伯德小姐”",
"cover": "http://img.yikaoapp.com/upload/shortVideo/8/56f3841776cea80bb24c3297c52ac664.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/8/56f3841776cea80bb24c3297c52ac664.mp4",
"width": "2160",
"height": "3840",
"duration": "04:47",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=8&subject_id=660018",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "9",
"title": "艺考老师引导学生如何表达“痛恨”自己的情绪。",
"cover": "http://img.yikaoapp.com/upload/shortVideo/9/d70cb372a0f13fbbf929e954911bc13d.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/9/d70cb372a0f13fbbf929e954911bc13d.mp4",
"width": "2160",
"height": "3840",
"duration": "00:38",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=9&subject_id=660019",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "10",
"title": "艺考临近，你真的准备好自我介绍了吗？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/10/65634c59ce49efaad1b8144b63895836.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/10/65634c59ce49efaad1b8144b63895836.mp4",
"width": "2160",
"height": "3840",
"duration": "00:43",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=10&subject_id=660020",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "11",
"title": "惊！！！艺考生竟然“光明正大”吃外卖，赶快",
"cover": "http://img.yikaoapp.com/upload/shortVideo/11/651c823608a5da41de9513c299ca74b5.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/11/651c823608a5da41de9513c299ca74b5.mp4",
"width": "2160",
"height": "3840",
"duration": "00:36",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=11&subject_id=660021",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "12",
"title": "艺考生的情绪训练有多“硬核”？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/12/7a896d2b18438920142fdadd5cecb8d2.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/12/7a896d2b18438920142fdadd5cecb8d2.mp4",
"width": "2160",
"height": "3840",
"duration": "00:37",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=12&subject_id=660022",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "13",
"title": "老师引导学生更生活化的表演，促进孩子与观众之间的交流。",
"cover": "http://img.yikaoapp.com/upload/shortVideo/13/62d40855b862a801ef55ecfae8e2dad3.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/13/62d40855b862a801ef55ecfae8e2dad3.mp4",
"width": "2160",
"height": "3840",
"duration": "00:24",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=13&subject_id=660023",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "14",
"title": "老师完美复制学生下课后躺床上玩手机，这是不是屏幕前的你？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/14/c1a1fd8c0e35c586f32595673f6ee2bb.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/14/c1a1fd8c0e35c586f32595673f6ee2bb.mp4",
"width": "2160",
"height": "3840",
"duration": "00:31",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=14&subject_id=660024",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "15",
"title": "每年的艺考生都非常可爱，老师于凌晨四点在市区见到了孩子们。",
"cover": "http://img.yikaoapp.com/upload/shortVideo/15/75f97169e9a5314c481b17119ebc1095.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/15/75f97169e9a5314c481b17119ebc1095.mp4",
"width": "2160",
"height": "3840",
"duration": "00:08",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=15&subject_id=660025",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "16",
"title": "艺考倒计时62天，同学们不要因为贪吃因小失大哦~",
"cover": "http://img.yikaoapp.com/upload/shortVideo/16/e7e4ea6a8afc4211405361026dbfc6c5.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/16/e7e4ea6a8afc4211405361026dbfc6c5.mp4",
"width": "2160",
"height": "3840",
"duration": "00:33",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=16&subject_id=660026",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "17",
"title": "减肥绝不可松懈，同学们千万不要囤“肉肉”来抵御寒冬哦~",
"cover": "http://img.yikaoapp.com/upload/shortVideo/17/b6320eae6be2dc1fe1c75d950a1f7f22.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/17/b6320eae6be2dc1fe1c75d950a1f7f22.mp4",
"width": "2160",
"height": "3840",
"duration": "00:29",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=17&subject_id=660027",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "18",
"title": "用心诠释人物的性格特征，不要成为一个“台词机器”。",
"cover": "http://img.yikaoapp.com/upload/shortVideo/18/053213b6856db53eac78180464124527.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/18/053213b6856db53eac78180464124527.mp4",
"width": "2160",
"height": "3840",
"duration": "00:34",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=18&subject_id=660028",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "19",
"title": "怎样的表演才能更好地诠释人物的内心世界？",
"cover": "http://img.yikaoapp.com/upload/shortVideo/19/d9e7de5a5ebd1531a082054173eb6d0a.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/19/d9e7de5a5ebd1531a082054173eb6d0a.mp4",
"width": "2160",
"height": "3840",
"duration": "00:39",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=19&subject_id=660029",
"user_url": "yikao://member?id=2063761&type=3"
},
{
"id": "20",
"title": "艺考时间紧迫，同学们要调整好心态继续努力哦~",
"cover": "http://img.yikaoapp.com/upload/shortVideo/20/948b8e27d24c62f0bafd9752471cf780.mp4?x-oss-process=video/snapshot,t_1,f_jpg,w_2160,h_3840,m_fast,ar_auto",
"video": "http://img.yikaoapp.com/upload/shortVideo/20/948b8e27d24c62f0bafd9752471cf780.mp4",
"width": "2160",
"height": "3840",
"duration": "00:45",
"name": "小鹿艺考",
"avatar": "http://img.yikaoapp.com/upload/member/2063761/2018091320375690663.jpg",
"nice_number": "0",
"collect_number": "0",
"reply_number": "0",
"url": "yikao://shortVideo/detail?id=20&subject_id=660030",
"user_url": "yikao://member?id=2063761&type=3"
}
]"""
}

package com.zwping.a

import android.app.PendingIntent
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.NotificationUtils
import com.blankj.utilcode.util.SnackbarUtils
import com.blankj.utilcode.util.ToastUtils
import com.zwping.a.databinding.AcMainBinding
import com.zwping.a.databinding.Test1Binding
import com.zwping.a.fm.FmHome
import com.zwping.alibx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AcMain : BaseAc<AcMainBinding>() {

    override fun initVB(inflater: LayoutInflater): AcMainBinding? {
        return AcMainBinding.inflate(inflater)
    }

    private var toast: Toast? = null

    private val fmHome by lazy { FmHome() }
    override fun initView() {
        val id = 0x01
        NotificationUtils.notify(id, {param ->
            intent.putExtra("id", id);
            param.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("title")
                .setContentText("content text: $id")
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
        })
        lifecycleScope.launch(Dispatchers.IO) {
//            com.hjq.toast.ToastUtils.show("1111")
            ToastUtil.show("1111")
            // Toast.makeText(this@AcMain, "2222", Toast.LENGTH_SHORT).show()

            vb?.btn1?.setOnClickListener {
                ToastUtil.show("1111")
//                com.hjq.toast.ToastUtils.show("1111")
            }
            vb?.btn2?.setOnClickListener {
                Toast.makeText(it.context, "2222", Toast.LENGTH_SHORT).show()
            }
            vb?.btn3?.setOnClickListener {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            vb?.btn4?.setOnClickListener {
                handler.postDelayed({ ToastUtil.show("3333") }, 3000)
            }
        }
    }


}
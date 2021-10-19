package com.zwping.a

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.zwping.alibx.BaseAc

class MainActivity : BaseAc<ViewBinding>() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }

    override fun onCreate2() {
        setContentView(R.layout.activity_main)
    }
}
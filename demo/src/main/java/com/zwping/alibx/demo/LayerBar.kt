package com.zwping.alibx.demo

import com.zwping.alibx.*

/**
 *
 * zwping @ 2022/4/15
 */
class LayerBar(val acMain: AcMain) {
    init {
        acMain.apply {
            vb.lyContainer.addView(line("常见bar控制"))
            vb.lyContainer.addView(btn("全屏[×]") {
                var state = it.text.toString().contains("√")
                state = !state
                it.text = "全屏[${if (state) "√" else "×"}]"
                setFullScreen(state)
            })
            vb.lyContainer.addView(btn("StatusBar DarkMode[默认]") {
                var state = it.text.toString().contains("√")
                state = !state
                it.text = "StatusBar DarkMode[${if (state) "√" else "×"}]"
                setStatusBarDarkMode(state)
            })
            vb.lyContainer.addView(btn("NavBar DarkMode[默认]") {
                var state = it.text.toString().contains("√")
                state = !state
                it.text = "NavBar DarkMode[${if (state) "√" else "×"}]"
                setNavBarDarkMode(state)
            })
            vb.lyContainer.addView(btn("隐藏状态栏[×]") {
                var state = it.text.toString().contains("√")
                state = !state
                it.text = "隐藏状态栏[${if (state) "√" else "×"}]"
                setStatusBarHide(state)
            })
            vb.lyContainer.addView(line("只可activity设置沉浸式, fm只能操控bar"))
            vb.lyContainer.addView(btn("immersive") { immersive(); it.isEnabled = false })
        }
    }
}
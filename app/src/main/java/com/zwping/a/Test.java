package com.zwping.a;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.zwping.alibx.IJson;
import com.zwping.alibx.ItemViewType;
import com.zwping.alibx.ViewKtx;
import com.zwping.alibx.ViewKtxKt;

import org.json.JSONObject;

/**
 * zwping @ 2021/11/10
 */
public class Test {

    public static class Bean extends IJson{
        public String title;

        public Bean(@Nullable JSONObject obj) {
            super(obj, true);
        }
    }

}

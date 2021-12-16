package com.zwping.a;

import android.content.Context;

import androidx.annotation.Nullable;


import com.zwping.alibx.DataStoreUtil;
import com.zwping.alibx.Util;

import org.json.JSONObject;

/**
 * zwping @ 2021/11/10
 */
public class Test {

    public static void ss(Context ctx) {
        DataStoreUtil.INSTANCE.get(ctx, "title");
    }

    public static class Bean{
        public int type;

        public Bean(@Nullable JSONObject obj) { }

        @Override
        public String toString() {
            return "Bean{" +
                    "type=" + type +
                    '}';
        }
    }

}

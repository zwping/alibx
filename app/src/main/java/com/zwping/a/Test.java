package com.zwping.a;

import android.app.ActivityOptions;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.zwping.alibx.Bus;
import com.zwping.alibx.DataStore;
import com.zwping.alibx.DataStoreKt;
import com.zwping.alibx.IJson;
import com.zwping.alibx.ItemViewType;
import com.zwping.alibx.ViewKtx;
import com.zwping.alibx.ViewKtxKt;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * zwping @ 2021/11/10
 */
public class Test {

    public static void ss(Context ctx) {
        DataStore.INSTANCE.get(ctx, "title");
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

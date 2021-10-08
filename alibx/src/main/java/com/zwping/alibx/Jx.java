package com.zwping.alibx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 实现ktx高阶函数特性
 * zwping @ 5/18/21
 */
public class Jx {

    public interface OnCommCallback<T> { void callback(@NonNull T it); }
    public interface OnCommCallback2<T, R> { R callback(@NonNull T it); }

    public static @Nullable <T> T also(T it, OnCommCallback<T> callback) {
        if (null != it) { callback.callback(it); }
        return it;
    }

    public static @Nullable <T, R> R with(@Nullable T it, @Nullable R defaultValue, OnCommCallback2<T, R> callback) {
        if (null != it) { return callback.callback(it); }
        return defaultValue;
    }

}

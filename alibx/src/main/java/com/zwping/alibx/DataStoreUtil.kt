package com.zwping.alibx

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

/**
 * DataStore扩展
 * zwping @ 1/11/21
 */
private interface IDataStore {
    fun get(ctx: Context?, key: String): String? // 提供string支持
    fun put(ctx: Context?, key: String, value: String?)
}
object DataStoreUtil: IDataStore {
    var NAME = "DataStore"

    override fun get(ctx: Context?, key: String): String? {
        ctx ?: return null
        return ctx.dataStore.get(key)
    }
    override fun put(ctx: Context?, key: String, value: String?) {
        ctx ?: return
        ctx.dataStore.put(key, value)
    }
}

/* ----------KTX----------- */

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreUtil.NAME)
inline fun <reified T: Any> DataStore<Preferences>.get(key: String): T? {
    var result: T? = null
    val preferencesKey = when(T::class) {
        Boolean::class -> booleanPreferencesKey(key)
        Int::class -> intPreferencesKey(key)
        Long::class -> longPreferencesKey(key)
        Double::class -> doublePreferencesKey(key)
        Float::class -> floatPreferencesKey(key)
        else -> stringPreferencesKey(key)
    }
    runBlocking { data.firstOrNull {
        // val res = it[preferencesKey]
        // if (res == null || res is T) result = res as T
        result = it[preferencesKey] as T // 类型强转存在crash风险, 但该风险在开发期间会被规避掉
        true
    } }
    return result
}
inline fun <reified T: Any> DataStore<Preferences>.put(key: String, value: T?) {
    runBlocking { edit {
        when(T::class) {
            Boolean::class -> it[booleanPreferencesKey(key)] = value as Boolean
            Int::class -> it[intPreferencesKey(key)] = value as Int
            Long::class -> it[longPreferencesKey(key)] = value as Long
            Double::class -> it[doublePreferencesKey(key)] = value as Double
            Float::class -> it[floatPreferencesKey(key)] = value as Float
            else -> it[stringPreferencesKey(key)] = "$value"
        }
    } }
}
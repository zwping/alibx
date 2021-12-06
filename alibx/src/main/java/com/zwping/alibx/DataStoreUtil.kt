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
internal interface IDataStore {
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
inline fun <reified T: Any> DataStore<Preferences>.get(key: String, default: T?=null): T? {
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
        it[preferencesKey]?.also { if (it is T) result = it }
        true
    } }
    return result ?: default
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
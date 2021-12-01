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
 * @lastTime 2021年11月01日17:18:22
 */
// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "DataStore")
inline fun <reified T: Any> DataStore<Preferences>.get(key: String): T? {
    var result: T? = null
    runBlocking { data.firstOrNull {
        result = when(T::class){
            Boolean::class -> it[booleanPreferencesKey(key)] as T
            Int::class -> it[intPreferencesKey(key)] as T
            Long::class -> it[longPreferencesKey(key)] as T
            Double::class -> it[doublePreferencesKey(key)] as T
            Float::class -> it[floatPreferencesKey(key)] as T
            else -> it[stringPreferencesKey(key)] as T
        }
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
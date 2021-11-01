package com.zwping.alibx

/**
 * DataStore扩展
 * zwping @ 1/11/21
 * @lastTime 2021年11月01日17:18:22
 */

//lateinit var dataStore: DataStore<Preferences>
//
//inline fun <reified BasicType : Any> DataStore<Preferences>.get(key: String, default: BasicType): BasicType {
//    var result = default
//
//    runBlocking { data.first { result =  it[preferencesKey<BasicType>(key)] ?: default; true}  }
//    return result
//}
//
//inline fun <reified BasicType : Any> DataStore<Preferences>.put(key: String, value: BasicType) {
//    runBlocking { edit { it[preferencesKey<BasicType>(key)] = value } }
//}
package com.zwping.alibx

import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.ParameterizedType


/**
 * 原生json解析扩充
 *
 * zwping @ 2020/9/25
 * @param obj 解析对象
 * @param autoReflexParse 自动解析简单对象
 *                        混淆必须加 { -keep public class * extends com.zwping.alibx.IJson { *; } }
 *                        alibx库已加混淆规则
 * @lastTime 2021年11月12日11:05:07
 */
abstract class IJson(obj: JSONObject?=null, autoReflexParse: Boolean=false) {

    var _log = StringBuilder() // 调试使用, 也许下个版本就找不到它了

    init {
        if (obj != null && autoReflexParse) {
            /* 根据变量名&变量属性 自动解析
            原理图: var [title]: {String}?=null 解析==> [title]=obj.opt{String}('[title]')  括号字段一一对称
            变量属性: 只支持3类
                        基础数据类型[String|Int|Boolean|Float|Double|Long]
                        IJson派生类
                        List<[基础数据类型|IJson派生类]>
            一、局限性
                1、只做一个简单的偷懒功能, 在kt中使用Java反射库, 以减少包大小和维护成本
                2、逆向可知public var变量最终会生成private+get+set存在,
                    这样一来class.fields()方法无法获取到kt继承类中所有public变量,
                    最终无法通过java反射库简单的获取所有变量,
                    终: 使用declaredFields()只解析当前类的所有变量。
                3、只能自动解析一级派生类,
                    如果仅依靠该自动解析方法, 那需要创建更多零散的Entity, 无疑会增加代码复杂度
                    但你的Entity是否更加耦合及更容易维护
                4、反射意味无法完全混淆, 安全度降低, 在这只保留:IJson类
                    -keep public class * extends com.zwping.alibx.IJson { *; }
                5、不支持变量默认值 (⊙︿⊙)
                    在new的过程中完成了解析, 默认值会在new完成后赋值
             */
            try {
                javaClass.declaredFields.forEach { f -> // 当前类(最终的派生类)的所有变量
                    _log.append("${f.name}\t${f.type}\t${f.genericType}\t \n")
                    try {
                        f.isAccessible=true
                        when (f.type) {
                            String::class.java -> f.set(this, obj.optString(f.name))
                            Int::class.java, Int::class.javaObjectType -> f.set(this, obj.optInt(f.name))
                            Boolean::class.java, Boolean::class.javaObjectType -> f.set(this, obj.optBoolean(f.name))
                            Float::class.java, Float::class.javaObjectType -> f.set(this, obj.optDouble(f.name).toFloat())
                            Double::class.java, Double::class.javaObjectType -> f.set(this, obj.optDouble(f.name))
                            Long::class.java, Long::class.javaObjectType -> f.set(this, obj.optLong(f.name))
                            List::class.java, List::class.javaObjectType -> {
                                val type1 = f.genericType // 获取泛型
                                if (type1 is ParameterizedType) {
                                    val gtCls = type1.actualTypeArguments[0] as Class<*> // 提取泛型
                                    val arr = obj.optJSONArray(f.name)
                                    when(gtCls) {
                                        String::class.java -> f.set(this, arr.optJSONArrayBasic { it })
                                        Int::class.java, Int::class.javaObjectType -> f.set(this, arr.optJSONArrayBasic { "$it".toInt() })
                                        Boolean::class.java, Boolean::class.javaObjectType -> f.set(this, arr.optJSONArrayBasic { "$it".toBoolean() })
                                        Float::class.java, Float::class.javaObjectType -> f.set(this, arr.optJSONArrayBasic { "$it".toFloat() })
                                        Double::class.java, Double::class.javaObjectType -> f.set(this, arr.optJSONArrayBasic { "$it".toDouble() })
                                        Long::class.java, Long::class.javaObjectType -> f.set(this, arr.optJSONArrayBasic { "$it".toLong() })
                                        else -> {
                                            if (gtCls.superclass == IJson::class.java) { // 需要遵循IJson构造函数的传值
                                                _log.append("发现list<:IJson> $gtCls\n")
                                                // IJson不能为内部类
                                                gtCls.declaredConstructors.forEach { _log.append("构造函数参数:$it\n") } // IJson派生类为内部类时构参为
                                                val cons = gtCls.getDeclaredConstructor(JSONObject::class.java) // 获取构造函数
                                                cons.isAccessible = true
                                                f.set(this, arr.optJSONArrayOrNull { cons.newInstance(it) })
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                if (f.type.superclass == IJson::class.java) { // 需要遵循IJson构造函数的传值
                                    _log.append("发现(:IJson) ${f.type}\n")
                                    val ob = obj.optJSONObject(f.name) ?: return@forEach
                                    val cons = f.type.getDeclaredConstructor(JSONObject::class.java) // 获取构造函数
                                    cons.isAccessible = true
                                    f.set(this, cons.newInstance(ob))
                                } else _log.append("未知参数 ${f.name}\n")
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace(); _log.append("$e\n") }
                }
            } catch (e: Exception) { e.printStackTrace(); _log.append("$e\n") }
        }
    }

    companion object {

        @JvmStatic fun JSONArray.forEach(lis: (ob: JSONObject?) -> Unit) { for (i in 0 until length()) { lis.invoke(optJSONObject(i)) } }
        @JvmStatic fun JSONArray.forEachIndexed(lis: (index: Int, ob: JSONObject?) -> Unit) { for (i in 0 until length()) { lis.invoke(i, optJSONObject(i)) } }

        /*
        [
            '',
            ''
        ]
        array.optJSONArrayBasic { "$it" }
         */
        @JvmStatic fun <BasicType> JSONArray?.optJSONArrayBasic(lis: (Any) -> BasicType):
                MutableList<BasicType>? {
            this ?: return null
            val data = mutableListOf<BasicType>()
            for(i in 0 until length()) { get(i)?.also { data.add(lis(it)) } }
            return data
        }

        /*
        [
            {},
            {}
        ]
        array.optJSONArrayOrNull { Entity(it) }
         */
        @JvmStatic fun <T> JSONArray?.optJSONArrayOrNull(lis: (JSONObject) -> T):
                MutableList<T>? {
            this ?: return null
            val data = mutableListOf<T>()
            for(i in 0 until length()) { optJSONObject(i)?.also { data.add(lis(it)) } }
            return data
        }

        @JvmStatic fun <T> JSONObject?.optJSONObjectOrNull(lis: (JSONObject) -> T): T? {
            this ?: return null
            if (this.length() == 0) return null
            return lis(this)
        }

        @JvmStatic fun <T> JSONObject?.optJSONObjectOrNull(key: String, lis: (JSONObject) -> T): T? {
            this ?: return null
            val ob = optJSONObject(key) ?: return null
            if (ob.length() == 0) return null
            return lis(ob)
        }
    }
}
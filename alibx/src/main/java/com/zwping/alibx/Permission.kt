package com.zwping.alibx

import android.content.Context

/**
 *
 * zwping @ 2021/11/24
 */
interface PermissionInterface {
    // 是否获取权限
    fun Context?.checkSelfPermission(permission: String): Boolean
    // 是否拒绝过权限
}
class Permission {
}
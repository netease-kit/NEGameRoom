/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl.model

import com.netease.yunxin.kit.roomkit.api.NEErrorCode

/**
 * 通用结果
 * @param T 泛型
 * @property code code码
 * @property requestId 请求id
 * @property msg 消息
 * @property ts 时间戳
 * @property data 数据
 * @property isSuccess 是否成功
 * @constructor 创建一个通用结果
 */
data class NEResult<T>(
    /**
     * code码
     */
    val code: Int,
    /**
     * 请求id
     */
    val requestId: String = "0",
    /**
     * 消息
     */
    val msg: String? = null,
    /**
     * 时间戳
     */
    val ts: Long = 0,
    /**
     * 数据
     */
    val data: T? = null
) {

    val isSuccess: Boolean
        /**
         * 是否成功
         */
        get() = code == NEErrorCode.SUCCESS

    fun toShortString() = if (isSuccess) "Success($msg)" else "Failure($code, $msg)"
}

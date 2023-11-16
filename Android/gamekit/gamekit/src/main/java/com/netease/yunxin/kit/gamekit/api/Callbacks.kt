/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api

import com.netease.yunxin.kit.roomkit.api.NEErrorCode

/**
 * 通用回调
 */
fun interface NEGameCallback<in T> {
    /**
     * 通用回调
     * @param code 错误码，0为成功，其他为失败
     * @param message 描述信息
     * @param data 数据实体
     */
    fun onResult(code: Int, message: String?, data: T?)
}

/**
 * 通用回调
 */
public abstract class NEGameCallback2<T> : NEGameCallback<T> {

    final override fun onResult(code: Int, message: String?, data: T?) {
        if (code == NEErrorCode.SUCCESS) {
            onSuccess(data)
        } else {
            onError(code, message)
        }
    }

    /**
     * 成功回调
     * @param data 数据
     */
    open fun onSuccess(data: T?) {}

    /**
     * 失败回调
     * @param code code码
     * @param message 消息
     */
    open fun onError(code: Int, message: String?) {}
}

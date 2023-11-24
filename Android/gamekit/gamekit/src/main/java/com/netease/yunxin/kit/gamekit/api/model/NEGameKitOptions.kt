/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

/**
 * 提供初始化配置
 * @property appKey NERoom appKey
 * @property roomUuid 房间Id
 * @property baseUrl 服务器地址
 */
data class NEGameKitOptions(
    val appKey: String,
    val roomUuid: String,
    val baseUrl: String
)

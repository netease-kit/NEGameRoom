/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

import android.app.Activity

/**
 * 加载游戏参数
 * @property gameId 游戏Id
 * @property language 游戏语言
 * @property activity 宿主Activity
 * @constructor
 */
data class NELoadGameParams(
    val gameId: String,
    val language: String = "zh-CN",
    val activity: Activity
)

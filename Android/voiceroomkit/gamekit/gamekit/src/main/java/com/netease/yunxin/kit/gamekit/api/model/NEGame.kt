/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

data class NEGame(
    /**
     * 游戏Id
     */
    val gameId: String,
    /**
     * 游戏名称
     */
    val gameName: String,
    /**
     * 游戏描述
     */
    val gameDesc: String,
    /**
     * 游戏规则
     */
    val rule: String,
    /**
     * 游戏背景图
     */
    val thumbnail: String,
    /**
     * 游戏供应商
     */
    val channel: Int
) {
    constructor(gameId: String, gameName: String, rule: String) : this(
        gameId,
        gameName,
        "",
        rule,
        "",
        NEGameChannel.SUD.ordinal
    )
}

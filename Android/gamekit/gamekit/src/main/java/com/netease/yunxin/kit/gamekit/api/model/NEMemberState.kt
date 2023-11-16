/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

/**
 *  成员状态
 */
enum class NEMemberState {
    /**
     * 未知
     */
    UNKNOWN,

    /**
     * 闲置中
     */
    IDLE,

    /**
     * 准备中
     */
    PREPARING,

    /**
     * 游戏中
     */
    PLAYING
}

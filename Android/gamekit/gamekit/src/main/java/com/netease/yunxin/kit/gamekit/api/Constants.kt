/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api

/**
 * 房间错误码
 */
object NEGameErrorCode {
    /*=================== Error Code From SDK ====================*/
    /**
     * 通用失败code码
     */
    const val FAILURE = -1

    /**
     * 成功code码
     */
    const val SUCCESS = 0

    const val GAME_SDK_INIT_FAILED = 101
    const val GAME_SDK_DESTROY_FAILED = 102
}

/**
 * 游戏错误信息
 */
object NEGameErrorMsg {

    const val FAILURE = "Failure"

    const val SUCCESS = "Success"
    const val GAME_SDK_INIT_FAILED = "Game SDK init failed"
    const val GAME_SDK_DESTROY_FAILED = "Game SDK destroy failed"
}

object NEGameStatus {
    /**
     * 准备中
     */
    const val GAME_PREPARING = 0

    /**
     * 游戏中
     */
    const val GAME_PLAYING = 1

    /**
     * 退出
     */
    const val GAME_END = -1
}

object NEGameMemberStatus {
    /**
     * 准备中
     */
    const val MEMBER_PREPARING = 0

    /**
     * 游戏中
     */
    const val MEMBER_PLAYING = 1

    /**
     * 退出
     */
    const val MEMBER_IDLE = 2
}

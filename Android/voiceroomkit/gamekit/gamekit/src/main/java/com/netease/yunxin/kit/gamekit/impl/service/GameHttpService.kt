/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl.service

import android.content.Context
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NEGame
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NELoginResponse
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo
import com.netease.yunxin.kit.gamekit.impl.model.GameMember
import kotlinx.coroutines.flow.Flow

interface HttpErrorReporter {

    /**
     * 网络错误事件
     * @property code 错误码
     * @property msg 信息
     * @property requestId 请求id
     * @constructor
     */
    data class ErrorEvent(
        val code: Int,
        val msg: String?,
        val requestId: String
    )

    fun reportHttpErrorEvent(error: ErrorEvent)

    val httpErrorEvents: Flow<ErrorEvent>
}

/**
 * 游戏房 服务端接口对应service
 */
interface GameHttpService : HttpErrorReporter {

    fun initialize(context: Context, url: String)

    fun addHeader(key: String, value: String)

    fun login(
        callback:
        NetRequestCallback<NELoginResponse>
    )

    fun getGameList(
        callback:
        NetRequestCallback<List<NEGame>>
    )

    fun createGame(
        gameId: String,
        roomUuid: String,
        callback:
        NetRequestCallback<NECreateGameResponse>
    )

    fun joinGame(
        gameId: String,
        roomUuid: String,
        callback:
        NetRequestCallback<NEJoinGameResponse>
    )

    fun leaveGame(
        gameId: String,
        roomUuid: String,
        callback:
        NetRequestCallback<Boolean>
    )

    fun startGame(
        gameId: String,
        roomUuid: String,
        callback:
        NetRequestCallback<Boolean>
    )

    fun endGame(
        gameId: String,
        roomUuid: String,
        callback:
        NetRequestCallback<Boolean>
    )

    fun getGameInfo(
        roomUuid: String,
        callback:
        NetRequestCallback<NERoomGameInfo>
    )

    fun getGameMembers(
        gameId: String,
        roomUuid: String,
        callback:
        NetRequestCallback<List<GameMember>>
    )
}

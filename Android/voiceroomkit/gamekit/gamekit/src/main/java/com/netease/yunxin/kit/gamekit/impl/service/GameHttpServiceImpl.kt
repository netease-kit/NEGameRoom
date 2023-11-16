/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl.service

import android.content.Context
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.common.network.Request
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NEGame
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NELoginResponse
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo
import com.netease.yunxin.kit.gamekit.impl.model.GameMember
import com.netease.yunxin.kit.gamekit.impl.repository.GameRepository
import com.netease.yunxin.kit.gamekit.impl.util.GameLog
import com.netease.yunxin.kit.roomkit.api.NEErrorCode
import com.netease.yunxin.kit.roomkit.api.NEErrorMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object GameHttpServiceImpl : GameHttpService {

    private const val TAG = "GameHttpServiceImpl"
    private var gameRepository = GameRepository()

    private var gameScope: CoroutineScope? = null
    init {
        gameScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun initialize(context: Context, url: String) {
        gameRepository.initialize(context, url)
    }

    override fun addHeader(key: String, value: String) {
        gameRepository.addHeader(key, value)
    }

    override fun login(callback: NetRequestCallback<NELoginResponse>) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.login()
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getGameList(
        callback: NetRequestCallback<List<NEGame>>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.getGameList()
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun createGame(
        gameId: String,
        roomUuid: String, callback: NetRequestCallback<NECreateGameResponse>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.createGame(gameId, roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun joinGame(
        gameId: String,
        roomUuid: String, callback: NetRequestCallback<NEJoinGameResponse>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.joinGame(gameId, roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun leaveGame(
        gameId: String,
        roomUuid: String, callback: NetRequestCallback<Boolean>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.leaveGame(gameId, roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun startGame(
        gameId: String,
        roomUuid: String, callback: NetRequestCallback<Boolean>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.startGame(gameId, roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun endGame(
        gameId: String,
        roomUuid: String, callback: NetRequestCallback<Boolean>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.endGame(gameId, roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getGameInfo(
        roomUuid: String,
        callback: NetRequestCallback<NERoomGameInfo>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.getGameInfo(roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun getGameMembers(
        gameId: String,
        roomUuid: String,
        callback: NetRequestCallback<List<GameMember>>
    ) {
        gameScope?.launch {
            Request.request(
                {
                    gameRepository.getGameMembers(gameId, roomUuid)
                },
                success = {
                    callback.success(it)
                },
                error = { code, msg ->
                    reportHttpErrorEvent(HttpErrorReporter.ErrorEvent(code, msg, ""))
                    callback.error(code, msg)
                }
            )
        }
    }

    override fun reportHttpErrorEvent(error: HttpErrorReporter.ErrorEvent) {
        if (error.code != NEErrorCode.SUCCESS) {
            GameLog.e(TAG, "report http error: $error")
        }
        httpErrorEvents.value = error
    }

    override val httpErrorEvents =
        MutableStateFlow(HttpErrorReporter.ErrorEvent(NEErrorCode.SUCCESS, NEErrorMsg.SUCCESS, "0"))

    fun destroy() {
        gameScope?.cancel()
        gameScope = null
    }
}

/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl.repository

import android.content.Context
import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.common.network.ServiceCreator
import com.netease.yunxin.kit.gamekit.BuildConfig
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NEGame
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NELoginResponse
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo
import com.netease.yunxin.kit.gamekit.impl.model.GameMember
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.impl.repository.ServerConfig
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository {
    companion object {
        lateinit var serverConfig: ServerConfig
    }
    private val serviceCreator: ServiceCreator = ServiceCreator()

    private lateinit var gameApi: GameApi

    fun initialize(context: Context, url: String) {
        serviceCreator.init(
            context,
            url,
            if (BuildConfig.DEBUG) ServiceCreator.LOG_LEVEL_BODY else ServiceCreator.LOG_LEVEL_BASIC,
            NERoomKit.getInstance().deviceId
        )
        val localLanguage = Locale.getDefault().language
        serviceCreator.addHeader(ServiceCreator.ACCEPT_LANGUAGE_KEY, localLanguage)
        gameApi = serviceCreator.create(GameApi::class.java)
    }

    fun addHeader(key: String, value: String) {
        serviceCreator.addHeader(key, value)
    }

    suspend fun login(): Response<NELoginResponse> = withContext(Dispatchers.IO) {
        gameApi.login()
    }

    suspend fun getGameList(): Response<List<NEGame>> = withContext(Dispatchers.IO) {
        gameApi.getGameList()
    }

    suspend fun createGame(gameId: String, roomUuid: String): Response<NECreateGameResponse> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "gameId" to gameId,
            "roomUuid" to roomUuid
        )
        gameApi.createGame(params)
    }

    suspend fun joinGame(gameId: String, roomUuid: String): Response<NEJoinGameResponse> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "gameId" to gameId,
            "roomUuid" to roomUuid
        )
        gameApi.joinGame(params)
    }

    suspend fun leaveGame(gameId: String, roomUuid: String): Response<Boolean> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "gameId" to gameId,
            "roomUuid" to roomUuid
        )
        gameApi.leaveGame(params)
    }

    suspend fun startGame(gameId: String, roomUuid: String): Response<Boolean> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "gameId" to gameId,
            "roomUuid" to roomUuid
        )
        gameApi.startGame(params)
    }

    suspend fun endGame(gameId: String, roomUuid: String): Response<Boolean> = withContext(
        Dispatchers.IO
    ) {
        val params = mapOf(
            "gameId" to gameId,
            "roomUuid" to roomUuid
        )
        gameApi.endGame(params)
    }

    suspend fun getGameInfo(roomUuid: String): Response<NERoomGameInfo> = withContext(
        Dispatchers.IO
    ) {
        gameApi.getGameInfo(roomUuid)
    }

    suspend fun getGameMembers(gameId: String, roomUuid: String): Response<List<GameMember>> = withContext(
        Dispatchers.IO
    ) {
        gameApi.getGameMembers(gameId, roomUuid)
    }
}

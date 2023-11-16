/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.gamekit.impl.repository

import com.netease.yunxin.kit.common.network.Response
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NEGame
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NELoginResponse
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo
import com.netease.yunxin.kit.gamekit.impl.model.GameMember
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GameApi {

    /**
     * 获取游戏code
     */
    @POST("nemo/game/sud/login")
    suspend fun login(): Response<NELoginResponse>

    /**
     * 获取游戏列表
     */
    @GET("nemo/game/list")
    suspend fun getGameList(): Response<List<NEGame>>

    /**
     * 加入游戏
     */
    @POST("nemo/game/create")
    suspend fun createGame(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<NECreateGameResponse>

    /**
     * 加入游戏
     */
    @POST("nemo/game/join")
    suspend fun joinGame(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<NEJoinGameResponse>

    /**
     * 退出游戏
     */
    @POST("nemo/game/exit")
    suspend fun leaveGame(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Boolean>

    /**
     * 开始游戏
     */
    @POST("nemo/game/start")
    suspend fun startGame(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Boolean>

    /**
     * 结束游戏
     */
    @POST("nemo/game/end")
    suspend fun endGame(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<Boolean>

    /**
     * 游戏成员列表
     */
    @GET("nemo/game/members")
    suspend fun getGameMembers(
        @Query("gameId") gameId: String,
        @Query("roomUuid") roomUuid: String
    ): Response<List<GameMember>>

    /**
     * 获取游戏信息
     */
    @GET("nemo/game/gameInfo")
    suspend fun getGameInfo(
        @Query("roomUuid") roomUuid: String
    ): Response<NERoomGameInfo>
}

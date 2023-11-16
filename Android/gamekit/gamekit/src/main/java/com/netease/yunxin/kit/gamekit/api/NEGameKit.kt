/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api

import android.annotation.SuppressLint
import android.view.View
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameParams
import com.netease.yunxin.kit.gamekit.api.model.NEGameKitOptions
import com.netease.yunxin.kit.gamekit.api.model.NEGameList
import com.netease.yunxin.kit.gamekit.api.model.NEGameLoginInfo
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameParams
import com.netease.yunxin.kit.gamekit.api.model.NELoadGameParams
import com.netease.yunxin.kit.gamekit.api.model.NEMemberState
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo
import com.netease.yunxin.kit.gamekit.api.model.NEStartGameParams
import com.netease.yunxin.kit.gamekit.impl.GameKitImpl

interface NEGameKit {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @JvmField
        val instance: NEGameKit = GameKitImpl()

        @JvmStatic
        fun getInstance(): NEGameKit = instance
    }

    /**
     * 查询初始化状态
     */
    val isInitialized: Boolean

    /**
     *
     * 初始化
     * @param options 初始化参数
     * @param callback 回调
     */
    fun initialize(options: NEGameKitOptions, info: NEGameLoginInfo, callback: NEGameCallback<Unit>?)

    /**
     * 获取游戏列表
     * @param callback 回调
     */
    fun getGameList(callback: NEGameCallback<NEGameList>)

    /**
     * 创建游戏
     * @param params 创建游戏参数
     * @param callback 回调
     * <br>相关回调：房间内成员会触发[NEGameListener.onGameCreated]回调
     */
    fun createGame(params: NECreateGameParams, callback: NEGameCallback<Unit>? = null)

    /**
     * 参与游戏
     * @param params 参与游戏参数
     * @param callback 回调
     * <br>相关回调：房间内成员会触发[NEGameListener.onMemberJoinGame]回调
     */
    fun joinGame(params: NEJoinGameParams, callback: NEGameCallback<Unit>? = null)

    /**
     * 开始游戏
     * @param params 开始游戏参数
     * @param callback 回调
     * <br>相关回调：房间内成员会触发[NEGameListener.onGameStarted]回调
     */
    fun startGame(params: NEStartGameParams, callback: NEGameCallback<Unit>? = null)

    /**
     * 获取房间内游戏信息
     */
    fun getRoomGameInfo(callback: NEGameCallback<NERoomGameInfo>)

    /**
     * 主播/房主调用
     * 结束游戏
     * @param callback 回调
     * <br>相关回调：房间内成员会触发[NEGameListener.onGameEnded]回调
     */
    fun endGame(callback: NEGameCallback<Unit>? = null)

    /**
     * 离开游戏
     * @param callback 回调
     * <br>相关回调：房间内成员会触发[NEGameListener.onMemberLeaveGame]回调
     */
    fun leaveGame(callback: NEGameCallback<Unit>? = null)

    /**
     * 加载游戏
     * @param params 加载游戏参数
     * @param callback  回调
     *
     */
    fun loadGame(params: NELoadGameParams, callback: NEGameCallback<View>)

    /**
     * 卸载游戏
     * @param callback  回调
     */
    fun unloadGame(callback: NEGameCallback<Unit>? = null)

    /**
     * 获取成员状态
     * @param userUuid 用户Id
     * @return 成员状态
     */
    fun getMemberState(userUuid: String?): NEMemberState

    /**
     * 销毁SDK
     */
    fun destroy()

    /**
     * 添加游戏监听
     * @param listener 监听器
     */
    fun addGameListener(listener: NEGameListener)

    /**
     * 移除游戏监听
     * @param listener 监听器
     */
    fun removeGameListener(listener: NEGameListener)

    /**
     * 通知你画我猜游戏命中关键词
     * @param isHit Boolean
     * @param keyWord String
     * @param text String
     * @param wordType String?
     * @param keyWordList MutableList<String>?
     * @param numberList MutableList<Int>?
     */
    fun notifyAPPCommonSelfTextHitState(
        isHit: Boolean,
        keyWord: String,
        text: String,
        wordType: String?,
        keyWordList: MutableList<String>?,
        numberList: MutableList<Int>?
    )
}

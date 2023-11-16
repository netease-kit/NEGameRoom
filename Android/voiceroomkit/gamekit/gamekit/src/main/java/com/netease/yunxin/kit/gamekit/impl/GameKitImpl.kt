/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl

import android.text.TextUtils
import android.view.View
import com.google.gson.JsonObject
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.common.utils.NetworkUtils
import com.netease.yunxin.kit.common.utils.ThreadUtils
import com.netease.yunxin.kit.common.utils.XKitUtils
import com.netease.yunxin.kit.gamekit.api.NEGameCallback
import com.netease.yunxin.kit.gamekit.api.NEGameErrorCode
import com.netease.yunxin.kit.gamekit.api.NEGameKit
import com.netease.yunxin.kit.gamekit.api.NEGameListener
import com.netease.yunxin.kit.gamekit.api.NEGameMemberStatus
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameParams
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NEGame
import com.netease.yunxin.kit.gamekit.api.model.NEGameKitOptions
import com.netease.yunxin.kit.gamekit.api.model.NEGameList
import com.netease.yunxin.kit.gamekit.api.model.NEGameLoginInfo
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameParams
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameResponse
import com.netease.yunxin.kit.gamekit.api.model.NELoadGameParams
import com.netease.yunxin.kit.gamekit.api.model.NELoginResponse
import com.netease.yunxin.kit.gamekit.api.model.NEMemberState
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo
import com.netease.yunxin.kit.gamekit.api.model.NEStartGameParams
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameCreatedModel
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameEndedModel
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameStartedModel
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEMemberJoinModel
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEMemberLeaveModel
import com.netease.yunxin.kit.gamekit.impl.model.GameMember
import com.netease.yunxin.kit.gamekit.impl.service.GameHttpService
import com.netease.yunxin.kit.gamekit.impl.service.GameHttpServiceImpl
import com.netease.yunxin.kit.gamekit.impl.service.SudSdkService
import com.netease.yunxin.kit.gamekit.impl.util.GameLog
import com.netease.yunxin.kit.gamekit.impl.util.GsonUtils
import com.netease.yunxin.kit.roomkit.api.NERoomChatMessage
import com.netease.yunxin.kit.roomkit.api.NERoomContext
import com.netease.yunxin.kit.roomkit.api.NERoomKit
import com.netease.yunxin.kit.roomkit.api.NERoomListenerAdapter
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import com.netease.yunxin.kit.roomkit.api.service.NESeatEventListener
import com.netease.yunxin.kit.roomkit.api.service.NESeatItem
import com.netease.yunxin.kit.roomkit.api.service.NESeatItemStatus
import com.netease.yunxin.kit.roomkit.impl.model.RoomCustomMessages
import java.util.concurrent.CopyOnWriteArrayList

internal class GameKitImpl : NEGameKit {
    private val gameHttpService: GameHttpService by lazy { GameHttpServiceImpl }
    private val sudSdkService = SudSdkService()
    private val isSupport: Boolean
        get() = NERoomKit.getInstance().isInitialized && NERoomKit.getInstance().authService.isLoggedIn
    private val listeners: CopyOnWriteArrayList<NEGameListener> by lazy {
        CopyOnWriteArrayList<NEGameListener>()
    }
    private var _isInitialized = false
    private var baseUrl: String = ""
    private var roomUuid: String = // 游戏房间id
        ""
    private var gameId: String = "" // 当前使用的游戏id
    private var roomContext: NERoomContext? = null

    /**
     * 保存userUuid和游戏状态值的映射关系
     */
    private val uid2GameMembers = linkedMapOf<String, Int>()

    /**
     * 当前麦位列表
     */
    private var currentSeatItems: List<NESeatItem>? = null

    private val roomListener = object : NERoomListenerAdapter() {
        override fun onReceiveChatroomMessages(messages: List<NERoomChatMessage>) {
            super.onReceiveChatroomMessages(messages)
            messages.forEach {
                if (it is RoomCustomMessages) {
                    GameLog.i(TAG, "onReceiveChatroomMessages customAttachment:${it.attachStr}")
                    when (getType(it.attachStr)) {
                        GameConstant.GAME_CREATED -> {
                            uid2GameMembers.clear()
                            GameLog.i(TAG, "uid2GameMembers clear")
                            val result = GsonUtils.fromJson(
                                it.attachStr,
                                NEGameCreatedModel::class.java
                            )
                            gameId = result.data.gameId
                            GameLog.i(TAG, "onReceiveChatroomMessages GAME_CREATED:$result")
                            listeners.forEach { listener ->
                                listener.onGameCreated(result.data)
                            }
                        }
                        GameConstant.GAME_JOIN -> {
                            val result = GsonUtils.fromJson(
                                it.attachStr,
                                NEMemberJoinModel::class.java
                            )
                            GameLog.i(TAG, "onReceiveChatroomMessages GAME_JOIN:$result")
                            listeners.forEach { listener ->
                                val members = ArrayList<NERoomMember>()
                                val member = roomContext?.getMember(result.data.userUuid)
                                if (member != null) {
                                    members.add(member)
                                    uid2GameMembers[member.uuid] = result.data.status
                                    GameLog.i(
                                        TAG,
                                        "uid2GameMembers[member.uuid]:${uid2GameMembers[member.uuid]},status:${result.data.status}"
                                    )
                                }
                                listener.onMemberJoinGame(members)
                            }
                        }
                        GameConstant.GAME_LEAVE -> {
                            val result = GsonUtils.fromJson(
                                it.attachStr,
                                NEMemberLeaveModel::class.java
                            )
                            GameLog.i(TAG, "onReceiveChatroomMessages GAME_LEAVE:$result")
                            listeners.forEach { listener ->
                                val members = ArrayList<NERoomMember>()
                                val member = roomContext?.getMember(result.data.userUuid)
                                if (member != null) {
                                    members.add(member)
                                    uid2GameMembers[member.uuid] = result.data.status
                                    GameLog.i(
                                        TAG,
                                        "uid2GameMembers[member.uuid]:${uid2GameMembers[member.uuid]},status:${result.data.status}"
                                    )
                                }
                                listener.onMemberLeaveGame(members)
                            }
                        }
                        GameConstant.GAME_START -> {
                            for (key in uid2GameMembers.keys) {
                                val status = uid2GameMembers[key]
                                status?.let {
                                    if (getMemberStateByStatus(status) == NEMemberState.PREPARING) {
                                        uid2GameMembers[key] = NEGameMemberStatus.MEMBER_PLAYING
                                        GameLog.i(
                                            TAG,
                                            "uid2GameMembers[key]:${uid2GameMembers[key]},status:${NEGameMemberStatus.MEMBER_PLAYING}"
                                        )
                                    }
                                }
                            }

                            val result = GsonUtils.fromJson(
                                it.attachStr,
                                NEGameStartedModel::class.java
                            )
                            GameLog.i(TAG, "onReceiveChatroomMessages GAME_START:$result")
                            listeners.forEach { listener ->
                                listener.onGameStarted(result.data)
                            }
                        }
                        GameConstant.GAME_END -> {
                            uid2GameMembers.clear()
                            GameLog.i(TAG, "uid2GameMembers clear")
                            unloadGame(null)
                            val result = GsonUtils.fromJson(
                                it.attachStr,
                                NEGameEndedModel::class.java
                            )
                            GameLog.i(TAG, "onReceiveChatroomMessages GAME_END:$result")
                            listeners.forEach { listener ->
                                listener.onGameEnded(result.data)
                            }
                        }
                    }
                }
            }
        }
    }

    private val seatListener = object : NESeatEventListener() {
        override fun onSeatListChanged(seatItems: List<NESeatItem>) {
            super.onSeatListChanged(seatItems)
            handleSeatListChanged(seatItems)
        }
    }

    private val networkStateListener: NetworkUtils.NetworkStateListener =
        object : NetworkUtils.NetworkStateListener {
            private var isFirst = true
            override fun onConnected(networkType: NetworkUtils.NetworkType?) {
                GameLog.i(TAG, "onNetwork available isFirst = $isFirst")
                if (!isFirst) {
                    getRoomGameInfo { code, message, data ->
                        GameLog.i(
                            TAG,
                            "network reconnect,onRoomGameInfo code:$code,message:$message,data:$data"
                        )
                    }
                }
                isFirst = false
            }

            override fun onDisconnected() {
                GameLog.i(TAG, "onNetwork unavailable")
                isFirst = false
            }
        }
    companion object {
        const val TAG = "NEGameKit"
        private const val COMMON_ERROR_CODE = NEGameErrorCode.FAILURE
        private const val NOT_INITIALIZED_ERROR_MSG = "gamekit not initialized"
        private const val ROOMKIT_ERROR_MSG = "not support,because neroom sdk not initialized or not login"
        private const val INVALID_PARAMS_ERROR_MSG = "invalid params"
    }

    override val isInitialized: Boolean
        get() = _isInitialized

    override fun initialize(options: NEGameKitOptions, info: NEGameLoginInfo, callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "gameKit initialize，options:$options")
        roomUuid = options.roomUuid
        baseUrl = options.baseUrl
        gameHttpService.initialize(XKitUtils.getApplicationContext(), baseUrl)
        gameHttpService.addHeader("appkey", options.appKey)
        roomContext = NERoomKit.getInstance().roomService.getRoomContext(roomUuid)
        NetworkUtils.registerNetworkStatusChangedListener(networkStateListener)
        if (roomContext == null) {
            GameLog.e(TAG, "roomContext is null")
            callback?.onResult(NEGameErrorCode.FAILURE, "roomContext is null", null)
            return
        } else {
            roomContext?.addRoomListener(roomListener)
            roomContext?.seatController?.addSeatListener(seatListener)
        }
        _isInitialized = true
        login(info, callback)
    }
    private fun handleSeatListChanged(seatItems: List<NESeatItem>) {
        GameLog.i(TAG, "handleSeatListChanged,seatItems:$seatItems")
        roomContext?.localMember?.let {
            GameLog.i(
                TAG,
                "handleSeatListChanged roomContext.localMember:${roomContext?.localMember}"
            )
            val myUuid = roomContext?.localMember!!.uuid
            val old = currentSeatItems?.firstOrNull { it.user == myUuid }
            val now = seatItems.firstOrNull { it.user == myUuid }
            if (old != null && old.status == NESeatItemStatus.TAKEN && now == null) {
                // 之前在麦上且现在不在麦上了，如果是在准备中或者游戏中需要调用退出游戏接口
                val memberState = getMemberState(myUuid)
                GameLog.i(
                    TAG,
                    "handleSeatListChanged localUser Uuid:$myUuid,localUser memberState:$memberState"
                )
                if (memberState == NEMemberState.PREPARING || memberState == NEMemberState.PLAYING) {
                    GameLog.i(TAG, "leaveGame because seatItem.status != NESeatItemStatus.TAKEN")
                    // todo 这块逻辑放到业务上去处理，新增语聊房接口
                    // / 自己从上麦状态变为下麦状态
                    //  @objc optional func onSelfLeftSeat()
                    leaveGame()
                }
            }
        }
        currentSeatItems = seatItems
    }

    private fun login(info: NEGameLoginInfo, callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "login，info:$info")
        // 按照现有设计，需要传进来
        gameHttpService.addHeader("user", info.account)
        gameHttpService.addHeader("token", info.token)
        gameHttpService.login(object : NetRequestCallback<NELoginResponse> {
            override fun error(code: Int, msg: String?) {
                GameLog.e(TAG, "login error code:$code,msg:$msg")
                callback?.onResult(code, msg, null)
            }
            override fun success(info: NELoginResponse?) {
                info?.let {
                    sudSdkService.setSudCode(it.code)
                    initSudSdk(info.appKey, info.appId, baseUrl, callback)
                }
            }
        })
    }

    override fun getGameList(callback: NEGameCallback<NEGameList>) {
        GameLog.api(TAG, "getGameList")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        gameHttpService.getGameList(object : NetRequestCallback<List<NEGame>> {
            override fun error(code: Int, msg: String?) {
                GameLog.e(TAG, "getGameList error code:$code,msg:$msg")
                callback.onResult(code, msg, null)
            }

            override fun success(info: List<NEGame>?) {
                val neGameList = NEGameList(data = info)
                callback.onResult(NEGameErrorCode.SUCCESS, "", neGameList)
                GameLog.i(TAG, "getGameList success info:$info")
            }
        })
    }

    override fun createGame(params: NECreateGameParams, callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "createGame,params:$params")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        this.gameId = params.gameId
        gameHttpService.createGame(
            gameId = gameId,
            roomUuid,
            object : NetRequestCallback<NECreateGameResponse> {
                override fun error(code: Int, msg: String?) {
                    callback?.onResult(code, msg, null)
                    GameLog.e(TAG, "createGame error code:$code,msg:$msg")
                }

                override fun success(info: NECreateGameResponse?) {
                    callback?.onResult(NEGameErrorCode.SUCCESS, "", null)
                    GameLog.i(TAG, "createGame success info:$info")
                }
            }
        )
    }

    override fun joinGame(params: NEJoinGameParams, callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "joinGame")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        this.gameId = params.gameId
        if (TextUtils.isEmpty(gameId) || TextUtils.isEmpty(roomUuid)) {
            GameLog.e(TAG, "joinGame failed,$INVALID_PARAMS_ERROR_MSG")
            callback?.onResult(COMMON_ERROR_CODE, "joinGame failed,$INVALID_PARAMS_ERROR_MSG", null)
            return
        }
        gameHttpService.joinGame(
            gameId,
            roomUuid,
            object : NetRequestCallback<NEJoinGameResponse> {
                override fun error(code: Int, msg: String?) {
                    callback?.onResult(code, msg, null)
                    GameLog.e(TAG, "joinGame error code:$code,msg:$msg")
                }

                override fun success(info: NEJoinGameResponse?) {
                    callback?.onResult(NEGameErrorCode.SUCCESS, "", null)
                    GameLog.i(TAG, "joinGame success info:$info")
                }
            }
        )
    }

    override fun startGame(params: NEStartGameParams, callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "startGame")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        gameHttpService.startGame(
            params.gameId,
            roomUuid,
            object : NetRequestCallback<Boolean> {
                override fun error(code: Int, msg: String?) {
                    callback?.onResult(code, msg, null)
                    GameLog.e(TAG, "startGame error code:$code,msg:$msg")
                }

                override fun success(info: Boolean?) {
                    callback?.onResult(NEGameErrorCode.SUCCESS, "", null)
                    GameLog.i(TAG, "startGame success")
                }
            }
        )
    }

    override fun getRoomGameInfo(callback: NEGameCallback<NERoomGameInfo>) {
        GameLog.api(TAG, "getRoomGameInfo")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        gameHttpService.getGameInfo(
            roomUuid,
            object : NetRequestCallback<NERoomGameInfo> {
                override fun error(code: Int, msg: String?) {
                    GameLog.e(TAG, "getRoomGameInfo error code:$code,msg:$msg")
                    callback.onResult(code, msg, null)
                    if (!TextUtils.isEmpty(gameId)) {
                        getGameMembers(null, null)
                    }
                }

                override fun success(info: NERoomGameInfo?) {
                    if (info == null) {
                        callback.onResult(NEGameErrorCode.SUCCESS, "", null)
                        return
                    } else {
                        if (!TextUtils.isEmpty(info.gameId)) {
                            gameId = info.gameId
                            getGameMembers(callback, info)
                        } else {
                            callback.onResult(NEGameErrorCode.SUCCESS, "", info)
                            GameLog.i(TAG, "getRoomGameInfo success info:$info")
                        }
                    }
                }
            }
        )
    }

    override fun endGame(callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "endGame")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        if (TextUtils.isEmpty(gameId) || TextUtils.isEmpty(roomUuid)) {
            GameLog.e(TAG, "endGame failed,$INVALID_PARAMS_ERROR_MSG")
            callback?.onResult(COMMON_ERROR_CODE, "endGame failed,$INVALID_PARAMS_ERROR_MSG", null)
            return
        }
        gameHttpService.endGame(
            gameId,
            roomUuid,
            object : NetRequestCallback<Boolean> {
                override fun error(code: Int, msg: String?) {
                    callback?.onResult(code, msg, null)
                    GameLog.e(TAG, "endGame error code:$code,msg:$msg")
                }

                override fun success(info: Boolean?) {
                    callback?.onResult(NEGameErrorCode.SUCCESS, "", null)
                    GameLog.i(TAG, "endGame success")
                }
            }
        )
    }

    override fun leaveGame(callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "leaveGame")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        if (TextUtils.isEmpty(gameId) || TextUtils.isEmpty(roomUuid)) {
            GameLog.e(TAG, "leaveGame failed,$INVALID_PARAMS_ERROR_MSG")
            callback?.onResult(
                COMMON_ERROR_CODE,
                "leaveGame failed,$INVALID_PARAMS_ERROR_MSG",
                null
            )
            return
        }
        gameHttpService.leaveGame(
            gameId,
            roomUuid,
            object : NetRequestCallback<Boolean> {
                override fun error(code: Int, msg: String?) {
                    callback?.onResult(code, msg, null)
                    GameLog.e(TAG, "leaveGame error code:$code,msg:$msg")
                }

                override fun success(info: Boolean?) {
                    callback?.onResult(NEGameErrorCode.SUCCESS, "", null)
                    GameLog.i(TAG, "leaveGame success")
                }
            }
        )
    }

    override fun loadGame(params: NELoadGameParams, callback: NEGameCallback<View>) {
        GameLog.api(TAG, "loadGame,params:$params,callback:$callback")
        if (!isSupport) {
            callback.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        if (!TextUtils.isEmpty(gameId)) {
            unloadGame(null)
        }
        gameId = params.gameId
        if (TextUtils.isEmpty(gameId) || TextUtils.isEmpty(roomUuid)) {
            GameLog.e(TAG, "loadGame failed,$INVALID_PARAMS_ERROR_MSG")
            callback.onResult(COMMON_ERROR_CODE, "loadGame failed,$INVALID_PARAMS_ERROR_MSG", null)
            return
        }
        if (params.activity.isDestroyed) {
            return
        }
        if (TextUtils.isEmpty(sudSdkService.getSudCode())) {
            gameHttpService.login(object : NetRequestCallback<NELoginResponse> {
                override fun error(code: Int, msg: String?) {
                    GameLog.e(TAG, "getCode error code:$code,msg:$msg")
                }

                override fun success(info: NELoginResponse?) {
                    info?.let {
                        sudSdkService.setSudCode(it.code)
                        loadGameInner(params, callback)
                    }
                }
            })
        } else {
            loadGameInner(params, callback)
        }
    }

    override fun unloadGame(callback: NEGameCallback<Unit>?) {
        GameLog.api(TAG, "unloadGame")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, ROOMKIT_ERROR_MSG, null)
            return
        }
        if (!_isInitialized) {
            GameLog.e(TAG, NOT_INITIALIZED_ERROR_MSG)
            callback?.onResult(COMMON_ERROR_CODE, NOT_INITIALIZED_ERROR_MSG, null)
            return
        }
        if (TextUtils.isEmpty(gameId) || TextUtils.isEmpty(roomUuid)) {
            GameLog.e(TAG, "unloadGame failed,$INVALID_PARAMS_ERROR_MSG")
            callback?.onResult(
                COMMON_ERROR_CODE,
                "unloadGame failed,$INVALID_PARAMS_ERROR_MSG",
                null
            )
            return
        }
        gameId = ""
        sudSdkService.destroyMG()
        callback?.onResult(COMMON_ERROR_CODE, "", null)
        GameLog.i(TAG, "unloadGame success")
    }

    override fun getMemberState(userUuid: String?): NEMemberState {
        GameLog.api(TAG, "getMemberState")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            GameLog.i(TAG, "getMemberState result:UNKNOWN")
            return NEMemberState.UNKNOWN
        }
        val status = uid2GameMembers[userUuid]
        return if (status == null) {
            NEMemberState.IDLE
        } else {
            getMemberStateByStatus(status)
        }
    }

    override fun destroy() {
        GameLog.api(TAG, "release")
        reset()
    }

    override fun addGameListener(listener: NEGameListener) {
        GameLog.api(TAG, "addGameListener:$listener")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            return
        }
        listeners.add(listener)
        sudSdkService.addGameListener(listener)
    }

    override fun removeGameListener(listener: NEGameListener) {
        GameLog.api(TAG, "removeGameListener:$listener")
        if (!isSupport) {
            GameLog.e(TAG, ROOMKIT_ERROR_MSG)
            return
        }
        listeners.remove(listener)
        sudSdkService.removeGameListener(listener)
    }

    override fun notifyAPPCommonSelfTextHitState(
        isHit: Boolean,
        keyWord: String,
        text: String,
        wordType: String?,
        keyWordList: MutableList<String>?,
        numberList: MutableList<Int>?
    ) {
        sudSdkService.notifyAPPCommonSelfTextHitState(
            isHit,
            keyWord,
            text,
            wordType,
            keyWordList,
            numberList
        )
    }

    // endregion GameKit回调

    private fun initSudSdk(appKey: String, appId: String, serverUrl: String = this.baseUrl, callback: NEGameCallback<Unit>?) {
        sudSdkService.initSDK(
            appKey,
            appId,
            serverUrl
        ) { code, message, data ->
            callback?.onResult(code, message, data)
        }
    }

    private fun loadGameInner(params: NELoadGameParams, callback: NEGameCallback<View>) {
        GameLog.i(
            TAG,
            "loadGameInner,threadName:${Thread.currentThread().name},params:$params,roomUuid:$roomUuid,callback:$callback"
        )
        ThreadUtils.runOnUiThread {
            val gameId = try {
                params.gameId.toLong()
            } catch (e: Exception) {
                0L
            }
            val iSudFSTAPP = sudSdkService.loadMG(
                params.activity,
                roomUuid,
                gameId,
                params.language
            )
            // 如果返回空，则代表参数问题或者非主线程
            if (iSudFSTAPP == null) {
                GameLog.e(TAG, "Sud SDK loadMG error because iSudFSTAPP is null")
                callback.onResult(COMMON_ERROR_CODE, "Sud SDK loadMG error", null)
                return@runOnUiThread
            }
            // 获取游戏视图，将其抛回Activity进行展示
            // Activity调用：gameContainer.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            callback.onResult(NEGameErrorCode.SUCCESS, "", iSudFSTAPP.gameView)
            GameLog.i(TAG, "loadGame success")
        }
    }

    private fun getType(json: String): Int? {
        val jsonObject: JsonObject = GsonUtils.fromJson(
            json,
            JsonObject::class.java
        )
        return jsonObject["type"]?.asInt
    }

    private fun getGameMembers(callback: NEGameCallback<NERoomGameInfo>? = null, gameInfo: NERoomGameInfo?) {
        GameLog.i(TAG, "getGameMembers start")
        gameHttpService.getGameMembers(
            gameId,
            roomUuid,
            object : NetRequestCallback<List<GameMember>> {
                override fun error(code: Int, msg: String?) {
                    GameLog.e(TAG, "getGameMembers error:$code,msg:$msg")
                    callback?.onResult(code, msg, null)
                }

                override fun success(info: List<GameMember>?) {
                    info?.let {
                        info.forEach {
                            uid2GameMembers[it.userUuid] = it.status
                        }
                        GameLog.i(TAG, "getGameMembers success:$info")
                    }
                    callback?.onResult(NEGameErrorCode.SUCCESS, "", gameInfo)
                }
            }
        )
    }

    fun getRoomContext(): NERoomContext? {
        return roomContext
    }

    private fun getMemberStateByStatus(status: Int): NEMemberState {
        return when (status) {
            NEGameMemberStatus.MEMBER_PREPARING -> NEMemberState.PREPARING
            NEGameMemberStatus.MEMBER_PLAYING -> NEMemberState.PLAYING
            else -> NEMemberState.IDLE
        }
    }

    private fun reset() {
        roomContext?.seatController?.removeSeatListener(seatListener)
        roomContext?.removeRoomListener(roomListener)
        gameId = ""
        roomUuid = ""
        roomContext = null
        listeners.clear()
        _isInitialized = false
        uid2GameMembers.clear()
        sudSdkService.destroy()
        currentSeatItems = null
        NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener)
        GameLog.i(TAG, "reset")
    }
}

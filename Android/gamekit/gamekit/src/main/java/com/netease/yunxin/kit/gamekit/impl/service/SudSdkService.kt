/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl.service

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import com.netease.yunxin.kit.common.network.NetRequestCallback
import com.netease.yunxin.kit.common.utils.XKitUtils
import com.netease.yunxin.kit.gamekit.api.NEGameCallback
import com.netease.yunxin.kit.gamekit.api.NEGameErrorCode
import com.netease.yunxin.kit.gamekit.api.NEGameErrorMsg
import com.netease.yunxin.kit.gamekit.api.NEGameKit
import com.netease.yunxin.kit.gamekit.api.NEGameListener
import com.netease.yunxin.kit.gamekit.api.model.NEGameChannel
import com.netease.yunxin.kit.gamekit.api.model.NELoginResponse
import com.netease.yunxin.kit.gamekit.api.model.event.GameKeyWordToHitEvent
import com.netease.yunxin.kit.gamekit.impl.GameKitImpl
import com.netease.yunxin.kit.gamekit.impl.util.GameLog
import java.util.concurrent.CopyOnWriteArrayList
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGDecorator
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGListener
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSTAPPDecorator
import tech.sud.mgp.SudMGPWrapper.model.GameConfigModel
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel
import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState
import tech.sud.mgp.SudMGPWrapper.utils.ISudFSMStateHandleUtils
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils
import tech.sud.mgp.core.ISudFSMStateHandle
import tech.sud.mgp.core.ISudFSTAPP
import tech.sud.mgp.core.ISudListenerInitSDK
import tech.sud.mgp.core.ISudListenerUninitSDK
import tech.sud.mgp.core.SudInitSDKParamModel
import tech.sud.mgp.core.SudMGP

class SudSdkService : SudFSMMGListener {
    companion object {
        const val TAG = "SudSdkService"
    }
    private val gameHttpService: GameHttpService by lazy { GameHttpServiceImpl }
    private var sudFSTAPPDecorator: SudFSTAPPDecorator? = null // app调用sdk的封装类
    private var sudFSMMGDecorator: SudFSMMGDecorator? = null // 用于处理游戏SDK部分回调业务
    private var sudCode: String = ""
    private var gameView: View? = null
    private val gameConfigModel = GameConfigModel() // 游戏配置
    private val listeners: CopyOnWriteArrayList<NEGameListener> by lazy {
        CopyOnWriteArrayList<NEGameListener>()
    }
    init {
        GameLog.i(
            TAG,
            "SudSdkService init,sudFSTAPPDecorator:$sudFSTAPPDecorator,sudFSMMGDecorator:$sudFSMMGDecorator"
        )
    }
    fun initSDK(appKey: String, appId: String, serverUrl: String, callback: NEGameCallback<Unit>) {
        sudFSTAPPDecorator = SudFSTAPPDecorator()
        sudFSMMGDecorator = SudFSMMGDecorator()
        val model = SudInitSDKParamModel()
        model.context = XKitUtils.getApplicationContext()
        model.appId = appId
        model.appKey = appKey
        model.isTestEnv = false
        SudMGP.initSDK(
            model,
            object : ISudListenerInitSDK {
                override fun onSuccess() {
                    GameLog.i(TAG, "initSudSdk onSuccess")
                    callback.onResult(NEGameErrorCode.SUCCESS, "", null)
                    // 给装饰类设置回调
                    sudFSMMGDecorator?.setSudFSMMGListener(this@SudSdkService)
                }

                override fun onFailure(p0: Int, p1: String?) {
                    GameLog.e(TAG, "initSudSdk failed code:$p0,msg:$p1")
                    callback.onResult(NEGameErrorCode.SUCCESS, "", null)
                    listeners.forEach {
                        it.onGameError(
                            NEGameErrorCode.GAME_SDK_INIT_FAILED,
                            NEGameErrorMsg.GAME_SDK_INIT_FAILED
                        )
                    }
                }
            }
        )
    }

    fun loadMG(
        activity: Activity,
        roomUuid: String,
        gameId: Long,
        language: String
    ): ISudFSTAPP? {
        val iSudFSTAPP = SudMGP.loadMG(
            activity,
            getUserId(),
            roomUuid,
            sudCode,
            gameId,
            language,
            sudFSMMGDecorator
        )
        GameLog.i(
            TAG,
            "loadMG,activity:$activity,userId:${getUserId()},roomUuid:$roomUuid,sudCode:$sudCode,gameId:$gameId,language:$language,sudFSMMGDecorator:$sudFSMMGDecorator"
        )
        if (iSudFSTAPP != null) {
            setISudFSTAPP(iSudFSTAPP)
            gameView = iSudFSTAPP.gameView
        }
        return iSudFSTAPP
    }

    private fun setISudFSTAPP(iSudFSTAPP: ISudFSTAPP) {
        // APP调用游戏接口的装饰类设置
        sudFSTAPPDecorator?.setISudFSTAPP(iSudFSTAPP)
    }

    private fun uninitSDK() {
        SudMGP.uninitSDK(object : ISudListenerUninitSDK {
            override fun onSuccess() {
                GameLog.i(TAG, "SudMGP uninitSDK success")
            }

            override fun onFailure(p0: Int, p1: String?) {
                GameLog.e(TAG, "SudMGP uninitSDK error code:$p0,msg:$p1")
                listeners.forEach {
                    it.onGameError(
                        NEGameErrorCode.GAME_SDK_DESTROY_FAILED,
                        NEGameErrorMsg.GAME_SDK_DESTROY_FAILED
                    )
                }
            }
        })
    }

    private fun getUserId(): String {
        val gameKit: GameKitImpl = NEGameKit.getInstance() as GameKitImpl
        return gameKit.getRoomContext()?.localMember?.uuid ?: ""
    }

    // region 游戏侧回调

    override fun onGameStarted() {
        GameLog.i(TAG, "onGameStarted")
    }

    override fun onGameDestroyed() {
        GameLog.i(TAG, "onGameDestroyed")
    }

    override fun onExpireCode(handle: ISudFSMStateHandle, dataJson: String?) {
        GameLog.i(TAG, "onExpireCode,dataJson:$dataJson")
        processOnExpireCode(sudFSTAPPDecorator, handle)
    }

    override fun onGetGameViewInfo(handle: ISudFSMStateHandle, dataJson: String?) {
        GameLog.i(TAG, "onGetGameViewInfo,dataJson:$dataJson")
        processOnGetGameViewInfo(gameView, handle)
    }

    override fun onGetGameCfg(handle: ISudFSMStateHandle, dataJson: String?) {
        GameLog.i(TAG, "onExpireCode,dataJson:$dataJson")
        processOnGetGameCfg(handle, dataJson)
    }

    override fun onGameMGCommonKeyWordToHit(
        handle: ISudFSMStateHandle?,
        model: SudMGPMGState.MGCommonKeyWordToHit?
    ) {
        super.onGameMGCommonKeyWordToHit(handle, model)
        model?.let {
            GameLog.i(TAG, "onGameMGCommonKeyWordToHit handle:$handle,model:$model")
            listeners.forEach {
                it.onGameKeyWordToHit(
                    event = GameKeyWordToHitEvent(
                        channel = NEGameChannel.SUD,
                        handle = handle,
                        message = model
                    )
                )
            }
            ISudFSMStateHandleUtils.handleSuccess(handle)
        }
    }

    override fun onGameMGCommonSelfClickGameSettleCloseBtn(
        handle: ISudFSMStateHandle?,
        model: SudMGPMGState.MGCommonSelfClickGameSettleCloseBtn?
    ) {
        super.onGameMGCommonSelfClickGameSettleCloseBtn(handle, model)
        model?.let {
            GameLog.i(TAG, "onGameMGCommonSelfClickGameSettleCloseBtn handle:$handle,model:$model")
            listeners.forEach {
                it.onGameSelfClickGameSettleClose()
            }
            ISudFSMStateHandleUtils.handleSuccess(handle)
        }
    }

    // endregion 游戏侧回调

    private fun processOnExpireCode(
        sudFSTAPPDecorator: SudFSTAPPDecorator?,
        handle: ISudFSMStateHandle
    ) {
        GameLog.e(TAG, "processOnExpireCode,start get Sud Code")
        gameHttpService.login(object : NetRequestCallback<NELoginResponse> {
            override fun error(code: Int, msg: String?) {
                GameLog.e(TAG, "getCode error code:$code,msg:$msg")
                val mgStateResponse = MGStateResponse()
                mgStateResponse.ret_code = -1
                handle.failure(SudJsonUtils.toJson(mgStateResponse))
            }

            override fun success(info: NELoginResponse?) {
                info?.let {
                    sudCode = it.code
                    GameLog.i(TAG, "getCode success code:${it.code}")
                    val mgStateResponse = MGStateResponse()
                    mgStateResponse.ret_code = MGStateResponse.SUCCESS
                    sudFSTAPPDecorator?.updateCode(it.code, null)
                    handle.success(SudJsonUtils.toJson(mgStateResponse))
                }
            }
        })
    }

    fun destroyMG() {
        sudFSMMGDecorator?.destroyMG()
        sudFSTAPPDecorator?.destroyMG()
        gameView = null
    }

    fun playerIsPlaying(userUuid: String): Boolean? {
        return sudFSMMGDecorator?.playerIsPlaying(userUuid)
    }

    fun playerIsReady(userUuid: String): Boolean? {
        return sudFSMMGDecorator?.playerIsReady(userUuid)
    }

    fun destroy() {
        sudFSTAPPDecorator = null
        sudFSMMGDecorator = null
        gameView = null
        uninitSDK()
        GameLog.i(TAG, "destroy")
    }

    fun addGameListener(listener: NEGameListener) {
        listeners.add(listener)
    }

    fun removeGameListener(listener: NEGameListener) {
        listeners.remove(listener)
    }

    private fun processOnGetGameViewInfo(gameView: View?, handle: ISudFSMStateHandle) {
        gameView ?: return
        // 拿到游戏View的宽高
        val gameViewWidth = gameView.measuredWidth
        val gameViewHeight = gameView.measuredHeight
        if (gameViewWidth > 0 && gameViewHeight > 0) {
            notifyGameViewInfo(handle, gameViewWidth, gameViewHeight)
            return
        }
        // 如果游戏View未加载完成，则监听加载完成时回调
        gameView
            .viewTreeObserver
            .addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        gameView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val width = gameView.measuredWidth
                        val height = gameView.measuredHeight
                        notifyGameViewInfo(handle, width, height)
                    }
                }
            )
    }

    /** 通知游戏，游戏视图信息  */
    private fun notifyGameViewInfo(
        handle: ISudFSMStateHandle, gameViewWidth: Int, gameViewHeight: Int
    ) {
        val gameViewInfoModel = GameViewInfoModel()
        gameViewInfoModel.ret_code = 0
        // 游戏View大小
        gameViewInfoModel.view_size.width = gameViewWidth
        gameViewInfoModel.view_size.height = gameViewHeight
        // 游戏安全操作区域
        for (listener in listeners) {
            listener.getGameRect(gameViewInfoModel)
        }
        // 给游戏侧进行返回
        val json = SudJsonUtils.toJson(gameViewInfoModel)
        // 如果设置安全区有疑问，可将下面的日志打印出来，分析json数据
        GameLog.i(TAG, "notifyGameViewInfo:$json")
        handle.success(json)
    }

    private fun processOnGetGameCfg(handle: ISudFSMStateHandle, dataJson: String?) {
        handle.success(SudJsonUtils.toJson(gameConfigModel))
    }

    fun notifyAPPCommonSelfTextHitState(
        hit: Boolean,
        keyWord: String,
        text: String,
        wordType: String?,
        keyWordList: MutableList<String>?,
        numberList: MutableList<Int>?
    ) {
        sudFSTAPPDecorator?.notifyAPPCommonSelfTextHitState(
            hit,
            keyWord,
            text,
            wordType,
            keyWordList,
            numberList
        )
    }

    fun setSudCode(sudCode: String) {
        this.sudCode = sudCode
    }
    fun getSudCode(): String {
        return sudCode
    }
}

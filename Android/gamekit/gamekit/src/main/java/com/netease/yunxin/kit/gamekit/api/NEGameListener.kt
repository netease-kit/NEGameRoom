/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api

import com.netease.yunxin.kit.gamekit.api.model.event.GameKeyWordToHitEvent
import com.netease.yunxin.kit.gamekit.api.model.event.GameMemberPaintingEvent
import com.netease.yunxin.kit.gamekit.api.model.event.GamePublicMessageEvent
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameCreatedEvent
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameEndedEvent
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameStartedEvent
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel

interface NEGameListener {
    /**
     * 游戏创建成功通知
     */
    fun onGameCreated(event: NEGameCreatedEvent)

    /**
     * 游戏开始通知
     */
    fun onGameStarted(event: NEGameStartedEvent)

    /**
     * 游戏结束通知
     */
    fun onGameEnded(event: NEGameEndedEvent)

    /**
     * 成员参与游戏回调
     * @param members 成员列表   // todo 去掉语聊房的NERoomMember
     */
    fun onMemberJoinGame(members: List<NERoomMember>)

    /**
     * 成员离开游戏回调
     * @param members 成员列表
     */
    fun onMemberLeaveGame(members: List<NERoomMember>)

    /**
     * 游戏异常回调
     * @param code 错误码
     * @param msg 错误信息
     */
    fun onGameError(code: Int, msg: String)

    /**
     * todo
     * 游戏公屏消息回调
     * @param event 事件
     */
    fun onGamePublicMessage(event: GamePublicMessageEvent)

    /**
     * 关键词状态消息回调
     * @param event 事件
     */
    fun onGameKeyWordToHit(event: GameKeyWordToHitEvent)

    /**
     * 游戏积分结算界面关闭回调
     */
    fun onGameSelfClickGameSettleClose()

    /**
     * 设置游戏的安全操作区域，{@link ISudFSMMG}.onGetGameViewInfo()的实现。
     * @param gameViewInfoModel 游戏视图模型
     */
    fun getGameRect(gameViewInfoModel: GameViewInfoModel)

    /**
     * 成员作画中状态消息回调
     * @param event 事件
     */
    fun onMemberPainting(event: GameMemberPaintingEvent)
}

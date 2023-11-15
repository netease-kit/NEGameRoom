// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit
import SudMGPWrapper

@objc public protocol NEGameListener: NSObjectProtocol {
  /**
   * 游戏创建成功     tips：对应服务端协议 createGame(1104,"创建游戏")
   * @param gameInfo 房间内游戏信息
   */
  @objc optional func onGameCreated(event: NEGameCreatedEvent)

  /**
   * 游戏开始通知     tips：对应服务端协议 gameEnd(1103,"游戏结束")
   */
  @objc optional func onGameStarted(event: NEGameStartedEvent)

  /**
   * 游戏结束通知     tips：对应服务端协议 GAME_END(1103,"游戏结束")
   */
  @objc optional func onGameEnded(event: NEGameEndedEvent)

  /**
   * 成员参与游戏回调       tips：对应服务端协议 joinGame(1100,"加入游戏")
   * @param members 成员列表
   */
  @objc optional func onMemberJoinGame(members: [NERoomMember])

  /**
   * 成员离开游戏回调       tips：对应服务端协议 leaveGame(1101,"离开游戏")
   * @param members 成员列表
   */
  @objc optional func onMemberLeaveGame(members: [NERoomMember])

  /**
   * 点击关闭按钮回调
   */

  @objc optional func onGameSelfClickGameSettleClose()

  /**
   * 游戏异常回调
   * @param code 错误码
   * @param msg 错误信息
   */
  @objc optional func onGameError(code: Int, msg: String)

  /**
   * 游戏公屏消息回调
   * @param event 事件
   */
  @objc optional func onGamePublicMessage(event: GamePublicMessageEvent)

  /**
   * 关键词状态消息回调
   * @param event 事件
   */
  @objc optional func onGameKeyWordToHit(event: GameKeyWordToHitEvent)

  /**
   * 成员作画中状态消息回调
   * @param event 事件
   */
  @objc optional func onMemberPainting(event: GameMemberPaintingEvent)

  // 配置获取游戏界面
  @objc optional func onGetGameViewInfo(_ handle: ISudFSMStateHandle, dataJson: String)

  // TODO: 如果有遗漏，需要再把SudFSMMGListener中用到的回调包一遍
}

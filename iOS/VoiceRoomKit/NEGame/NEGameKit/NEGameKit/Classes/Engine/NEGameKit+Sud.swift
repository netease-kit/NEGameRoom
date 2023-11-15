// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SudMGPWrapper
import SudMGP

let gameENV = false

public extension NEGameKit {
  // 创建SudMGPWrapper·
  internal func createSudMGPWrapper() {
    // 构建SudMGPWrapper实例
    sudFSTAPPDecorator = SudFSTAPPDecorator()
    sudFSMMGDecorator = SudFSMMGDecorator()
    // 设置游戏回调监听
    sudFSMMGDecorator?.setEventListener(self)
  }

  // 退出游戏 销毁SudMGP SDK
  internal func logoutGame() {
    sudFSMMGDecorator?.clearAllStates()
    sudFSTAPPDecorator?.destroyMG()
    currentGameId = ""
    gameLoaded = .idle
  }

  /**
   * 加载游戏
   * @param params 加载游戏参数
   * @param callback  回调
   *
   */
  func loadGame(_ params: NELoadGameParams, callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Load game params:\(params.description)")
    Judge.initCondition({
      // 游戏加载中
      if self.gameLoaded == .loading {
        NEGameLog.successLog(kitTag, desc: "game is loadding.")
        callback?(NEGameErrorCode.success, nil, nil)
        return
      }
      /// 游戏已加载，并且和当前游戏一致
      if self.gameLoaded == .loaded, self.currentGameId == params.gameId {
        NEGameLog.successLog(kitTag, desc: "game has been loaded.")
        callback?(NEGameErrorCode.success, nil, nil)
        return
      }

      self.gameLoaded = .loading
      guard let sudFSMMGDecorator = self.sudFSMMGDecorator else {
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to load game . sudFSMMGDecorator is nil "
        )
        self.gameLoaded = .idle
        callback?(NEGameErrorCode.failed, "Failed to load game . sudFSMMGDecorator is nil ", nil)
        return
      }

      guard let userId = self.roomContext?.localMember.uuid, let roomId = self.roomContext?.roomUuid, let gameCode = self.gameCode, let gameId = params.gameId else {
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to load game. invalid params "
        )
        self.gameLoaded = .idle
        callback?(NEGameErrorCode.failed, "Failed to load game invalid params ", nil)
        return
      }
      self.logoutGame()
      self.currentGameId = gameId
      // 必须配置当前登录用户
      sudFSMMGDecorator.setCurrentUserId(userId)
      // 3. 加载SudMGP SDK<SudMGP loadMG>，注：客户端必须持有iSudFSTAPP实例
      let iSudFSTAPP: ISudFSTAPP? = SudMGP.loadMG(userId, roomId: roomId, code: gameCode.code, mgId: Int64(self.currentGameId) ?? 0, language: params.language, fsmMG: sudFSMMGDecorator, rootView: params.gameView)
      if let iSudFSTAPP = iSudFSTAPP {
        self.sudFSTAPPDecorator?.iSudFSTAPP = iSudFSTAPP
        NEGameLog.successLog(kitTag, desc: "Successfully load game.")
        self.gameLoaded = .loaded
        callback?(NEGameErrorCode.success, nil, nil)
      } else {
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to load game . SudMGP.loadMG fail "
        )
        self.gameLoaded = .idle
        callback?(NEGameErrorCode.failed, "Failed to load game . SudMGP.loadMG fail ", nil)
      }
    }, failure: callback)
  }

  /**
   * 卸载游戏
   * @param callback  回调
   */
  func unloadGame(callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "unloadGame game")
    Judge.initCondition({
      self.logoutGame()
      self.localGameMemberMap.removeAll()
      callback?(NEGameErrorCode.success, nil, nil)
      NEGameLog.successLog(kitTag, desc: "Successfully unloadGame game.")
    }, failure: callback)
  }

  /**
   * 销毁SDK
   */
  func destroy() {
    NEGameLog.apiLog(kitTag, desc: "destroy game")
    logoutGame()
    isInitialized = false
    roomContext?.removeRoomListener(listener: self)
    localGameMemberMap.removeAll()
    roomContext = nil
    SudMGP.uninitSDK()
    NEGameLog.successLog(kitTag, desc: "Successfully destroy game.")
  }
}

extension NEGameKit: SudFSMMGListener {
  /// 获取游戏View信息
  public func onGetGameViewInfo(_ handle: ISudFSMStateHandle, dataJson: String) {
    // 屏幕缩放比例，游戏内部采用px，需要开发者获取本设备比值 x 屏幕点数来获得真实px值设置相关字段中
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
      listener.onGetGameViewInfo?(handle, dataJson: dataJson)
    }
  }

  public func onExpireCode(_ handle: ISudFSMStateHandle, dataJson: String) {
    /// Code 过期回调
    /// 获取Code 更新Code
    roomService.getGameCode({ data in
      NEGameLog.successLog(kitTag, desc: "Successfully get game code .")
      self.sudFSTAPPDecorator?.updateCode(data?.code ?? "")
      handle.success(self.sudFSMMGDecorator?.handleMGSuccess() ?? "")
    }) { error in
      NEGameLog.errorLog(
        kitTag,
        desc: "Failed to get game Code. Code: \(error.code). Msg: \(error.localizedDescription)"
      )
      handle.failure(self.sudFSMMGDecorator?.handleMGFailure() ?? "")
    }
  }

  /// 获取游戏Config
  public func onGetGameCfg(_ handle: ISudFSMStateHandle, dataJson: String) {
    // 默认游戏配置
    let m = GameCfgModel.default()
    m.ui.game_settle_close_btn.custom = true
    m.ui.game_settle_again_btn.hide = true
    m.ui.game_bg.hide = true
    m.ui.ping.hide = true
    m.ui.version.hide = true
    m.ui.lobby_player_captain_icon.hide = true
    // 大厅配置隐藏
    m.ui.lobby_game_setting.hide = true
    m.ui.lobby_players.hide = true
    m.ui.ready_btn.hide = true
    m.ui.cancel_join_btn.hide = true
    m.ui.game_setting_select_pnl.hide = true
    m.ui.start_btn.hide = true
    m.ui.share_btn.hide = true
    m.ui.cancel_ready_btn.hide = true
    m.ui.lobby_rule.hide = true
    m.ui.level.hide = true
    m.ui.lobby_help_btn.hide = true
    m.ui.game_setting_select_pnl.hide = true
    m.ui.logo.hide = true
    m.ui.lobby_setting_btn.hide = true
    m.ui.join_btn.hide = true

    handle.success(m.toJSON() ?? "")
  }

  public func onGameMGCommonSelfClickGameSettleCloseBtn(_ handle: ISudFSMStateHandle, model: MGCommonSelfClickGameSettleCloseBtn) {
    localGameMemberMap.removeAll()
    DispatchQueue.main.async {
      for pointerListener in self.listeners.allObjects {
        guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
        listener.onGameSelfClickGameSettleClose?()
      }
    }
  }
}

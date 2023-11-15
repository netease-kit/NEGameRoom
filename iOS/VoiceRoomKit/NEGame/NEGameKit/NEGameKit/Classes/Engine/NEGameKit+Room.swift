// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public extension NEGameKit {
  /**
   * 获取游戏列表
   * @param callback 回调
   */
  func getGameList(_ callback: NEGameCallback<NEGameList>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Get game list.")
    Judge.initCondition({
      self.roomService.gatGameList({ list in
        NEGameLog.successLog(kitTag, desc: "Successfully get game list.")
        callback?(NEGameErrorCode.success, nil, list)
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to get game list. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  /**
   * 创建游戏
   * @param params 创建游戏参数
   * @param callback 回调
   * <br>相关回调：会触发[NEGameListener.onGameCreated]回调
   */
  func createGame(_ params: NECreateGameParams, callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Create game")
    Judge.initCondition({
      self.roomService.createGame(params.gameId, roomUuid: self.roomContext?.roomUuid ?? "", success: {
        // 游戏id赋值
        self.currentGameId = params.gameId
        NEGameLog.successLog(kitTag, desc: "Successfully create game.")
        callback?(NEGameErrorCode.success, nil, nil)
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to create game . Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  /**
   * 参与游戏
   * @param  params 参与游戏参数
   * @param  callback 回调
   * <br>相关回调：会触发[NEGameListener.onMemberJoinGame]回调
   */
  func joinGame(_ params: NEJoinGameParams, callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Join game")
    Judge.initCondition({
      self.roomService.joinGame(params.gameId, roomUuid: self.roomContext?.roomUuid ?? "", success: {
        // 游戏id赋值
        self.currentGameId = params.gameId
        NEGameLog.successLog(kitTag, desc: "Successfully join game.")
        callback?(NEGameErrorCode.success, nil, nil)
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to join game . Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  /**
   * 开始游戏
   * @param params 开始游戏参数
   * @param callback 回调
   * <br>相关回调：会触发[NEGameListener.onGameStarted]回调
   */
  func startGame(_ params: NEStartGameParams, callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Start game")
    Judge.initCondition({
      self.roomService.startGame(params.gameId, roomUuid: self.roomContext?.roomUuid ?? "", success: {
        NEGameLog.successLog(kitTag, desc: "Successfully start game.")
        callback?(NEGameErrorCode.success, nil, nil)
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to start game . Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  /**
   * 主播/房主调用
   * 结束游戏
   * @param  callback 回调
   * <br>相关回调：会触发[NEGameListener.onGameEnded]回调
   */
  func endGame(_ callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "End game")
    Judge.initCondition({
      self.roomService.endGame(self.currentGameId, roomUuid: self.roomContext?.roomUuid ?? "", success: {
        NEGameLog.successLog(kitTag, desc: "Successfully end game.")
        callback?(NEGameErrorCode.success, nil, nil)
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to end game . Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  /**
   * 离开游戏
   * @param callback 回调
   * <br>相关回调：会触发[NEGameListener.onMemberLeaveGame]回调
   */
  func leaveGame(_ callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Leave game")
    Judge.initCondition({
      self.roomService.exitGame(self.currentGameId, roomUuid: self.roomContext?.roomUuid ?? "", success: {
        NEGameLog.successLog(kitTag, desc: "Successfully leave game.")
        callback?(NEGameErrorCode.success, nil, nil)
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to leave game . Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  /**
   * 获取成员状态    tips：用于麦位上游戏状态的判断
   * @param userUuid 用户Id
   * @return 成员状态
   */
  func getMemberState(userUuid: String) -> NEMemberState {
    NEGameLog.apiLog(kitTag, desc: "getMemberState userUuid:\(userUuid)")
    guard let state = (Judge.syncResult {
      if let status = self.localGameMemberMap[userUuid] {
        NEGameLog.successLog(kitTag, desc: "getMemberState userUuid:\(userUuid) state:\(status)")
        return NEMemberState(rawValue: status)
      }
      NEGameLog.successLog(kitTag, desc: "getMemberState userUuid:\(userUuid) state:\(NEMemberState.idle)")
      return NEMemberState.idle
    }) else {
      NEGameLog.successLog(kitTag, desc: "getMemberState userUuid:\(userUuid) state:\(NEMemberState.idle)")
      return NEMemberState.idle
    }
    NEGameLog.successLog(kitTag, desc: "getMemberState userUuid:\(userUuid) state:\(state ?? NEMemberState.idle)")
    return state ?? NEMemberState.idle
  }

  /**
   * 获取房间内游戏信息
   */
  func getRoomGameInfo(_ callback: NEGameCallback<NERoomGameInfo>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "Get game info")
    Judge.initCondition({
      self.roomService.getRoomGameInfo(self.roomContext?.roomUuid ?? "", success: { data in
        NEGameLog.successLog(kitTag, desc: "Successfully get game info.")
        NEGameLog.apiLog(kitTag, desc: "Get game members")
        self.currentGameId = data?.gameId ?? ""
        self.roomService.getGameMembers(self.currentGameId, roomUuid: self.roomContext?.roomUuid ?? "") { members in
          NEGameLog.successLog(kitTag, desc: "Successfully get game members.")
          self.localGameMemberMap.removeAll()
          members?.data?.forEach { member in
            self.localGameMemberMap[member.userUuid] = member.status.rawValue
          }
          callback?(NEGameErrorCode.success, nil, data)
        } failure: { error in
          NEGameLog.successLog(kitTag, desc: "fail get game members.")
          callback?(NEGameErrorCode.success, nil, data)
        }
      }, failure: { error in
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to get game info . Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }
}

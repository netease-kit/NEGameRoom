// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

class NEGameRoomService {
  /// 获取游戏列表
  /// - Parameters:
  ///   - success: 成功
  ///   - failure: 失败
  public func gatGameList(_ success: ((NEGameList?) -> Void)? = nil,
                          failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.gameList.request(returnType: NEGameList.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      success?(data)
    } failed: { error in
      failure?(error)
    }
  }

  /// 创建游戏
  /// - Parameters:
  ///   - gameId: 游戏ID
  ///   - roomUuid: 房间ID
  ///   - success: 成功
  ///   - failure: 失败
  public func createGame(_ gameId: String, roomUuid: String, success: (() -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "gameId": gameId,
      "roomUuid": roomUuid,
    ]
    NEAPI.Room.createGame.request(param) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  /// 加入游戏
  /// - Parameters:
  ///   - gameId: 游戏ID
  ///   - roomUuid: 房间ID
  ///   - liveRecordId: 直播ID
  ///   - success: 成功
  ///   - failure: 失败
  public func joinGame(_ gameId: String, roomUuid: String, success: (() -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "gameId": gameId,
      "roomUuid": roomUuid,
    ]
    NEAPI.Room.joinGame.request(param) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  /// 退出游戏
  /// - Parameters:
  ///   - gameId: 游戏ID
  ///   - roomUuid: 房间ID
  ///   - success: 成功
  ///   - failure: 失败
  public func exitGame(_ gameId: String, roomUuid: String, success: (() -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "gameId": gameId,
      "roomUuid": roomUuid,
    ]

    NEAPI.Room.exitGame.request(param) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  /// 开始游戏
  /// - Parameters:
  ///   - gameId: 游戏ID
  ///   - roomUuid: 房间ID
  ///   - success: 成功
  ///   - failure: 失败
  public func startGame(_ gameId: String, roomUuid: String, success: (() -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "gameId": gameId,
      "roomUuid": roomUuid,
    ]
    NEAPI.Room.startGame.request(param) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  /// 结束游戏
  /// - Parameters:
  ///   - gameId: 游戏ID
  ///   - roomUuid: 房间ID
  ///   - success: 成功
  ///   - failure: 失败
  public func endGame(_ gameId: String, roomUuid: String, success: (() -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    let param: [String: Any] = [
      "gameId": gameId,
      "roomUuid": roomUuid,
    ]
    NEAPI.Room.endGame.request(param) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  /// 获取游戏成员列表信息
  /// - Parameters:
  ///   - gameId: 游戏ID
  ///   - roomUuid: 房间ID
  ///   - success: 成功
  ///   - failure: 失败
  public func getGameMembers(_ gameId: String, roomUuid: String, success: ((NEGameMemberList?) -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.gameMembers(gameId: gameId, roomUuid: roomUuid).request(returnType: NEGameMemberList.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      success?(data)
    } failed: { error in
      failure?(error)
    }
  }

  /// 获取游戏Code
  /// - Parameters:
  ///   - success: 成功
  ///   - failure: 失败
  func getGameCode(_ success: ((NEGameCodeInfo?) -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.getCode.request(returnType: NEGameCodeInfo.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      success?(data)
    } failed: { error in
      failure?(error)
    }
  }

  /// 获取房间游戏信息
  /// - Parameters:
  ///   - roomUuid: 房间ID
  ///   - success: 成功
  ///   - failure: 失败
  public func getRoomGameInfo(_ roomUuid: String, success: ((NERoomGameInfo?) -> Void)? = nil, failure: ((NSError) -> Void)? = nil) {
    NEAPI.Room.gameInfo(roomUuid).request(returnType: NERoomGameInfo.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      success?(data)
    } failed: { error in
      failure?(error)
    }
  }
}

// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit
import SudMGPWrapper
let kitTag = "NEGameKit"

@objcMembers public class NEGameKit: NSObject {
  /// 初始化状态
  public var isInitialized: Bool = false

  /// 单例初始化
  /// - Returns: 单例对象
  public static func getInstance() -> NEGameKit {
    instance
  }

  public var localMember: NERoomMember? {
    Judge.syncResult {
      self.roomContext!.localMember
    }
  }

  /// 添加游戏监听
  /// - Parameter listener: 事件监听

  public func addGameListener(_ listener: NEGameListener) {
    NEGameLog.apiLog(kitTag, desc: "Add game listener.")
    listeners.addWeakObject(listener)
  }

  /// 移除游戏监听
  /// - Parameter listener: 事件监听
  public func removeGameListener(_ listener: NEGameListener) {
    NEGameLog.apiLog(kitTag, desc: "Remove game listener.")
    listeners.removeWeakObject(listener)
  }

  // 维护房间上下文 ---- 内部不进行赋值，只提供外部，万一需要用到NERoomKit的接口，那么需要进行赋值。
  var roomContext: NERoomContext?

  // NEGameKit 初始化
  /// - Parameters:
  ///   - config: 初始化配置
  ///   - callback: 回调
  public func initialize(config: NEGameKitOptions,
                         info: NEGameLoginInfo,
                         callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.infoLog(kitTag, desc: config.description)
    if isInitialized {
      callback?(NEGameErrorCode.success, "NEGameKit isInitialized", nil)
      return
    }
    NEGameLog.setUp(config.appKey)
    NEGameLog.apiLog(kitTag, desc: "Initialize")

    if let baseUrl = config.baseUrl {
      NE.config.customUrl = baseUrl
    }

    self.config = config

    // 创建游戏对象
    createSudMGPWrapper()
    // Roomkit监听
    roomContext = NERoomKit.shared().roomService.getRoomContext(roomUuid: self.config?.roomUuid ?? "")
    if let _ = roomContext {
      // 成功获取到RoomContext
      NEGameLog.successLog(kitTag, desc: "Successfully to get roomContext.")
      login(info) { [weak self] code, msg, obj in
        if code != 0 {
          callback?(code, msg, nil)
        } else {
          guard let self = self else {
            callback?(NEGameErrorCode.failed, "NEGameKit is release", nil)
            return
          }
          // 添加NERoom监听
          self.roomContext?.addRoomListener(listener: self)
          self.isInitialized = true
          callback?(NEGameErrorCode.success, nil, nil)
        }
      }
    } else {
      // 未获取到RoomContext
      NEGameLog.errorLog(kitTag, desc: "fail to get roomContext.")
      callback?(NEGameErrorCode.failed, "fail to get roomContext", nil)
    }
  }

  /// 更新游戏成员
  /// - Parameter account: 成员ID
  public func updateGameMember(_ accounts: [String]) {
    for key in localGameMemberMap.keys {
      if !accounts.contains(key) {
        localGameMemberMap.removeValue(forKey: key)
      }
    }
  }

  // MARK: - ------------------------- Private method --------------------------

  private static let instance = NEGameKit()
  // 监听器数组
  var listeners = NSPointerArray.weakObjects()

  var config: NEGameKitOptions?
  // 房间服务
  private var _roomService = NEGameRoomService()
  internal var roomService: NEGameRoomService { _roomService }

  // 游戏相关
  public var sudFSTAPPDecorator: SudFSTAPPDecorator?
  public var sudFSMMGDecorator: SudFSMMGDecorator?
  // 短期令牌code
  internal var gameCode: NEGameCodeInfo?
  // 游戏ID
  internal var currentGameId: String = ""
  // 本地维护游戏数据
  internal var localGameMemberMap: [String: Int] = [:]

  // 本地游戏是否已经加载
  internal var gameLoaded: NEGameLoadStatus = .idle
}

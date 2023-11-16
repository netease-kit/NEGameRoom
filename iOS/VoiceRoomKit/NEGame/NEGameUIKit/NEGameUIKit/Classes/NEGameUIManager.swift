// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import NEVoiceRoomKit

public class NEGameUIManager: NSObject {
  public static let instance = NEGameUIManager()

  internal var config: NEVoiceRoomKitConfig?
  internal var account: String = ""
  internal var token: String = ""
  internal var baseUrl: String = ""
  public var nickname: String?
  internal var configId: Int = 0

  override private init() {
    super.init()
  }

  public func initialize(config: NEVoiceRoomKitConfig, configId: Int, callback: ((Int, String?, AnyObject?) -> Void)?) {
    self.configId = configId
    self.config = config
    if let baseUrl = config.extras["baseUrl"] {
      self.baseUrl = baseUrl
    }
    NEVoiceRoomKit.getInstance().initialize(config: config, callback: callback)
    NEVoiceRoomKit.getInstance().addAuthListener(self)
  }

  public func login(account: String, token: String, nickname: String, callback: ((Int, String?, AnyObject?) -> Void)?) {
    self.nickname = nickname
    self.account = account
    self.token = token
    NEVoiceRoomKit.getInstance().login(account, token: token, callback: callback)
  }

  public var gameJoinRoomEvent: (() -> Void)?
  public var gameLeaveRoomEvent: (() -> Void)?
  public var checkIfInOtherRoom: (() -> Bool)?
  public var leaveOtherRoom: (((() -> Void)?) -> Void)?
  public var gameClientEvent: ((NEVoiceRoomAuthEvent) -> Void)?
}

extension NEGameUIManager: NEVoiceRoomAuthListener {
  public func onVoiceRoomAuthEvent(_ event: NEVoiceRoomAuthEvent) {
    gameClientEvent?(event)
  }
}

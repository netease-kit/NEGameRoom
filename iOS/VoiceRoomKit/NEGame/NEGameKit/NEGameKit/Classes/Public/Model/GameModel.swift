// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SudMGPWrapper
import NERoomKit

// 登录账号信息
@objc public class NEGameLoginInfo: NSObject {
  public var account: String = ""
  public var token: String = ""
}

@objc public class NEGameCodeInfo: NSObject, Codable {
  // 忽然code
  public var code: String
  // 忽然appId
  public var appId: String
  // 忽然appKey
  public var appKey: String
}

// 游戏列表
@objc public class NEGameList: NSObject, Codable {
  public var data: [NEGame]?
}

/// 游戏对象
@objc public class NEGame: NSObject, Codable {
  // 游戏ID
  public var gameId: String = ""
  // 游戏名称
  public var gameName: String = ""
  // 游戏描述
  public var gameDesc: String = ""
  // 游戏规则
  public var rule: String = ""
  // 游戏缩略图
  public var thumbnail: String = ""

  override public init() {
    super.init()
  }

  public convenience init(event: NEGameCreatedEvent) {
    self.init()
    gameId = event.gameId
    gameName = event.gameName
    gameDesc = event.gameDesc
    rule = event.rule
    thumbnail = event.thumbnail
  }

  public convenience init(gameInfo: NERoomGameInfo) {
    self.init()
    gameId = gameInfo.gameId ?? ""
    gameName = gameInfo.gameName ?? ""
    gameDesc = gameInfo.gameDesc ?? ""
    rule = gameInfo.rule ?? ""
    thumbnail = gameInfo.thumbnail ?? ""
  }
}

// 游戏成员列表
@objc public class NEGameMemberList: NSObject, Codable {
  public var data: [NEGameMember]?
}

// 游戏状态
@objc public enum NEGameState: Int, Codable {
  // 准备中
  case ready = 0
  // 游戏中
  case playing = 1
  // 空闲/退出
  case idle = -1
}

// 成员游戏状态
@objc public enum NEMemberState: Int, Codable {
  // 准备中
  case ready = 0
  // 游戏中
  case playing = 1
  // 空闲/退出
  case idle = 2
}

// 游戏成员
@objc public class NEGameMember: NSObject, Codable {
  // 游戏ID
  public var gameId: String
  // 房间号
  public var roomUuid: String
  // 游戏名称
  public var gameName: String
  public var appkey: String?
  // 玩家id
  public var userUuid: String
  // 游戏状态
  public var status: NEMemberState = .idle
  /// 用户昵称
  public var userName: String
  /// 直播编号
  public var liveRecordId: Int64 = 0
  // 房间唯一编号
  public var roomArchiveId: String
}

/**
 * 创建游戏参数
 */
@objc public class NECreateGameParams: NSObject {
  // 游戏ID
  public var gameId: String = ""
  // 扩展字段
  public var extra: String = ""
}

@objc public class NEJoinGameParams: NSObject {
  // 游戏ID
  public var gameId: String = ""
}

@objc public class NEStartGameParams: NSObject {
  // 游戏ID
  public var gameId: String = ""
}

@objc public class NEEndGameParams: NSObject {
  // 游戏ID
  public var gameId: String = ""
}

/**
 * 房间游戏信息
 * @param game 游戏信息
 * @param gameStatus 游戏状态  todo 游戏未开始、游戏进行中
 */
@objc public class NERoomGameInfo: NSObject, Codable {
  public var gameId: String?
  public var roomUuid: String = ""
  public var gameName: String?
  public var gameDesc: String?
  public var thumbnail: String?
  public var rule: String?
  public var userUuid: String?
  public var gameStatus: NEGameState = .idle
  public var userName: String?
  public var liveRecordId: Int64 = 0
  public var appKey: String = ""
  public var roomArchiveId: String = ""
}

@objc public class NELoadGameParams: NSObject {
  // 游戏ID
  public var gameId: String?
  // 语言
  public var language: String = "zh-CN"
  /// 加载展示视图
  public var gameView: UIView = .init()
}

// MARK: 以下是游戏相关的数据

public enum NEGameChannel: Int {
  case sud = 0
}

// 公屏消息模型
@objc public class GamePublicMessageEvent: NSObject {
  public var channel: NEGameChannel = .sud
  public var handle: ISudFSMStateHandle?
  public var message: MGCommonPublicMessageModel?
}

// 关键词命中模型
@objc public class GameKeyWordToHitEvent: NSObject {
  public var channel: NEGameChannel = .sud
  public var handle: ISudFSMStateHandle?
  public var message: MGCommonKeyWrodToHitModel?
}

// 你话我猜 对应数据模型
@objc public class GameMemberPaintingEvent: NSObject {
  public var channel: NEGameChannel = .sud
  public var member: NERoomMember?
  public var handle: ISudFSMStateHandle?
  public var message: MGDGPaintingModel?
}

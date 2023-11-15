// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct NEAPIItem: NEAPIProtocol {
  let urlPath: String
  var url: String { NE.config.baseUrl + urlPath }
  let description: String
  let extra: String?
  var method: NEHttpMethod
  init(_ url: String,
       desc: String,
       method: NEHttpMethod = .post,
       extra: String? = nil) {
    urlPath = url
    self.method = method
    description = desc
    self.extra = extra
  }
}

enum NEAPI {
  // Game模块
  enum Room {
    static let gameList = NEAPIItem("/nemo/game/list", desc: "游戏列表", method: .get)
    static let createGame = NEAPIItem("/nemo/game/create", desc: "创建游戏")
    static let joinGame = NEAPIItem("/nemo/game/join", desc: "加入游戏")
    static let exitGame = NEAPIItem("/nemo/game/exit", desc: "退出游戏")
    static let startGame = NEAPIItem("/nemo/game/start", desc: "开始游戏")
    static let endGame = NEAPIItem("/nemo/game/end", desc: "结束游戏")

    static func gameMembers(gameId: String, roomUuid: String) -> NEAPIItem {
      NEAPIItem("/nemo/game/members?roomUuid=\(roomUuid)&gameId=\(gameId)", desc: "游戏成员列表查询", method: .get)
    }

    static let getCode = NEAPIItem("/nemo/game/sud/login", desc: "忽然游戏账号code获取")
    static func gameInfo(_ roomUuid: String) -> NEAPIItem {
      NEAPIItem("/nemo/game/gameInfo?roomUuid=\(roomUuid)", desc: "游戏信息查询", method: .get)
    }

    // TODO: 待服务端补充

    static let stateUpload = NEAPIItem("/nemo/game/info", desc: "游戏状态上报")
  }
}

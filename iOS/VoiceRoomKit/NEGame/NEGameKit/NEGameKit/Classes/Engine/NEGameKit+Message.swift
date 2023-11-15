// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit

extension NEGameKit: NERoomListener {
  public func onReceiveChatroomMessages(messages: [NERoomChatMessage]) {
    DispatchQueue.main.async {
      for message in messages {
        switch message.messageType {
        case .custom:
          if let msg = message as? NERoomChatCustomMessage {
            self.handleCustomMessage(msg)
          }
        case .image: break
        case .file: break
        default: break
        }
      }
    }
  }

  /// 处理RoomKit自定义消息
  func handleCustomMessage(_ message: NERoomChatCustomMessage) {
    NEGameLog.infoLog(kitTag, desc: "Receive custom message.")
    guard let dic = message.attachStr?.toDictionary() else { return }
    NEGameLog.infoLog(kitTag, desc: "custom message:\(dic)")
    // 服务端协议解析
    if let data = dic["data"] as? [String: Any],
       let jsonData = try? JSONSerialization.data(withJSONObject: data, options: []),
       let jsonString = String(data: jsonData, encoding: .utf8),
       let type = dic["type"] as? Int {
      handleGameMessage(type, jsonString: jsonString)
    }
  }

  /// 处理游戏消息
  internal func handleGameMessage(_ type: Int, jsonString: String) {
    switch type {
    case NEGameConstant.joinGame.rawValue:
      // 玩家加入游戏
      if let obj = NEGameDecoder.decode(
        NEGameMemberJoinEvent.self,
        jsonString: jsonString
      ) {
        localGameMemberMap[obj.userUuid] = obj.status

        DispatchQueue.main.async {
          for pointerListener in self.listeners.allObjects {
            guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
            guard let member = self.roomContext?.getMember(uuid: obj.userUuid) else {
              NEGameLog.infoLog(kitTag, desc: "roomkit get member is nil.")
              return
            }
            listener.onMemberJoinGame?(members: [member])
          }
        }
      }

    case NEGameConstant.gameEnd.rawValue:
      // 游戏结束
      if let obj = NEGameDecoder.decode(
        NEGameEndedEvent.self,
        jsonString: jsonString
      ) {
        // 卸载游戏
        unloadGame()
        // 数据清空
        localGameMemberMap.removeAll()
        DispatchQueue.main.async {
          for pointerListener in self.listeners.allObjects {
            guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
            listener.onGameEnded?(event: obj)
          }
        }
      }
    case NEGameConstant.gameStart.rawValue:
      // 游戏开始
      // 数据变更
      localGameMemberMap = localGameMemberMap.mapValues { member in
        if member == NEMemberState.ready.rawValue {
          return NEMemberState.playing.rawValue
        } else {
          return member
        }
      }
      if let obj = NEGameDecoder.decode(
        NEGameStartedEvent.self,
        jsonString: jsonString
      ) {
        DispatchQueue.main.async {
          for pointerListener in self.listeners.allObjects {
            guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
            listener.onGameStarted?(event: obj)
          }
        }
      }
    case NEGameConstant.gameLeave.rawValue:
      // 退出游戏
      if let obj = NEGameDecoder.decode(
        NEGameMemberLeaveEvent.self,
        jsonString: jsonString
      ) {
        localGameMemberMap.removeValue(forKey: obj.userUuid)
        DispatchQueue.main.async {
          for pointerListener in self.listeners.allObjects {
            guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
            guard let member = self.roomContext?.getMember(uuid: obj.userUuid) else {
              NEGameLog.infoLog(kitTag, desc: "roomkit get member is nil.")
              return
            }
            listener.onMemberLeaveGame?(members: [member])
          }
        }
      }

    case NEGameConstant.createGame.rawValue:
      if let obj = NEGameDecoder.decode(
        NEGameCreatedEvent.self,
        jsonString: jsonString
      ) {
        DispatchQueue.main.async {
          for pointerListener in self.listeners.allObjects {
            guard pointerListener is NEGameListener, let listener = pointerListener as? NEGameListener else { continue }
            listener.onGameCreated?(event: obj)
          }
        }
      }
    default: break
    }
  }
}

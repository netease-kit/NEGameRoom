// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SudMGP
import SudMGPWrapper
@objcMembers
public class NEGameCreatedEvent: NSObject, Codable {
  public var id: Int = 0
  public var appKey: String = ""
  public var liveRecordId: Int = 0
  public var roomUuid: String = ""
  public var gameCreator: String = ""
  public var gameStatus: Int = 0
  public var gameId: String = ""
  public var gameName: String = ""
  public var gameDesc: String = ""
  public var rule: String = ""
  public var thumbnail: String = ""
  public var roomArchiveId: String = ""
}

@objcMembers
public class NEGameEndedEvent: NSObject, Codable {
  public var id: Int = 0
  public var appKey: String = ""
  public var liveRecordId: Int = 0
  public var roomUuid: String = ""
  public var gameCreator: String = ""
  public var gameStatus: Int = 0
  public var gameId: String = ""
  public var gameName: String = ""
  public var gameDesc: String = ""
  public var thumbnail: String = ""
  public var roomArchiveId: String = ""
}

@objcMembers
public class NEGameStartedEvent: NSObject, Codable {
  public var id: Int = 0
  public var appKey: String = ""
  public var liveRecordId: Int = 0
  public var roomUuid: String = ""
  public var gameCreator: String = ""
  public var gameStatus: Int = 0
  public var gameId: String = ""
  public var gameName: String = ""
  public var gameDesc: String = ""
  public var thumbnail: String = ""
  public var roomArchiveId: String = ""
}

@objcMembers
public class NEGameMemberJoinEvent: NSObject, Codable {
  public var id: Int = 0
  public var appKey: String = ""
  public var liveRecordId: Int = 0
  public var roomUuid: String = ""
  public var status: Int = 0
  public var joinTime: Int64 = 0
  public var gameId: String = ""
  public var userUuid: String = ""
  public var userName: String = ""
  public var roomArchiveId: String = ""
}

@objcMembers
public class NEGameMemberLeaveEvent: NSObject, Codable {
  public var id: Int = 0
  public var appKey: String = ""
  public var liveRecordId: Int = 0
  public var roomUuid: String = ""
  public var status: Int = 0
  public var joinTime: Int64 = 0
  public var gameId: String = ""
  public var userUuid: String = ""
  public var userName: String = ""
  public var roomArchiveId: String = ""
}

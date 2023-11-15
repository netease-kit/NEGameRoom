// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers

/// NEGameKit 配置项
public class NEGameKitOptions: NSObject {
  /// appKey 为roomkit的Key
  public var appKey: String = ""
  /// 房间ID
  public var roomUuid: String = ""
  /// 服务器地址
  public var baseUrl: String?
}

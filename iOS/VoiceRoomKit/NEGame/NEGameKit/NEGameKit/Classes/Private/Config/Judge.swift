// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERoomKit
struct Judge {
  /// 初始化判断条件
  static func initCondition<T: Any>(_ success: @escaping () -> Void,
                                    failure: NEGameCallback<T>? = nil) {
    guard NEGameKit.getInstance().isInitialized else {
      NEGameLog.errorLog(kitTag, desc: "NEGameKit Uninitialized.")
      failure?(NEGameErrorCode.failed, "NEGameKit Uninitialized.", nil)
      return
    }

    guard let _ = NEGameKit.getInstance().roomContext else {
      NEGameLog.errorLog(kitTag, desc: "RoomContext is nil.")
      failure?(NEGameErrorCode.failed, "RoomContext is nil.", nil)
      return
    }

    success()
  }

  /// 同步返回
  static func syncResult<T: Any>(_ success: @escaping () -> T) -> T? {
    guard NEGameKit.getInstance().isInitialized else {
      NEGameLog.errorLog(kitTag, desc: "Uninitialized.")
      return nil
    }
    guard let _ = NEGameKit.getInstance().roomContext else {
      NEGameLog.errorLog(kitTag, desc: "RoomContext is nil.")
      return nil
    }
    return success()
  }
}

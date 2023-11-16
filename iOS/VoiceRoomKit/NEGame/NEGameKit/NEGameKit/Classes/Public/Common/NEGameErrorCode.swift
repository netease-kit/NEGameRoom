// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers

/// 错误码
public class NEGameErrorCode: NSObject {
  /// 失败
  public static var failed: Int { -1 }
  /// 成功
  public static var success: Int { 0 }

  /// 游戏初始化失败
  public static var gameInitFailed: Int { 101 }
  /// 游戏销毁失败
  public static var gameDestroyFailed: Int { 102 }
}

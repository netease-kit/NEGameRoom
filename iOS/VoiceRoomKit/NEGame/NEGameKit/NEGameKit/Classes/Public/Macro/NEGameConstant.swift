// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
/// 游戏事件
public enum NEGameConstant: Int {
  /// 加入游戏
  case joinGame = 1100
  /// 退出游戏
  case gameLeave = 1101
  /// 游戏开始
  case gameStart = 1102
  /// 游戏结束
  case gameEnd = 1103
  /// 创建游戏
  case createGame = 1104
}

/// 游戏状态
public enum NEGameLoadStatus: Int {
  /// 未加载
  case idle = 0
  /// 加载中
  case loading = 1
  /// 加载完成
  case loaded = 2
}

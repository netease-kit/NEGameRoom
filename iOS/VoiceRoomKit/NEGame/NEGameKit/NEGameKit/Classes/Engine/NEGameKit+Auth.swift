// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SudMGP

internal extension NEGameKit {
  func login(_ info: NEGameLoginInfo, callback: NEGameCallback<AnyObject>? = nil) {
    NEGameLog.apiLog(kitTag, desc: "login,account:\(info.account),token:\(info.token)")
    // headers添加属性
    NE.addHeader([
      "user": info.account,
      "token": info.token,
      "appkey": config?.appKey ?? "",
    ])

    // 请求业务服务接口获取游戏初始化SDK需要的code码<reqGameLoginWithSuccess>
    roomService.getGameCode({ [weak self] code in
      // Code 赋值
      self?.gameCode = code
      NEGameLog.successLog(kitTag, desc: "Successfully get gameCode.")
      // 初始化游戏
      guard let code = code else {
        NEGameLog.errorLog(
          kitTag,
          desc: "Failed to getGameCode code is nil"
        )
        callback?(NEGameErrorCode.failed, "Failed to getGameCode code is nil", nil)
        return
      }

      DispatchQueue.main.async {
        SudMGP.initSDK(code.appId, appKey: code.appKey, isTestEnv: gameENV) { retCode, retMsg in
          if retCode != 0 {
            NEGameLog.errorLog(
              kitTag,
              desc: "Failed to initialize game. Code: \(retCode). Msg: \(retMsg)"
            )
            callback?(Int(retCode), retMsg, nil)
            return
          }
          NEGameLog.successLog(kitTag, desc: "Successfully initialize game.")
          callback?(NEGameErrorCode.success, nil, nil)
        }
      }
    }) { error in
      NEGameLog.errorLog(
        kitTag,
        desc: "Failed to get game code. Code: \(error.code). Msg: \(error.localizedDescription)"
      )
      callback?(error.code, error.description, nil)
    }
  }
}

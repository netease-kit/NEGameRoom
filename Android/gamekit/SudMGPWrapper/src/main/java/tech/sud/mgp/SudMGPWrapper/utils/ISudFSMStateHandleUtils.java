// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package tech.sud.mgp.SudMGPWrapper.utils;

import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse;
import tech.sud.mgp.core.ISudFSMStateHandle;

public class ISudFSMStateHandleUtils {

  /**
   * 回调游戏，成功
   *
   * @param handle
   */
  public static void handleSuccess(ISudFSMStateHandle handle) {
    MGStateResponse response = new MGStateResponse();
    response.ret_code = MGStateResponse.SUCCESS;
    response.ret_msg = "success";
    handle.success(SudJsonUtils.toJson(response));
  }
}

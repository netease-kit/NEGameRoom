// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package tech.sud.mgp.SudMGPWrapper.state;

/** mg2app，状态响应 */
public class MGStateResponse {

  // 返回码，成功
  public static final int SUCCESS = 0;

  public int ret_code; // 返回码
  public String ret_msg; // 返回消息
}

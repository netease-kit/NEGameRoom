// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package tech.sud.mgp.SudMGPWrapper.utils;

import android.util.Log;

public class LogUtil {
  private static final String PREFIX = "LogUtil->";

  public static void i(String tag, String msg) {
    Log.i(PREFIX + tag, msg);
  }
}

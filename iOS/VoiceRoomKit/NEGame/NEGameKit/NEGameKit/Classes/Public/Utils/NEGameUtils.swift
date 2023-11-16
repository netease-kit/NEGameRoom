// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc public class NEGameUtils: NSObject {
  static func safeAreaInsets() -> UIEdgeInsets {
    if #available(iOS 11.0, *) {
      return UIApplication.shared.keyWindow?.safeAreaInsets ?? UIEdgeInsets.zero
    } else {
      // Fallback on earlier versions
      return UIEdgeInsets.zero
    }
  }
}

// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import QuartzCore

// MARK: - NodePropertyMap

protocol NodePropertyMap {
  var properties: [AnyNodeProperty] { get }
}

extension NodePropertyMap {
  var childKeypaths: [KeypathSearchable] {
    []
  }

  var keypathLayer: CALayer? {
    nil
  }

  /// Checks if the node's local contents need to be rebuilt.
  func needsLocalUpdate(frame: CGFloat) -> Bool {
    for property in properties {
      if property.needsUpdate(frame: frame) {
        return true
      }
    }
    return false
  }

  /// Rebuilds only the local nodes that have an update for the frame
  func updateNodeProperties(frame: CGFloat) {
    properties.forEach { property in
      property.update(frame: frame)
    }
  }
}
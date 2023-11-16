// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import SnapKit

@objcMembers
public class NEGameAlertView: UIView {
  private let backView = UIView()
  private let whiteView = UIView()
  private let titleLabel = UILabel()
  public let descLabel = UILabel()
  private let horizontalLineLabel = UILabel()
  private let verticalLineLabel = UILabel()

  private let sureButton = UIButton()
  private let cancelButton = UIButton()
  public var clickSure: (() -> Void)?

  override init(frame: CGRect) {
    super.init(frame: frame)
    loadSubViews()
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func loadSubViews() {
    addSubview(backView)
    backView.backgroundColor = UIColor(hexString: "#000000", 0.4)
    backView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    addSubview(whiteView)
    whiteView.backgroundColor = .white
    whiteView.layer.masksToBounds = true
    whiteView.layer.cornerRadius = 8
    whiteView.snp.makeConstraints { make in
      make.center.equalToSuperview()
      make.width.equalTo(270)
      make.height.equalTo(160)
    }

    whiteView.addSubview(titleLabel)
    titleLabel.textColor = UIColor(hexString: "#222222")
    titleLabel.font = UIFont.systemFont(ofSize: 17)
    titleLabel.text = NEGameUIBundle.localized("提示")
    titleLabel.textAlignment = .center
    titleLabel.accessibilityIdentifier = "id.titleLabel"
    titleLabel.snp.makeConstraints { make in
      make.centerX.equalTo(whiteView)
      make.top.equalTo(whiteView).offset(20)
      make.height.equalTo(26)
    }

    whiteView.addSubview(horizontalLineLabel)
    horizontalLineLabel.backgroundColor = UIColor(hexString: "#222222", 0.1)
    horizontalLineLabel.text = ""
    horizontalLineLabel.snp.makeConstraints { make in
      make.left.right.equalTo(whiteView)
      make.bottom.equalTo(whiteView).offset(-42)
      make.height.equalTo(1)
    }

    whiteView.addSubview(descLabel)
    descLabel.textColor = UIColor(hexString: "#999999")
    descLabel.font = UIFont.systemFont(ofSize: 14)
    descLabel.numberOfLines = 0
    descLabel.text = NEGameUIBundle.localized("上麦后才能加入游戏，是否确认\n申请上麦")
    descLabel.textAlignment = .center
    descLabel.accessibilityIdentifier = "id.descLabel"
    descLabel.snp.makeConstraints { make in
      make.top.equalTo(titleLabel.snp.bottom).offset(8)
      make.bottom.equalTo(horizontalLineLabel.snp.top).offset(-10)
      make.left.right.equalTo(whiteView)
    }

    whiteView.addSubview(verticalLineLabel)
    verticalLineLabel.backgroundColor = UIColor(hexString: "#222222", 0.1)
    verticalLineLabel.text = ""
    verticalLineLabel.snp.makeConstraints { make in
      make.top.equalTo(horizontalLineLabel.snp.bottom)
      make.bottom.equalTo(whiteView)
      make.centerX.equalTo(whiteView)
      make.width.equalTo(1)
    }

    whiteView.addSubview(cancelButton)
    cancelButton.backgroundColor = .clear
    cancelButton.setTitleColor(UIColor(hexString: "#222222"), for: .normal)
    cancelButton.setTitle(NEGameUIBundle.localized("取消"), for: .normal)
    cancelButton.titleLabel?.font = UIFont.systemFont(ofSize: 17)
    cancelButton.addTarget(self, action: #selector(clickCancelButton), for: .touchUpInside)
    cancelButton.accessibilityIdentifier = "id.cancelButton"
    cancelButton.snp.makeConstraints { make in
      make.left.bottom.equalTo(whiteView)
      make.top.equalTo(horizontalLineLabel.snp.bottom)
      make.right.equalTo(verticalLineLabel.snp.left)
      make.bottom.equalTo(whiteView)
    }

    whiteView.addSubview(sureButton)
    sureButton.backgroundColor = .clear
    sureButton.setTitleColor(UIColor(hexString: "#337EFF"), for: .normal)
    sureButton.setTitle(NEGameUIBundle.localized("确定"), for: .normal)
    sureButton.titleLabel?.font = UIFont.systemFont(ofSize: 17)
    sureButton.addTarget(self, action: #selector(clickSureButton), for: .touchUpInside)
    sureButton.accessibilityIdentifier = "id.suerBtn"
    sureButton.snp.makeConstraints { make in
      make.right.bottom.equalTo(whiteView)
      make.top.equalTo(horizontalLineLabel.snp.bottom)
      make.left.equalTo(verticalLineLabel.snp.right)
      make.bottom.equalTo(whiteView)
    }
  }

  @objc private func clickSureButton() {
    removeFromSuperview()
    clickSure?()
  }

  @objc private func clickCancelButton() {
    removeFromSuperview()
  }
}

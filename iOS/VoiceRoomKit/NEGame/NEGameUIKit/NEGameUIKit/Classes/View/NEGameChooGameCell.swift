// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
class NEGameChooGameCell: UITableViewCell {
  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    backgroundColor = .clear
    selectionStyle = .none
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubViews() {
    contentView.addSubview(bottomBackView)
    bottomBackView.addSubview(gameImageView)
    bottomBackView.addSubview(gameTitleLabel)
    bottomBackView.addSubview(gameDetailLabel)

    bottomBackView.snp.makeConstraints { make in

      make.left.equalTo(contentView).offset(20)
      make.right.equalTo(contentView).offset(-20)
      make.top.equalTo(contentView).offset(8)
      make.bottom.equalTo(contentView).offset(-8)
    }
    gameImageView.snp.makeConstraints { make in
      make.centerY.equalTo(bottomBackView)
      make.left.equalTo(bottomBackView).offset(12)
      make.width.height.equalTo(80)
    }

    gameTitleLabel.snp.makeConstraints { make in
      make.centerY.equalTo(bottomBackView).offset(-14)
      make.left.equalTo(gameImageView.snp.right).offset(14)
      make.height.equalTo(20)
      make.right.equalTo(bottomBackView.snp.right).offset(-10)
    }
    gameDetailLabel.snp.makeConstraints { make in
      make.centerY.equalTo(bottomBackView).offset(14)
      make.left.equalTo(gameTitleLabel)
      make.height.equalTo(20)
      make.right.equalTo(gameTitleLabel)
    }
  }

  lazy var gameImageView: UIImageView = {
    let view = UIImageView()
    view.contentMode = .center
    view.accessibilityIdentifier = "id.gameImageView"
    return view
  }()

  lazy var gameTitleLabel: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 16)
    view.accessibilityIdentifier = "id.gameTitleLabel"
    return view

  }()

  lazy var gameDetailLabel: UILabel = {
    let view = UILabel()
    view.textColor = UIColor(hexString: "#FFFFFF", 0.7)
    view.font = UIFont(name: "PingFangSC-Regular", size: 12)
    view.accessibilityIdentifier = "id.gameDetailLabel"
    return view
  }()

  lazy var bottomBackView: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor(hexString: "#969BE8", 0.1)
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 5
    view.accessibilityIdentifier = "id.bottomBackView"
    return view
  }()
}

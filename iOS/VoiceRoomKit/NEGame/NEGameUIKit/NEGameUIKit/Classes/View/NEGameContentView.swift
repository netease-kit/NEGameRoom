// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import Foundation
import NECommonKit
import SnapKit

class NEGameContentView: UIView {
  var isAnchor = true
  var anchorEnterGameEvent: ((Int) -> Void)?
  var anchorStartGameEvent: ((Int) -> Void)?
  var anchorCloseGameEvent: (() -> Void)?
  var audienceEnterGameEvent: ((Int) -> Void)?

  /// 背景渐变底色，目前只配置了你画我猜的颜色
  let bottomColorArray: [[CGColor]] = [[UIColor(hexString: "#0762FF").cgColor, UIColor(hexString: "#337EFF").cgColor, UIColor(hexString: "#3F85FF").cgColor]]

  var gradientColorArray: [[CGColor]] = [[UIColor(hexString: "#325FFF").cgColor, UIColor(hexString: "#2D9AFF").cgColor], [UIColor(hexString: "#FF1B5C").cgColor, UIColor(hexString: "#FF5C8B").cgColor]]

  override init(frame: CGRect) {
    super.init(frame: frame)
  }

  convenience init(_ isAnchor: Bool) {
    self.init(frame: .zero)
    self.isAnchor = isAnchor
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    gradientLayer.frame = bottomView.bounds
  }

  // UI 重置
  func resetSubViews() {
    setAudienceEventState(0)
    setAnchorEnterGameEventState(0)
    setAnchorStartGameEventState(0)
  }

  func loadSubViews() {
    addSubview(bottomView)
    bottomView.snp.makeConstraints { make in
      make.left.top.equalTo(self).offset(10)
      make.right.bottom.equalTo(self).offset(-10)
    }
    bottomView.layer.addSublayer(gradientLayer)
    bottomView.addSubview(titleLabel)
    titleLabel.snp.makeConstraints { make in
      make.left.equalTo(bottomView.snp.left).offset(20)
      make.top.equalTo(bottomView.snp.top).offset(8)
      make.height.equalTo(20)
    }
    bottomView.addSubview(commentButton)
    commentButton.snp.makeConstraints { make in
      make.left.equalTo(titleLabel.snp.right).offset(5)
      make.centerY.equalTo(titleLabel)
    }
    bottomView.addSubview(centerView)
    centerView.snp.makeConstraints { make in
      make.top.equalTo(bottomView.snp.top).offset(35)
      make.left.equalTo(bottomView.snp.left).offset(10)
      make.right.equalTo(bottomView.snp.right).offset(-10)
      make.bottom.equalTo(bottomView.snp.bottom).offset(-10)
    }

    centerView.addSubview(gameRoleImageView)
    gameRoleImageView.snp.makeConstraints { make in
      make.centerX.equalTo(centerView)
      make.top.equalTo(centerView).offset(13)
      make.width.equalTo(180)
      make.height.equalTo(53)
    }

    centerView.addSubview(roleLabel)
    roleLabel.snp.makeConstraints { make in
      make.top.equalTo(centerView.snp.top).offset(66)
      make.left.right.equalTo(centerView)
      make.bottom.equalTo(centerView.snp.bottom).offset(-91)
    }

    if isAnchor {
      // 关闭按钮
      bottomView.addSubview(closeButton)
      closeButton.snp.makeConstraints { make in
        make.centerY.equalTo(titleLabel)
        make.right.equalTo(bottomView).offset(-10)
        make.height.equalTo(20)
        make.width.equalTo(40)
      }

      // 参与游戏按钮
      centerView.addSubview(anchorEnterGameButton)
      anchorEnterGameButton.snp.makeConstraints { make in
        make.centerX.equalTo(centerView).offset(-78)
        make.bottom.equalTo(centerView).offset(-18)
        make.width.equalTo(144)
        make.height.equalTo(48)
      }
      // 开始游戏按钮
      centerView.addSubview(anchorStartGameButton)
      anchorStartGameButton.snp.makeConstraints { make in
        make.centerX.equalTo(centerView).offset(78)
        make.bottom.equalTo(centerView).offset(-18)
        make.width.equalTo(144)
        make.height.equalTo(48)
      }
    } else {
      // 观众
      centerView.addSubview(audienceEventButton)
      audienceEventButton.snp.makeConstraints { make in
        make.centerX.equalTo(centerView)
        make.bottom.equalTo(centerView).offset(-18)
        make.width.equalTo(144)
        make.height.equalTo(48)
      }
    }
  }

  // MARK: lazy load

  // 底部视图
  lazy var bottomView: UIView = {
    let view = UIView()
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 10
    view.backgroundColor = .gray
    view.accessibilityIdentifier = "id.bottomView"
    return view
  }()

  // 底部渐变背景色
  lazy var gradientLayer: CAGradientLayer = {
    let gradientLayer = CAGradientLayer()
    gradientLayer.colors = bottomColorArray[0]
    gradientLayer.startPoint = CGPoint(x: 0, y: 0)
    gradientLayer.endPoint = CGPoint(x: 0, y: 1)
    gradientLayer.locations = [0.0, 0.5, 1.0]
    gradientLayer.cornerRadius = 10
    return gradientLayer
  }()

  // 游戏标题
  lazy var titleLabel: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 12)
    view.accessibilityIdentifier = "id.titleLabel"
    return view
  }()

  // 感叹号图标
  lazy var commentButton: UIButton = {
    let view = UIButton()
    view.setImage(NEGameUIBundle.loadImage("comment_icon"), for: .normal)
    view.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    view.addTarget(self, action: #selector(clickCommentButton), for: .touchUpInside)
    view.accessibilityIdentifier = "id.commentButton"
    view.isHidden = true
    return view
  }()

  // 中心白色实图
  lazy var centerView: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 10
    view.accessibilityIdentifier = "id.centerView"
    return view
  }()

  // 游戏规则图片
  lazy var gameRoleImageView: UIImageView = {
    let view = UIImageView()
    view.image = NEGameUIBundle.loadImage("gameRole_icon")
    view.contentMode = .center
    view.accessibilityIdentifier = "id.gameRoleImageView"
    return view
  }()

  // 游戏规则详情
  lazy var roleLabel: UILabel = {
    let view = UILabel()
    view.textColor = .black
    view.backgroundColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 12)
    view.numberOfLines = 0
    view.accessibilityIdentifier = "id.roleLabel"
    view.textAlignment = .center
    view.contentMode = .top
    return view
  }()

  // 关闭按钮
  lazy var closeButton: UIButton = {
    let view = UIButton()
    view.setTitleColor(UIColor(hexString: "#FFFFFF", 0.7), for: .normal)
    view.setTitle(NEGameUIBundle.localized("关闭"), for: .normal)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 12)
    view.backgroundColor = UIColor(hexString: "#000000", 0.5)
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 10
    view.addTarget(self, action: #selector(clickCloseButton), for: .touchUpInside)
    view.accessibilityIdentifier = "id.closeButton"
    return view
  }()

  // 规则刷新，需要重新计算行高
  func updateLabel(_ label: UILabel, text: String) {
    let attributedString = NSMutableAttributedString(string: text)
    let paragraphStyle = NSMutableParagraphStyle()
    paragraphStyle.alignment = .center
    paragraphStyle.lineSpacing = 12
    attributedString.addAttribute(.paragraphStyle, value: paragraphStyle, range: NSRange(location: 0, length: attributedString.length))
    label.attributedText = attributedString
  }

  // 麦上观众点击事件按钮
  lazy var audienceEventButton: gradientButton = {
    let view = gradientButton(colorArray: gradientColorArray[0])
    view.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 16)
    view.setTitle(NEGameUIBundle.localized("参与游戏"), for: .normal)
    view.backgroundColor = UIColor(hexString: "#E8E8E8")
    view.addTarget(self, action: #selector(clickAudienceEventButton), for: .touchUpInside)
    view.accessibilityIdentifier = "id.audienceEventButton"
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 24
    view.tag = 0 // 默认为 0
    return view
  }()

  // 主播参与游戏点击事件按钮
  lazy var anchorEnterGameButton: gradientButton = {
    let view = gradientButton(colorArray: gradientColorArray[0])
    view.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 16)
    view.setTitle(NEGameUIBundle.localized("参与游戏"), for: .normal)
    view.backgroundColor = UIColor(hexString: "#E8E8E8")
    view.addTarget(self, action: #selector(clickAnchorEnterGameButton), for: .touchUpInside)
    view.accessibilityIdentifier = "id.anchorEnterGameButton"
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 24
    view.tag = 0 // 默认为 0
    return view
  }()

  // 主播开始游戏点击事件按钮
  lazy var anchorStartGameButton: gradientButton = {
    let view = gradientButton(colorArray: gradientColorArray[1])
    view.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 16)
    view.setTitle(NEGameUIBundle.localized("开始游戏"), for: .normal)
    view.backgroundColor = UIColor(hexString: "#FFBDD0")
    view.addTarget(self, action: #selector(clickAnchorStartGameButton), for: .touchUpInside)
    view.accessibilityIdentifier = "id.anchorStartGameButton"
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 24
    view.tag = 0 // 默认为 1
    return view
  }()

  // 麦上人员按钮状态设置
  func setAudienceEventState(_ state: Int) {
    switch state {
    case 0:
      // 参与游戏
      audienceEventButton.gradientLayer.removeFromSuperlayer()
      audienceEventButton.layer.insertSublayer(audienceEventButton.gradientLayer, at: 0)
      audienceEventButton.setTitle(NEGameUIBundle.localized("参与游戏"), for: .normal)
      audienceEventButton.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
      audienceEventButton.tag = 0
    case 1:
      // 退出游戏
      audienceEventButton.gradientLayer.removeFromSuperlayer()
      audienceEventButton.setTitle(NEGameUIBundle.localized("退出"), for: .normal)
      audienceEventButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
      audienceEventButton.tag = 1
    case 2:
      // 人数已满
      audienceEventButton.gradientLayer.removeFromSuperlayer()
      audienceEventButton.setTitle(NEGameUIBundle.localized("人数已满"), for: .normal)
      audienceEventButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
      audienceEventButton.tag = 2
    default:
      print("未匹配上状态")
    }
  }

  // 主播参与按钮状态设置
  func setAnchorEnterGameEventState(_ state: Int) {
    switch state {
    case 0:
      // 开始游戏
      anchorEnterGameButton.gradientLayer.removeFromSuperlayer()
      anchorEnterGameButton.layer.insertSublayer(anchorEnterGameButton.gradientLayer, at: 0)
      anchorEnterGameButton.setTitle(NEGameUIBundle.localized("参与游戏"), for: .normal)
      anchorEnterGameButton.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
      anchorEnterGameButton.tag = 0
    case 1:
      // 不能开始游戏
      anchorEnterGameButton.gradientLayer.removeFromSuperlayer()
      anchorEnterGameButton.tag = 1
      anchorEnterGameButton.setTitle(NEGameUIBundle.localized("退出"), for: .normal)
      anchorEnterGameButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    case 2:
      // 人数已满
      anchorEnterGameButton.gradientLayer.removeFromSuperlayer()
      anchorEnterGameButton.setTitle(NEGameUIBundle.localized("人数已满"), for: .normal)
      anchorEnterGameButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
      anchorEnterGameButton.tag = 2
    default:
      print("未匹配上状态")
    }
  }

  // 主播开始按钮状态设置
  func setAnchorStartGameEventState(_ state: Int) {
    switch state {
    case 0:
      // 参与游戏
      anchorStartGameButton.gradientLayer.removeFromSuperlayer()
      anchorStartGameButton.layer.insertSublayer(anchorStartGameButton.gradientLayer, at: 0)
      anchorStartGameButton.setTitle(NEGameUIBundle.localized("开始游戏"), for: .normal)
      anchorStartGameButton.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
      anchorStartGameButton.tag = 0
    case 1:
      // 退出游戏
      anchorStartGameButton.gradientLayer.removeFromSuperlayer()
      anchorStartGameButton.setTitle(NEGameUIBundle.localized("退出"), for: .normal)
      anchorStartGameButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
      anchorStartGameButton.tag = 1
    case 2:
      // 人数已满
      anchorStartGameButton.gradientLayer.removeFromSuperlayer()
      anchorStartGameButton.setTitle(NEGameUIBundle.localized("人数已满"), for: .normal)
      anchorStartGameButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
      anchorStartGameButton.tag = 2
    default:
      print("未匹配上状态")
    }
  }

  @objc func clickCommentButton() {
    print("clickCommentButton")
  }

  @objc func clickCloseButton() {
    print("clickCloseButton")
    anchorCloseGameEvent?()
  }

  @objc func clickAudienceEventButton() {
    print("clickAudienceEventButton")
    audienceEnterGameEvent?(audienceEventButton.tag)
  }

  @objc func clickAnchorEnterGameButton() {
    print("clickAnchorEnterGameButton")
    anchorEnterGameEvent?(anchorEnterGameButton.tag)
  }

  @objc func clickAnchorStartGameButton() {
    print("clickAnchorStartGameButton")
    NotificationCenter.default.post(name: NSNotification.Name("gameStart"), object: nil)
    anchorStartGameEvent?(anchorStartGameButton.tag)
  }
}

class gradientButton: UIButton {
  var bottomColorArray: [CGColor] = [UIColor(hexString: "#325FFF").cgColor, UIColor(hexString: "#2D9AFF").cgColor]

  override init(frame: CGRect) {
    super.init(frame: frame)
    layer.addSublayer(gradientLayer)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  convenience init(colorArray: [CGColor]) {
    self.init(frame: .zero)
    bottomColorArray = colorArray
  }

  override func layoutSubviews() {
    super.layoutSubviews()
    gradientLayer.frame = bounds
    gradientLayer.cornerRadius = bounds.size.height / 2
    gradientLayer.colors = bottomColorArray
  }

  // 底部渐变背景色
  lazy var gradientLayer: CAGradientLayer = {
    let gradientLayer = CAGradientLayer()
    gradientLayer.colors = bottomColorArray
    gradientLayer.startPoint = CGPoint(x: 0, y: 0)
    gradientLayer.endPoint = CGPoint(x: 1, y: 0)
    gradientLayer.locations = [0.0, 1.0]
    gradientLayer.cornerRadius = 10
    return gradientLayer
  }()
}

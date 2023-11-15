// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NEGameKit
import NECommonUIKit

class NEGameChooseGameView: UIView, UITableViewDataSource, UITableViewDelegate {
  var dataArray: NEGameList = .init()
  var selectGame: ((NEGame) -> Void)?
  var dismiss: (() -> Void)?
  override init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = UIColor(hexString: "#222222")
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func refreshGameList(_ failure: NEGameCallback<Any>? = nil) {
    NEGameKit.getInstance().getGameList { [weak self] code, msg, gameList in
      if code == 0 {
        DispatchQueue.main.async {
          self?.dataArray = gameList ?? NEGameList()
          self?.gameListTableView.reloadData()
        }
      } else {
        failure?(code, msg, nil)
      }
    }
  }

  func loadSubViews() {
    addSubview(titleLable)
    titleLable.snp.makeConstraints { make in
      make.centerX.equalTo(self)
      make.centerY.equalTo(self).offset(-164)
    }
    addSubview(whiteLable)
    whiteLable.snp.makeConstraints { make in
      make.centerX.equalTo(titleLable)
      make.top.equalTo(titleLable.snp.bottom).offset(24)
      make.width.equalTo(20)
      make.height.equalTo(4)
    }
    addSubview(gameListTableView)
    gameListTableView.snp.makeConstraints { make in
      make.left.right.equalTo(self)
      make.top.equalTo(whiteLable.snp.bottom).offset(24)
      make.bottom.equalTo(self).offset(50)
    }
  }

  lazy var titleLable: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.text = NEGameUIBundle.localized("选择你想玩的游戏")
    view.font = UIFont(name: "PingFangSC-Regular", size: 20)
    view.accessibilityIdentifier = "id.titleLable"
    return view
  }()

  lazy var whiteLable: UILabel = {
    let view = UILabel()
    view.textColor = .white
    view.text = ""
    view.backgroundColor = .white
    view.accessibilityIdentifier = "id.whiteLable"
    return view
  }()

  lazy var gameListTableView: UITableView = {
    let view = UITableView()
    view.translatesAutoresizingMaskIntoConstraints = false
    view.backgroundColor = .clear
    view.dataSource = self
    view.delegate = self
    view.separatorStyle = .none
    view.register(NEGameChooGameCell.self, forCellReuseIdentifier: "NEGameChooGameCell")
    return view
  }()
}

extension NEGameChooseGameView {
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    dataArray.data?.count ?? 0
  }

  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if let cell = tableView.dequeueReusableCell(withIdentifier: "NEGameChooGameCell", for: indexPath) as? NEGameChooGameCell {
      let item = dataArray.data?[indexPath.row] as? NEGame
      cell.gameImageView.sd_setImage(with: URL(string: item?.thumbnail ?? ""))
      cell.gameTitleLabel.text = item?.gameName
      cell.gameDetailLabel.text = item?.gameDesc
      return cell
    } else {
      return UITableViewCell()
    }
  }

  func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    116
  }

  func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    if let item = dataArray.data?[indexPath.row] as? NEGame {
      NotificationCenter.default.post(name: NSNotification.Name("gameChoose"), object: nil)
      selectGame?(item)
    }
  }

  override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    dismiss?()
  }
}

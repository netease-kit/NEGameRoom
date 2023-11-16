// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import LottieSwift
import NECoreKit
import SnapKit
import UIKit
import NEGameUIKit

class NPTHomeCell: UICollectionViewCell {
  override init(frame: CGRect) {
    super.init(frame: frame)

    contentView.backgroundColor = .clear
    contentView.addSubview(backgroundImage)
    backgroundImage.addSubview(lottieView)
    backgroundImage.addSubview(typeLabel)
    backgroundImage.addSubview(descriptionLabel)

    backgroundImage.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    lottieView.snp.makeConstraints { make in
      make.edges.equalToSuperview()
    }

    typeLabel.snp.makeConstraints { make in
      make.left.equalTo(backgroundImage).offset(20)
      make.top.equalTo(backgroundImage).offset(20)
      make.right.equalTo(backgroundImage)
      make.height.equalTo(30)
    }

    descriptionLabel.snp.makeConstraints { make in
      make.left.equalTo(backgroundImage).offset(20)
      make.top.equalTo(typeLabel.snp.bottom).offset(4)
      make.width.lessThanOrEqualTo(150)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var backgroundImage: UIImageView = {
    let image = UIImageView(image: UIImage(named: "home_blue"))
    image.clipsToBounds = true
    image.layer.cornerRadius = 8
    return image
  }()

  lazy var lottieView: LottieAnimationView = {
    let lottie = LottieAnimationView(name: "oneOnOne")
    lottie.loopMode = .loop
    return lottie
  }()

  lazy var typeLabel: UILabel = {
    var view = UILabel()
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Medium", size: 18)
    view.accessibilityIdentifier = "id.tvFunctionName"
    return view
  }()

  lazy var descriptionLabel: UILabel = {
    var view = UILabel()
    view.numberOfLines = 0
    view.backgroundColor = .clear
    view.textColor = .white
    view.font = UIFont(name: "PingFangSC-Regular", size: 14)
    view.accessibilityIdentifier = "id.tvFunctionDesc"
    return view
  }()
}

class NPTHomeViewController: UIViewController {
  var privateLatter: ((String) -> Void)?
  override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view.

    navigationItem.leftBarButtonItem = leftBarButtonItem
    navigationItem.rightBarButtonItem = rightBarButtonItem

    if #available(iOS 13.0, *) {
      let appearance = UINavigationBarAppearance()
      appearance.backgroundColor = .white
      navigationController?.navigationBar.scrollEdgeAppearance = appearance
      navigationController?.navigationBar.standardAppearance = appearance
      navigationController?.navigationBar.tintColor = .black
    } else {
      navigationController?.navigationBar.tintColor = .black
      navigationController?.navigationBar.barTintColor = .white
      navigationController?.navigationBar.isTranslucent = false
    }

    view.backgroundColor = .white
    view.addSubview(collectionView)

    collectionView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(20)
      make.right.equalToSuperview().offset(-20)
      if #available(iOS 11.0, *) {
        make.top.equalTo(view.safeAreaLayoutGuide).offset(20)
        make.bottom.equalTo(view.safeAreaLayoutGuide).offset(-20)
      } else {
        make.top.equalToSuperview().offset(20)
        make.bottom.equalToSuperview().offset(-20)
      }
    }
  }

  lazy var leftBarButtonItem: UIBarButtonItem = {
    let item = UIBarButtonItem()
    let image = UIImageView()
    image.image = UIDevice.isChinese ? UIImage(named: "yunxin") : UIImage(named: "yunxin_en")
    item.customView = image
    return item
  }()

  lazy var rightBarButtonItem: UIBarButtonItem = {
    let item = UIBarButtonItem()
    let btn = UIButton()
    btn.setTitle("Feedback".localized, for: .normal)
    btn.setTitleColor(UIColor.partyBlack, for: .normal)
    btn.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 17)
    btn.addTarget(self, action: #selector(feedback), for: .touchUpInside)
    item.customView = btn
    return item
  }()

  @objc func feedback() {
    let viewController = NPTFeedbackViewController()
    viewController.hidesBottomBarWhenPushed = true
    navigationController?.pushViewController(viewController, animated: true)
  }

  lazy var collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    layout.minimumInteritemSpacing = 10
    layout.minimumLineSpacing = 10
    layout.sectionInset = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
    layout.itemSize = CGSize(width: (view.bounds.width - 50) / 2, height: 210)

    let view = UICollectionView(frame: .zero, collectionViewLayout: layout)
    view.delegate = self
    view.dataSource = self
    view.register(NPTHomeCell.self, forCellWithReuseIdentifier: "NPTHomeCell")
    view.backgroundColor = .white
    view.showsHorizontalScrollIndicator = false
    view.showsVerticalScrollIndicator = false
    return view
  }()
}

extension NPTHomeViewController: UICollectionViewDelegate {
  func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    collectionView.deselectItem(at: indexPath, animated: false)
    if let delegate = UIApplication.shared.delegate as? AppDelegate {
      if !delegate.checkNetwork() {
        return
      }
    }
    let vc = NEGameRoomListViewController(liveType: .game)
    vc.hidesBottomBarWhenPushed = true
    navigationController?.pushViewController(vc, animated: true)
  }
}

extension NPTHomeViewController: UICollectionViewDataSource {
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    1
  }

  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    if let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "NPTHomeCell", for: indexPath) as? NPTHomeCell {
      cell.backgroundImage.image = UIImage(named: "home_org")
      cell.lottieView.animation = LottieAnimation.named("gameRoom")
      cell.typeLabel.text = "Game_Room".localized
      cell.descriptionLabel.text = "Game_Room_Description".localized
      cell.lottieView.play()
      return cell
    }
    return UICollectionViewCell()
  }
}

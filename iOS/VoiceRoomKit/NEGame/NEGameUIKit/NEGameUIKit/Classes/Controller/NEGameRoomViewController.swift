// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NEVoiceRoomBaseUIKit
import NEVoiceRoomKit
import NEGameKit
import NESocialUIKit
import NERoomKit
import SudMGPWrapper

let tag = "NEGameRoomViewController"

public class NEGameRoomViewController: NEVRBaseViewController, NEGameListener {
  var currentGame: NEGame?
  // 游戏房独有的布局
  override public func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(true, animated: true)
  }

  override public func customLayout() {
    // 添加麦位列表视图
    view.addSubview(seatsExView)
    seatsExView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(8)
      make.right.equalToSuperview().offset(-8)
      make.height.equalTo(63)
      make.top.equalTo(headerView.snp.bottom).offset(5)
    }
    let whole = [defaultOwnerSeat] + defaultAudienceSeats
    seatsExView.setupWholeModels(whole)
    seatsExView.clickedAction = { [weak self] cellModel in
      self?.clickSeatCell(model: cellModel)
    }

    loadSubViews()
    // 第一期先隐藏小窗按钮，实际小窗逻辑已经实现，要使用时打开就行
    headerView.smallWindowBtn.isHidden = true
    headerView.leaveGameBtn.isHidden = true
    headerView.leaveGameAction = { [weak self] in
      // 退出游戏
      NEGameLog.infoLog(tag, desc: "leaveGameAction")
      self?.showEndGameView()
    }
  }

  override public func viewDidLoad() {
    super.viewDidLoad()
    NEGameKit.getInstance().addGameListener(self)

    NEGameUIManager.instance.gameJoinRoomEvent?()
  }

  override public func dismiss() {
    if let controller = presentedViewController {
      controller.dismiss(animated: false)
    }
    if let list = navigationController?.viewControllers.first(where: { $0.isKind(of: NEGameRoomListViewController.self) }) {
      navigationController?.popToViewController(list, animated: true)
    } else {
      navigationController?.popViewController(animated: true)
    }
  }

  // 当自己从麦上到了麦下，比如自己下麦或者被踢下麦，离开游戏
  override public func didSelfLeaveSeat() {
    NEGameKit.getInstance().leaveGame()
    DispatchQueue.main.async { [weak self] in
      guard let self = self else { return }
      if self.isOwner {
        // 正常情况 无法进不来，异常case保障，主播被踢或者下麦
        self.gameContentView.setAnchorEnterGameEventState(0)
      } else {
        self.gameContentView.setAudienceEventState(0)
      }
    }
  }

  // MARK: 踢麦，下麦，关闭麦位，如果此时麦上的人在游戏中，需要额外提示

  override public func kickAction(_ model: NEVRBaseSeatCellModel) -> NESocialActionSheetAction {
    NESocialActionSheetAction(title: NEVRBaseBundle.localized("Seat_Kick")) { [weak self] _ in
      if let uuid = model.uuid {
        if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .playing {
          let alert = UIAlertController(title: NEGameUIBundle.localized("提示"), message: NEGameUIBundle.localized("当前正在游戏中，踢麦后用户将退出游戏，是否确认踢麦？"), preferredStyle: .alert)
          alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("确定"), style: .default) { _ in
            self?.baseKickSeat(model)
          })
          alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("取消"), style: .cancel))
          self?.present(alert, animated: true)
        } else {
          self?.baseKickSeat(model)
        }
      }
    }
  }

  override public func closeAction(_ model: NEVRBaseSeatCellModel) -> NESocialActionSheetAction {
    NESocialActionSheetAction(title: NEVRBaseBundle.localized("Seat_Close")) { [weak self] _ in
      if let uuid = model.uuid {
        if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .playing {
          let alert = UIAlertController(title: NEGameUIBundle.localized("提示"), message: NEGameUIBundle.localized("当前正在游戏中，关麦后用户将退出游戏，是否确认关麦？"), preferredStyle: .alert)
          alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("确定"), style: .default) { _ in
            self?.baseCloseSeats(model)
          })
          alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("取消"), style: .cancel))
          self?.present(alert, animated: true)
        } else {
          self?.baseCloseSeats(model)
        }
      }
    }
  }

  override public func leaveAction(_ model: NEVRBaseSeatCellModel) -> NESocialActionSheetAction {
    NESocialActionSheetAction(title: NEVRBaseBundle.localized("Seat_Leave"), titleColor: .red) { [weak self] _ in
      if let uuid = model.uuid {
        if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .playing {
          let alert = UIAlertController(title: NEGameUIBundle.localized("提示"), message: NEGameUIBundle.localized("当前正在游戏中，下麦后将退出游戏，是否确认下麦？"), preferredStyle: .alert)
          alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("确定"), style: .default) { _ in
            self?.baseLeaveAction()
          })
          alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("取消"), style: .cancel))
          self?.present(alert, animated: true)
        } else {
          self?.baseLeaveAction()
        }
      }
    }
  }

  override public func leaveRoom(callback: (() -> Void)? = nil) {
    func baseLeaveRoom() {
      super.leaveRoom(callback: callback)
    }

    if NEGameKit.getInstance().getMemberState(userUuid: NEVoiceRoomKit.getInstance().localMember?.account ?? "") == .playing {
      let alert = UIAlertController(title: NEGameUIBundle.localized("提示"), message: NEGameUIBundle.localized("当前正在游戏中，是否确认离开房间？"), preferredStyle: .alert)
      alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("确定"), style: .default) { _ in
        baseLeaveRoom()
      })
      alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("取消"), style: .cancel) { _ in
        callback?()
      })
      present(alert, animated: true)
    } else {
      baseLeaveRoom()
    }
  }

  lazy var seatsExView: NEVRBaseSeatsViewEx = .init(frame: .zero)

  lazy var gameContentView: NEGameContentView = {
    let view = NEGameContentView(isOwner)
    view.anchorEnterGameEvent = { [weak self] step in
      // 主播参与游戏
      switch step {
      case 0:
        // 参与游戏
        let joinGameParams = NEJoinGameParams()
        joinGameParams.gameId = self?.currentGame?.gameId ?? ""
        NEGameKit.getInstance().joinGame(joinGameParams) { [weak self] code, msg, obj in
          DispatchQueue.main.async {
            if code == 0 {
              self?.gameContentView.setAnchorEnterGameEventState(1)
            } else {
              // 参与游戏失败
              DispatchQueue.main.async {
                self?.showToastInWindow(NEGameUIBundle.localized("参与游戏失败，请稍后再试"))
              }
            }
          }
        }
      case 1:
        // 退出游戏
        NEGameKit.getInstance().leaveGame { [weak self] code, msg, obj in
          DispatchQueue.main.async {
            if code == 0 {
              self?.gameContentView.setAnchorEnterGameEventState(0)
            } else {
              // 退出游戏失败
              DispatchQueue.main.async {
                self?.showToastInWindow(NEGameUIBundle.localized("退出游戏失败，请稍后再试"))
              }
            }
          }
        }
      case 2: break
      default: break
      }
    }

    view.anchorStartGameEvent = { [weak self] step in
      switch step {
      case 0:
        // 开始游戏亮红色
        guard let gameId = self?.currentGame?.gameId else {
          // 开始游戏失败
          self?.showToastInWindow(NEGameUIBundle.localized("开始游戏失败，请稍后再试"))
          return
        }
        let startGameParams = NEStartGameParams()
        startGameParams.gameId = gameId
        NEGameKit.getInstance().startGame(startGameParams) { [weak self] code, msg, obj in
          if code == 0 {
            // 游戏正常开始
            self?.setCurrentGameProcess(2)
          } else {
            // 开始发生错误，跳出提示
            DispatchQueue.main.async {
              if code == 403 {
                self?.showToastInWindow(NEGameUIBundle.localized("参与人数不足，无法开始游戏"))

              } else {
                self?.showToastInWindow(NEGameUIBundle.localized(msg ?? ""))
              }
            }
          }
        }
      case 1: break
      default: break
      }
    }

    view.audienceEnterGameEvent = { [weak self] step in
      guard let self = self else { return }
      switch step {
      case 0:
        // 判断是否在麦位上
        if self.isMemberOnSeat(NEVoiceRoomKit.getInstance().localMember?.account ?? "") {
          // 麦上
          // 参与游戏
          let joinGameParams = NEJoinGameParams()
          joinGameParams.gameId = self.currentGame?.gameId ?? ""
          NEGameKit.getInstance().joinGame(joinGameParams) { [weak self] code, msg, obj in
            DispatchQueue.main.async {
              if code == 0 {
                self?.gameContentView.setAudienceEventState(1)
              } else {
                // 参与游戏失败
                self?.showToastInWindow(NEGameUIBundle.localized("参与游戏失败，请稍后再试"))
              }
            }
          }
        } else {
          // 申请上麦
          self.showNotOnSeatView()
        }
      case 1:
        // 退出游戏
        NEGameKit.getInstance().leaveGame { [weak self] code, msg, obj in
          DispatchQueue.main.async {
            if code == 0 {
              self?.gameContentView.setAudienceEventState(0)
            } else {
              // 退出游戏失败
              self?.showToastInWindow(NEGameUIBundle.localized("退出游戏失败，请稍后再试"))
            }
          }
        }
      case 2: break

      default: break
      }
    }

    view.anchorCloseGameEvent = { [weak self] in
      self?.showEndGameView()
    }
    return view
  }()

  lazy var gameDetailView: UIView = .init()

  lazy var chooseGameButton: gradientButton = {
    let view = gradientButton(colorArray: [UIColor(hexString: "325FFF").cgColor, UIColor(hexString: "2D9AFF").cgColor])
    view.layer.cornerRadius = 20
    view.setImage(NEGameUIBundle.loadImage("chooseGame_icon"), for: .normal)
    view.bringSubviewToFront(view.imageView ?? UIImageView())
    view.setTitle(NEGameUIBundle.localized("选择游戏"), for: .normal)
    view.titleEdgeInsets = UIEdgeInsets(top: -1, left: 10, bottom: 0, right: 0)
    view.imageEdgeInsets = UIEdgeInsets(top: 0, left: -10, bottom: 0, right: 0)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    view.layer.borderColor = UIColor.white.cgColor
    view.layer.borderWidth = 1
    view.addTarget(self, action: #selector(clickChooseGameButton), for: .touchUpInside)
    view.accessibilityIdentifier = "id.chooseGameButton"
    return view
  }()

  lazy var notOnSeatView: NEGameAlertView = {
    let view = NEGameAlertView(frame: UIScreen.main.bounds)
    view.clickSure = { [weak self] in
      NEVoiceRoomKit.getInstance().requestSeat { [weak self] code, msg, _ in
        if code != 0, let self = self {
          DispatchQueue.main.async {
            self.showToastInWindow(NEGameUIBundle.localized(msg ?? ""))
          }
        }
      }
    }
    return view
  }()

  lazy var endGameAlertView: NEGameAlertView = {
    let view = NEGameAlertView(frame: UIScreen.main.bounds)
    view.descLabel.text = NESocialBundle.localized("是否确定退出当前游戏")
    view.clickSure = { [weak self] in
      NEGameKit.getInstance().endGame { code, msg, obj in
        if code == 0 {
          // 关闭成功
        } else {
          DispatchQueue.main.async {
            self?.showToastInWindow(NEGameUIBundle.localized("关闭游戏失败"))
          }
        }
      }
    }
    return view
  }()

  lazy var gameChooseGameView: NEGameChooseGameView = .init()

  lazy var gameChooseViewController: UIViewController = {
    let gameChooseViewController = UIViewController()
    gameChooseViewController.modalPresentationStyle = .fullScreen
    gameChooseViewController.view.addSubview(gameChooseGameView)
    gameChooseGameView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(gameChooseViewController.view)
    }
    gameChooseGameView.dismiss = { [weak self] in
      self?.gameChooseViewController.dismiss(animated: false)
    }

    gameChooseGameView.selectGame = { [weak self] game in
      self?.currentGame = game
      let createGameParams = NECreateGameParams()
      createGameParams.gameId = game.gameId
      NEGameKit.getInstance().createGame(createGameParams) { [weak self] code, msg, obj in
        if code == 0 {
          // 创建成功
        } else {
          // 创建失败
          DispatchQueue.main.async {
            self?.showToastInWindow(NEGameUIBundle.localized("创建游戏失败,请稍后再试"))
          }
        }
      }
      self?.gameChooseViewController.dismiss(animated: false)
    }

    return gameChooseViewController
  }()

  // 判断用户是否在麦上
  func isMemberOnSeat(_ account: String) -> Bool {
    (NEVoiceRoomKit.getInstance().localSeats?.first(where: { $0.user == account })) != nil
  }

  @objc func clickChooseGameButton() {
    // 判断是否在麦上
    if let _ = NEVoiceRoomKit.getInstance().localSeats?.first(where: { $0.user == NEVoiceRoomKit.getInstance().localMember?.account }) {
      // 在麦上
      present(gameChooseViewController, animated: true, completion: nil)

      gameChooseGameView.refreshGameList { [weak self] code, msg, obj in
        DispatchQueue.main.async {
          self?.showToastInWindow(NEGameUIBundle.localized("获取游戏列表失败"))
        }
      }
    } else {
      // 未在麦上
    }

    // 获取房间详情数据，房间是否游戏中
    // 获取游戏列表数据
    // 展示

    print("clickCommentButton")
  }

  override public func onSendTextMessage(message: NESocialChatroomTextMessage) {
    super.onSendTextMessage(message: message)
    // 判断当前用户是否为游戏中
    guard let userUuid = NEVoiceRoomKit.getInstance().localMember?.account else { return }
    if NEGameKit.getInstance().getMemberState(userUuid: userUuid) == .playing {
      handleGameKeywordHitting(content: message.text ?? "")
    }
  }

  func handleGameKeywordHitting(content: String) {
    if let sudFSTAPPDecorator = NEGameKit.getInstance().sudFSTAPPDecorator,
//           let sudFSMMGDecorator = NEGameKit.getInstance().sudFSMMGDecorator,
       let keyWordHiting = NEGameKit.getInstance().sudFSMMGDecorator?.keyWordHiting,
       keyWordHiting == true,
       let drawKeyWord = NEGameKit.getInstance().sudFSMMGDecorator?.drawKeyWord,
       content == drawKeyWord {
      // 关键词命中
      sudFSTAPPDecorator.notifyAppComonDrawTextHit(true, keyWord: drawKeyWord, text: drawKeyWord)
    }
  }

  override public func onSeatListChanged(seats: [NEVoiceRoomSeatItem]) {
    var accountArray: [String] = []
    for seat in seats {
      if let account = seat.user, account.count > 0 {
        accountArray.append(account)
      }
    }
    NEGameKit.getInstance().updateGameMember(accountArray)
    refreshSeatStatus()
  }

  override public func joinOrRejoinChatroom(firstTime: Bool) {
    NEGameLog.infoLog(tag, desc: "joinOrRejoinChatroom,firstTime = \(firstTime) ,roomUuid=\(joinParams.roomUuid)")
    if firstTime {
      // 第一次进入
      let options = NEGameKitOptions()
      options.appKey = NEGameUIManager.instance.config?.appKey ?? ""
      options.roomUuid = joinParams.roomUuid
      options.baseUrl = NEGameUIManager.instance.baseUrl
      let loginInfo = NEGameLoginInfo()
      loginInfo.account = NEGameUIManager.instance.account
      loginInfo.token = NEGameUIManager.instance.token
      NEGameKit.getInstance().initialize(config: options, info: loginInfo) { [weak self] code, msg, obj in
        guard let self = self else { return }
        if code == 0 {
          if !self.isOwner {
            NEGameKit.getInstance().getRoomGameInfo { [weak self] code, msg, obj in
              if code == 0 {
                DispatchQueue.main.async {
                  // 请求成功
                  if let obj = obj, let self = self {
                    self.currentGame = NEGame(gameInfo: obj)
                    let gameParams = NELoadGameParams()
                    gameParams.gameView = self.gameDetailView
                    gameParams.gameId = self.currentGame?.gameId
                    switch obj.gameStatus {
                    case .idle:
                      // 空闲
                      NEGameLog.infoLog(tag, desc: "当前空闲")
                      self.setCurrentGameProcess(0)
                    case .ready:
                      // 空闲
                      NEGameLog.infoLog(tag, desc: "当前准备中")
                      NEGameKit.getInstance().loadGame(gameParams, callback: { [weak self] code, msg, obj in
                        guard let self = self else {
                          return
                        }
                        if code == 0 {
                          self.setCurrentGameProcess(1)
                        } else {
                          DispatchQueue.main.async {
                            self.showToastInWindow(NEGameUIBundle.localized("加载游戏失败,请稍后再试"))
                          }
                        }
                      })
                    case .playing:
                      // 空闲
                      print("当前游戏中")
                      NEGameKit.getInstance().loadGame(gameParams, callback: { [weak self] code, msg, obj in
                        guard let self = self else {
                          return
                        }
                        if code == 0 {
                          self.setCurrentGameProcess(2)
                        } else {
                          DispatchQueue.main.async {
                            self.showToastInWindow(NEGameUIBundle.localized("加载游戏失败,请稍后再试"))
                          }
                        }
                      })
                    default: break
                    }
                  } else {
                    // 无数据
                    self?.currentGame = nil
                  }
                }

              } else if code == 3000 {
                // 空闲
                DispatchQueue.main.async {
                  if self?.currentGame != nil {
                    self?.currentGame = nil
                    // 保底 销毁youxiang
                    NEGameKit.getInstance().unloadGame()
                  }
                  self?.gameContentView.resetSubViews()
                  self?.setCurrentGameProcess(0)
                }
              } else {
                // 请求失败
                DispatchQueue.main.async {
                  self?.showToastInWindow(NEGameUIBundle.localized("获取房间游戏信息失败"))
                }
              }
            }
          } else {
            self.setCurrentGameProcess(0)
          }

        } else {
          // 初始化失败
        }
      }
    } else {
      NEGameKit.getInstance().getRoomGameInfo { [weak self] code, msg, obj in
        if code == 0 {
          DispatchQueue.main.async {
            // 请求成功
            if let obj = obj, let self = self {
              self.currentGame = NEGame(gameInfo: obj)
              let gameParams = NELoadGameParams()
              gameParams.gameView = self.gameDetailView
              gameParams.gameId = self.currentGame?.gameId
              switch obj.gameStatus {
              case .idle:
                // 空闲
                DispatchQueue.main.async {
                  if self.currentGame != nil {
                    self.currentGame = nil
                    // 保底 销毁youxiang
                    NEGameKit.getInstance().unloadGame()
                  }
                  self.gameContentView.resetSubViews()
                  self.setCurrentGameProcess(0)
                }
              case .ready:
                // 空闲
                print("当前准备中")
                NEGameKit.getInstance().loadGame(gameParams, callback: { code, msg, obj in
                  if code == 0 {
                    // 刷新UI
                    self.setCurrentGameProcess(1)
                  } else {
                    DispatchQueue.main.async {
                      self.showToastInWindow(NEGameUIBundle.localized("加载游戏失败,请稍后再试"))
                    }
                  }
                })
              case .playing:
                // 空闲
                print("当前游戏中")
                NEGameKit.getInstance().loadGame(gameParams, callback: { code, msg, obj in
                  if code == 0 {
                    // 刷新UI
                    self.setCurrentGameProcess(2)
                  } else {
                    DispatchQueue.main.async {
                      self.showToastInWindow(NEGameUIBundle.localized("加载游戏失败,请稍后再试"))
                    }
                  }
                })

              default: break
              }
            } else {
              // 无数据
              DispatchQueue.main.async {
                self?.currentGame = nil
                // 保底 销毁youxiang
                NEGameKit.getInstance().unloadGame()
                self?.gameContentView.resetSubViews()
                self?.setCurrentGameProcess(0)
              }
            }
          }
        } else if code == 3000 {
          // 空闲
          DispatchQueue.main.async {
            if self?.currentGame != nil {
              self?.currentGame = nil
              // 保底 销毁游戏
              NEGameKit.getInstance().unloadGame()
            }
            self?.gameContentView.resetSubViews()
            self?.setCurrentGameProcess(0)
          }
        } else {
          // 请求失败
          DispatchQueue.main.async {
            self?.showToastInWindow(NEGameUIBundle.localized("获取房间游戏信息失败"))
          }
        }
      }
    }
  }

  // 展示未在麦上视图
  func showNotOnSeatView() {
    notOnSeatView.removeFromSuperview()
    view.addSubview(notOnSeatView)
  }

  // 展示关闭游戏警告框
  func showEndGameView() {
    DispatchQueue.main.async {
      self.endGameAlertView.removeFromSuperview()
      self.view.addSubview(self.endGameAlertView)
    }
  }

  func loadSubViews() {
    seatsExView.isHidden = true
    // 游戏前视图
    view.addSubview(gameContentView)
    gameContentView.snp.makeConstraints { make in
      make.height.lessThanOrEqualTo(350)
      make.bottom.lessThanOrEqualTo(chatroomView.snp.top).offset(-16)
      make.left.right.equalTo(seatsView)
      make.top.equalTo(seatsExView.snp.bottom).offset(10)
    }
    gameContentView.isHidden = true

    // 游戏内容视图
    view.insertSubview(gameDetailView, belowSubview: headerView)
    gameDetailView.snp.makeConstraints { make in
      make.edges.equalTo(self.view)
    }
    gameDetailView.isHidden = true

    if isOwner {
      // 房主
      // 选择游戏按钮
      view.addSubview(chooseGameButton)
      chooseGameButton.snp.makeConstraints { make in
        make.centerX.equalTo(view)
        make.height.equalTo(40)
        make.width.equalTo(130)
        make.top.equalTo(seatsView.snp.bottom)
      }
      chooseGameButton.isHidden = true
    }
  }

  deinit {
    // 释放
    NEGameLog.infoLog(tag, desc: "deinit")
    NEGameKit.getInstance().removeGameListener(self)
    NEGameKit.getInstance().destroy()
    NEGameUIManager.instance.gameLeaveRoomEvent?()
  }

  // MARK: NEGameListener

  public func onGetGameViewInfo(_ handle: ISudFSMStateHandle, dataJson: String) {
    // 屏幕缩放比例，游戏内部采用px，需要开发者获取本设备比值 x 屏幕点数来获得真实px值设置相关字段中
    let scale = UIScreen.main.nativeScale
    // 游戏GameView尺寸
    let viewSize = gameDetailView.frame

    let m = GameViewInfoModel()
    // 游戏展示区域
    m.view_size.width = viewSize.width * scale
    m.view_size.height = viewSize.height * scale
    // 游戏画板设计上正方形显示中间，顶，底间距等值如下计算
    let gameViewMargin = (viewSize.height - viewSize.width) / 2.0
    // 顶部间距
    let top = gameContentView.origin.y + 25
    m.view_game_rect.top = top * scale
    // 左边
    m.view_game_rect.left = 20 * scale
    // 右边
    m.view_game_rect.right = 20 * scale
    // 底部安全区域
    let bottom = gameViewMargin * 2 - top
    m.view_game_rect.bottom = bottom * scale

    m.ret_code = 0

    m.ret_msg = "success"

    handle.success(m.toJSON() ?? "")
  }

  public func onGameCreated(event: NEGameCreatedEvent) {
    NEGameLog.infoLog(tag, desc: "onGameCreated")
    currentGame = NEGame(event: event)
    let gameParams = NELoadGameParams()
    gameParams.gameView = gameDetailView
    gameParams.gameId = event.gameId
    NEGameKit.getInstance().loadGame(gameParams, callback: { [weak self] code, msg, obj in
      if code == 0 {
        self?.setCurrentGameProcess(1)
      } else {
        DispatchQueue.main.async {
          self?.showToastInWindow(NEGameUIBundle.localized("加载游戏失败,请稍后再试"))
        }
      }

    })
  }

  public func onGameStarted(event: NEGameStartedEvent) {
    // 游戏正常开始
    setCurrentGameProcess(2)
  }

  public func onMemberJoinGame(members: [NERoomMember]) {
    refreshSeatStatus()
  }

  public func onMemberLeaveGame(members: [NERoomMember]) {
    refreshSeatStatus()
  }

  // 更新麦位上已准备，准备的状态
  func refreshSeatStatus() {
    let temp = defaultAudienceSeats + [defaultOwnerSeat]
    temp.forEach { model in
      if let uuid = model.uuid, uuid.count > 0 {
        // 存在麦位数据
        if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .playing {
          model.ext = "游戏中"
          model.propertyChanged.forEach { block in
            block()
          }
        } else if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .ready {
          model.ext = "已准备"
          model.propertyChanged.forEach { block in
            block()
          }
        } else {
          // 本地不存在准备数据
          model.ext = ""
          model.propertyChanged.forEach { block in
            block()
          }
        }
      } else {
        // 麦位上没人
        model.ext = ""
        model.propertyChanged.forEach { block in
          block()
        }
      }
    }
    refreshGameContentViewState()
  }

  func refreshGameContentViewState() {
    if let uuid = NEGameKit.getInstance().localMember?.uuid {
      if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .playing {
        if isOwner {
          gameContentView.setAnchorEnterGameEventState(0)
        } else {
          gameContentView.setAudienceEventState(0)
        }
      } else if NEGameKit.getInstance().getMemberState(userUuid: uuid) == .ready {
        if isOwner {
          gameContentView.setAnchorEnterGameEventState(1)
        } else {
          gameContentView.setAudienceEventState(1)
        }
      } else {
        if isOwner {
          gameContentView.setAnchorEnterGameEventState(0)
        } else {
          gameContentView.setAudienceEventState(0)
        }
      }
    }
  }

  public func onGameEnded(event: NEGameEndedEvent) {
    currentGame = nil
    setCurrentGameProcess(0)
    DispatchQueue.main.async {
      self.gameContentView.resetSubViews()
    }
  }

  public func onGameSelfClickGameSettleClose() {
    setCurrentGameProcess(0)

    if isOwner {
      // 把所有人踢下去
      let createGameParams = NECreateGameParams()
      createGameParams.gameId = currentGame?.gameId ?? ""
      NEGameKit.getInstance().createGame(createGameParams) { [weak self] code, msg, obj in
        guard let self = self else {
          NEGameLog.infoLog(tag, desc: "self if release")
          return
        }
        if code == 0 {
          // 创建成功
          self.setCurrentGameProcess(1)
        } else {
          // 创建失败
          DispatchQueue.main.async {
            self.showToastInWindow(NEGameUIBundle.localized("创建游戏失败,请稍后再试"))
          }
        }
      }
    }
  }

  func setCurrentGameProcess(_ state: Int) {
    DispatchQueue.main.async {
      self.refreshSeatStatus()
      switch state {
      case 0:
        self.gameContentView.resetSubViews()
        // 默认状态，未选则游戏，界面上选择游戏按钮
        self.gameContentView.isHidden = true
        self.gameDetailView.isHidden = true
        self.chooseGameButton.isHidden = false
        self.seatsView.isHidden = false
        self.seatsExView.isHidden = true
        self.headerView.leaveGameBtn.isHidden = true
      case 1:
        // 房主已经创建好游戏
        self.gameContentView.updateLabel(self.gameContentView.roleLabel, text: self.currentGame?.rule ?? "")
        self.gameContentView.titleLabel.text = self.currentGame?.gameName ?? ""
        self.gameContentView.isHidden = false
        self.gameDetailView.isHidden = true
        self.chooseGameButton.isHidden = true
        self.seatsExView.isHidden = false
        self.seatsView.isHidden = true
        self.headerView.leaveGameBtn.isHidden = true

      case 2:
        // 游戏开始
        self.gameContentView.isHidden = true
        self.gameDetailView.isHidden = false
        self.chooseGameButton.isHidden = true
        self.seatsExView.isHidden = false
        self.seatsView.isHidden = true
        if self.isOwner {
          self.headerView.leaveGameBtn.isHidden = false
        }
      default: break
      }
    }
  }
}

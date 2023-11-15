// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import NEVoiceRoomBaseUIKit
import NEVoiceRoomKit
import NEUIKit

public class NEGameRoomListViewController: NEVRBaseRoomListViewController {
  override public func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    navigationController?.setNavigationBarHidden(false, animated: true)
  }

  @objc override public func createRoom() {
    let view = NEVRBaseCreateViewController()
//    view.ne_UINavigationItem.navigationBarHidden = true
    view.createAction = { [weak self] name, image, button in
      NotificationCenter.default.post(name: NSNotification.Name("gameStartLive"), object: nil)
      let params = NECreateVoiceRoomParams()
      params.liveTopic = name
      params.seatCount = 9
      params.cover = image
      params.liveType = .game
      params.configId = NEGameUIManager.instance.configId
      params.seatApplyMode = .free
      if let busy = NEGameUIManager.instance.checkIfInOtherRoom?() as? Bool,
         busy {
        let alert = UIAlertController(title: NEGameUIBundle.localized("提示"), message: NEGameUIBundle.localized("是否退出当前房间，并创建新房间"), preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("取消"), style: .cancel, handler: { _ in
          DispatchQueue.main.async {
            button.isEnabled = true
          }
        }))
        alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("确定"), style: .default, handler: { _ in
          NEGameUIManager.instance.leaveOtherRoom?({
            self?.createAction(params: params, button: button)
          })
        }))
        self?.present(alert, animated: true)
      } else {
        self?.createAction(params: params, button: button)
      }
    }
    navigationController?.pushViewController(view, animated: true)
  }

  override public func pushToRoomViewController(_ roomInfoModel: NEVoiceRoomInfo, isHost: Bool) {
    if let busy = NEGameUIManager.instance.checkIfInOtherRoom?() as? Bool,
       busy {
      let alert = UIAlertController(title: NEGameUIBundle.localized("提示"), message: NEGameUIBundle.localized("是否退出当前房间进入其他房间"), preferredStyle: .alert)
      alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("取消"), style: .cancel))
      alert.addAction(UIAlertAction(title: NEGameUIBundle.localized("确定"), style: .default, handler: { _ in
        NEGameUIManager.instance.leaveOtherRoom?({ [weak self] in
          DispatchQueue.main.async {
            self?.enterRoom(roomInfoModel, isHost: isHost)
          }
        })
      }))
      present(alert, animated: true)
    } else {
      enterRoom(roomInfoModel, isHost: isHost)
    }
  }

  func enterRoom(_ roomInfoModel: NEVoiceRoomInfo, isHost: Bool) {
    if !isHost {
      NotificationCenter.default.post(name: NSNotification.Name("gameEnter"), object: nil)
    }
    let params = NEVRBaseViewControllerParams()
    params.roomUuid = roomInfoModel.liveModel?.roomUuid ?? ""
    params.roomName = roomInfoModel.liveModel?.liveTopic ?? ""
    params.role = isHost ? .host : .audience
    params.liveRecordId = roomInfoModel.liveModel?.liveRecordId ?? -1
    params.nick = NEGameUIManager.instance.nickname ?? ""
    params.ownerIcon = roomInfoModel.anchor?.icon ?? ""
    params.ownerName = roomInfoModel.anchor?.userName ?? ""
    params.ownerUuid = roomInfoModel.anchor?.userUuid ?? ""
    params.cover = roomInfoModel.liveModel?.cover ?? ""
    let vc = NEGameRoomViewController(params: params)
    navigationController?.pushViewController(vc, animated: true)
  }
}

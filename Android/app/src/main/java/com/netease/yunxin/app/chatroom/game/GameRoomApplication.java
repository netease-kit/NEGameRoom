// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game;

import android.app.Application;
import com.netease.yunxin.app.chatroom.game.config.AppConfig;
import com.netease.yunxin.app.chatroom.game.ui.utils.GameUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;
import com.netease.yunxin.kit.entertainment.common.utils.IconFontUtil;
import com.netease.yunxin.kit.voiceroomkit.ui.base.NEVoiceRoomUI;

public class GameRoomApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    ALog.init(this, ALog.LEVEL_ALL);
    AppConfig.init(this);
    AppStatusManager.init(this);
    initGameRoomUI();
    IconFontUtil.getInstance().init(this);
    GameUtil.setBaseUrl(AppConfig.getBaseUrl());
    GameUtil.setAppKey(AppConfig.getAppKey());
  }

  private void initGameRoomUI() {
    NEVoiceRoomUI.getInstance().init(this);
  }
}

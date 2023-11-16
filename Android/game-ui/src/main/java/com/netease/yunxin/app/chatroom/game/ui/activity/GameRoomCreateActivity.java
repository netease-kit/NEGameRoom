// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.chatroom.game.ui.R;
import com.netease.yunxin.app.chatroom.game.ui.utils.NavUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.voiceroomkit.api.NELiveType;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomSeatApplyMode;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomInfo;
import com.netease.yunxin.kit.voiceroomkit.ui.base.activity.MultiCreateRoomActivity;

public class GameRoomCreateActivity extends MultiCreateRoomActivity {
  private static final String TAG = "GameRoomCreateActivity";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onCreateSuccess(NEVoiceRoomInfo roomInfo) {
    NavUtils.toGameAnchorPage(GameRoomCreateActivity.this, isOversea, username, avatar, roomInfo);
    finish();
  }

  @Override
  protected void onCreateFailed(int code, String msg) {
    if (code == 2001) {
      NavUtils.toAuthenticateActivity(GameRoomCreateActivity.this);
    } else {
      ToastX.showShortToast(getString(R.string.ec_join_failed_tips));
    }
  }

  @Override
  protected int getLiveType() {
    return NELiveType.LIVE_TYPE_GAME;
  }

  @Override
  protected int getSeatMode() {
    return NEVoiceRoomSeatApplyMode.free;
  }
}

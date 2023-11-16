// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.app.chatroom.game.activity.CommonSettingActivity;
import com.netease.yunxin.app.chatroom.game.config.AppConfig;
import com.netease.yunxin.app.chatroom.game.ui.activity.GameRoomListActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.R;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;

public class GameNavUtils {

  public static void toCommonSettingPage(Context context) {
    Intent intent = new Intent(context, CommonSettingActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  public static void toGameRoomListPage(Context context) {
    if (!NetworkUtils.isConnected()) {
      ToastUtils.INSTANCE.showShortToast(context, context.getString(R.string.network_error));
      return;
    }
    Intent intent = new Intent(context, GameRoomListActivity.class);
    intent.putExtra(RoomConstants.INTENT_IS_OVERSEA, AppConfig.isOversea());
    intent.putExtra(RoomConstants.INTENT_KEY_CONFIG_ID, AppConfig.getVoiceRoomConfigId());
    intent.putExtra(RoomConstants.INTENT_USER_NAME, AppUtils.getUserName());
    intent.putExtra(RoomConstants.INTENT_AVATAR, AppUtils.getAvatar());
    context.startActivity(intent);
  }
}

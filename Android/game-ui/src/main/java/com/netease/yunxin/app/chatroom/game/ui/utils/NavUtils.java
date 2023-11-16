// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.utils;

import android.content.Context;
import android.content.Intent;
import com.netease.yunxin.app.chatroom.game.ui.activity.GameAnchorActivity;
import com.netease.yunxin.app.chatroom.game.ui.activity.GameAudienceActivity;
import com.netease.yunxin.kit.entertainment.common.RoomConstants;
import com.netease.yunxin.kit.entertainment.common.activity.AuthenticateActivity;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomInfo;

public class NavUtils {

  private static final String TAG = "NavUtil";

  public static void toGameAnchorPage(
      Context context,
      boolean isOverSea,
      String username,
      String avatar,
      NEVoiceRoomInfo roomInfo) {
    RoomModel roomModel = new RoomModel();
    roomModel.setLiveRecordId(roomInfo.getLiveModel().getLiveRecordId());
    roomModel.setRoomUuid(roomInfo.getLiveModel().getRoomUuid());
    roomModel.setRole(RoomConstants.ROLE_HOST);
    roomModel.setRoomName(roomInfo.getLiveModel().getLiveTopic());
    roomModel.setNick(username);
    roomModel.setAvatar(avatar);
    roomModel.setAnchorAvatar(roomInfo.getAnchor().getAvatar());
    roomModel.setAnchorNick(roomInfo.getAnchor().getNick());
    roomModel.setCover(roomInfo.getLiveModel().getCover());
    Intent intent = new Intent(context, GameAnchorActivity.class);
    intent.putExtra(RoomConstants.INTENT_ROOM_MODEL, roomModel);
    context.startActivity(intent);
  }

  public static void toGameAudiencePage(
      Context context, String username, String avatar, RoomModel roomInfo, boolean needJoinRoom) {
    RoomModel roomModel = new RoomModel();
    roomModel.setLiveRecordId(roomInfo.getLiveRecordId());
    roomModel.setRoomUuid(roomInfo.getRoomUuid());
    roomModel.setRole(RoomConstants.ROLE_AUDIENCE);
    roomModel.setRoomName(roomInfo.getRoomName());
    roomModel.setNick(username);
    roomModel.setAvatar(avatar);
    roomModel.setAnchorAvatar(roomInfo.getAnchorAvatar());
    roomModel.setAnchorNick(roomInfo.getAnchorNick());
    roomModel.setCover(roomInfo.getCover());
    Intent intent = new Intent(context, GameAudienceActivity.class);
    intent.putExtra(RoomConstants.INTENT_ROOM_MODEL, roomModel);
    context.startActivity(intent);
  }

  public static void toAuthenticateActivity(Context context) {
    Intent intent = new Intent(context, AuthenticateActivity.class);
    context.startActivity(intent);
  }
}

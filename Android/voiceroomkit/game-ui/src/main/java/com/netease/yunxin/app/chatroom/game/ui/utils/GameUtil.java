// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.utils;

public class GameUtil {
  private static String appKey;
  private static String baseUrl;
  private static String roomUuid;

  public static void setAppKey(String appKey) {
    GameUtil.appKey = appKey;
  }

  public static String getAppKey() {
    return appKey;
  }

  public static void setRoomUuid(String roomUuid) {
    GameUtil.roomUuid = roomUuid;
  }

  public static String getRoomUuid() {
    return roomUuid;
  }

  public static void setBaseUrl(String baseUrl) {
    GameUtil.baseUrl = baseUrl;
  }

  public static String getBaseUrl() {
    return baseUrl;
  }
}

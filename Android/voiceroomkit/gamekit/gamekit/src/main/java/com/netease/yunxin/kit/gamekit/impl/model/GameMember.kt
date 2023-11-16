/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.impl.model

data class GameMember(
    var gameId: String,
    var roomUuid: String,
    var gameName: String,
    var appKey: String,
    var userUuid: String,
    var status: Int, // 用户状态：0 准备中 1游戏中 2 退出
    var userName: String,
    var liveRecordId: Long,
    var roomArchiveId: String
) {
    constructor(userUuid: String) : this("", "", "", "", userUuid, -1, "", 0, "")
}

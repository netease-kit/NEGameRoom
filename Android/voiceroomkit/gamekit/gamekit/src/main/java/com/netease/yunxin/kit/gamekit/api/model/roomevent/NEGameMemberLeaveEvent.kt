/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model.roomevent

data class NEGameMemberLeaveEvent(
    val id: Int,
    val appKey: String,
    val liveRecordId: Int,
    val roomUuid: String,
    val status: Int,
    val joinTime: Long,
    val gameId: String,
    val userUuid: String,
    val userName: String,
    val roomArchiveId: String
)
data class NEMemberLeaveModel(
    val data: NEGameMemberLeaveEvent
)

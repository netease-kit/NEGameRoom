/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model.roomevent

data class NEGameStartedEvent(
    val id: Int,
    val appKey: String,
    val liveRecordId: Int,
    val roomUuid: String,
    val gameCreator: String,
    val gameStatus: Int,
    val gameId: String,
    val gameName: String,
    val gameDesc: String,
    val thumbnail: String,
    val roomArchiveId: String
)
data class NEGameStartedModel(
    val data: NEGameStartedEvent
)

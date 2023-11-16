/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

data class NEJoinGameResponse(
    val gameId: Long,
    val gameName: String?,
    val roomUuid: String?,
    val liveRecordId: Long,
    val appKey: String,
    val roomArchiveId: String,
    val userUuid: String,
    val userName: String,
    val status: Int
)

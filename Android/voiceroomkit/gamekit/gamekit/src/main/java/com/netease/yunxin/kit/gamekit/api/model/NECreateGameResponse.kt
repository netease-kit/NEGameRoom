/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

data class NECreateGameResponse(
    val gameId: Long,
    val gameName: String?,
    val gameDesc: String?,
    val thumbnail: String?,
    val roomUuid: String?,
    val liveRecordId: Long,
    val appKey: String,
    val roomArchiveId: String,
    val gameStatus: Int
)

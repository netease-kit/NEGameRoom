/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model

data class NERoomGameInfo(
    val appKey: String,
    val liveRecordId: String,
    val roomUuid: String,
    val gameCreator: String,
    val gameStatus: Int,
    val gameId: String,
    val gameName: String,
    val rule: String,
    val gameDesc: String,
    val thumbnail: String,
    val roomArchiveId: String
)

/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.gamekit.api.model.event

import com.netease.yunxin.kit.gamekit.api.model.NEGameChannel
import com.netease.yunxin.kit.roomkit.api.NERoomMember
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState
import tech.sud.mgp.core.ISudFSMStateHandle

data class GameMemberPaintingEvent(
    val channel: NEGameChannel,
    val member: NERoomMember,
    val handle: ISudFSMStateHandle?,
    val message: SudMGPMGState.MGDGPainting
)

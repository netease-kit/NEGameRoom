/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.app.chatroom.game.ui.utils

import android.content.Context
import android.util.Log
import com.netease.yunxin.kit.corekit.XKitLog
import com.netease.yunxin.kit.corekit.XKitLogOptions

object GameUILog {
    private const val MODULE = "GameUI"

    @JvmStatic
    fun init(applicationContext: Context, options: XKitLogOptions) {
        XKitLog.init(applicationContext, options)
    }

    @JvmStatic
    fun i(tag: String, log: String) {
        XKitLog.i(tag, log, MODULE)
    }

    @JvmStatic
    fun w(tag: String, log: String) {
        XKitLog.w(tag, log, MODULE)
    }

    @JvmStatic
    fun d(tag: String, log: String) {
        XKitLog.d(tag, log, MODULE)
    }

    @JvmStatic
    fun e(tag: String, log: String) {
        XKitLog.e(tag, log, MODULE)
    }

    @JvmStatic
    fun e(tag: String, log: String, throwable: Throwable?) {
        XKitLog.e(tag, "$log \n ${Log.getStackTraceString(throwable)}", MODULE)
    }

    @JvmStatic
    fun api(tag: String, log: String) {
        XKitLog.api(tag, log, MODULE)
    }
}

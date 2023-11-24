// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.chatroom.game.ui.R;

public class SelectGameButton extends LinearLayout {
  public SelectGameButton(Context context) {
    super(context);
    init(context);
  }

  public SelectGameButton(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public SelectGameButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    inflate(context, R.layout.game_select_game_button, this);
  }
}

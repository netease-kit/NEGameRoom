// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.chatroom.game.ui.R;

/** 游戏规则 */
public class GameRuleView extends LinearLayout {
  private TextView tvRule;

  public GameRuleView(Context context) {
    super(context);
    init(context);
  }

  public GameRuleView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public GameRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    inflate(context, R.layout.game_game_rule, this);
    tvRule = findViewById(R.id.tv_rule);
  }

  public void setRule(String rule) {
    tvRule.setText(rule);
  }
}

// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.yunxin.app.chatroom.game.ui.R;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.gamekit.api.NEGameKit;
import com.netease.yunxin.kit.gamekit.api.model.NEMemberState;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomCallback;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomKit;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomListenerAdapter;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomSeatInfo;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomSeatItem;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomSeatItemStatus;
import java.util.List;

/** 游戏开始前自定义按钮 1.主播按钮：参与游戏、退出、人数已满、开始游戏 2.观众按钮：参与游戏、退出、人数已满 */
public class GameCustomButtons extends ConstraintLayout {
  private TextView btnAnchorLeft;
  private TextView btnAnchorRight;
  private TextView btnAudience;
  private ButtonType type;
  private AnchorButtonClickListener anchorButtonClickListener;
  private AudienceButtonClickListener audienceButtonClickListener;
  private final NEVoiceRoomListenerAdapter roomListener =
      new NEVoiceRoomListenerAdapter() {
        @Override
        public void onSeatKicked(
            int seatIndex, @NonNull String account, @NonNull String operateBy) {
          super.onSeatKicked(seatIndex, account, operateBy);
          resetAudienceButtonUI();
        }

        @Override
        public void onSeatLeave(int seatIndex, @NonNull String account) {
          super.onSeatLeave(seatIndex, account);
          resetAudienceButtonUI();
        }
      };

  private void resetAudienceButtonUI() {
    btnAudience.setText(R.string.game_join_game_text);
    btnAudience.setTextColor(Color.WHITE);
    btnAudience.setBackgroundResource(R.drawable.game_bg_blue_bttton_join_game);
  }

  public GameCustomButtons(Context context) {
    super(context);
    init(context);
  }

  public GameCustomButtons(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public GameCustomButtons(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    NEVoiceRoomKit.getInstance().removeVoiceRoomListener(roomListener);
  }

  private void init(Context context) {
    inflate(context, R.layout.game_game_buttons, this);
    btnAnchorLeft = findViewById(R.id.btn_anchor_left);
    btnAnchorRight = findViewById(R.id.btn_anchor_right);
    btnAudience = findViewById(R.id.btn_audience);
    btnAnchorLeft.setOnClickListener(
        v -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          if (anchorButtonClickListener == null) {
            return;
          }
          String btnText = btnAnchorLeft.getText().toString();
          if (getContext().getString(R.string.game_join_game_text).equals(btnText)) {
            anchorButtonClickListener.onJoinGameClick();
          } else if (getContext().getString(R.string.game_leave_game_text).equals(btnText)) {
            anchorButtonClickListener.onLeaveGameClick();
          }
        });
    btnAnchorRight.setOnClickListener(
        v -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          if (anchorButtonClickListener == null) {
            return;
          }
          anchorButtonClickListener.onStartGameClick();
        });
    btnAudience.setOnClickListener(
        v -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          if (audienceButtonClickListener == null) {
            return;
          }
          String btnText = btnAudience.getText().toString();
          if (getContext().getString(R.string.game_join_game_text).equals(btnText)) {
            audienceButtonClickListener.onJoinGameClick();
          } else if (getContext().getString(R.string.game_leave_game_text).equals(btnText)) {
            audienceButtonClickListener.onLeaveGameClick();
          }
        });

    setDefaultStyle();
    NEVoiceRoomKit.getInstance().addVoiceRoomListener(roomListener);
  }

  private void setDefaultStyle() {
    btnAnchorLeft.setText(getContext().getString(R.string.game_join_game_text));
    btnAnchorLeft.setTextColor(Color.WHITE);
    btnAnchorLeft.setBackgroundResource(R.drawable.game_bg_blue_bttton_join_game);
    btnAnchorRight.setBackgroundResource(R.drawable.game_bg_red_bttton_start_game);
    btnAudience.setText(getContext().getString(R.string.game_join_game_text));
    btnAudience.setTextColor(Color.WHITE);
    btnAudience.setBackgroundResource(R.drawable.game_bg_blue_bttton_join_game);
  }

  public void setType(ButtonType type) {
    this.type = type;
    if (type == ButtonType.ANCHOR) {
      btnAnchorLeft.setVisibility(VISIBLE);
      btnAnchorRight.setVisibility(VISIBLE);
      btnAudience.setVisibility(GONE);
    } else if (type == ButtonType.AUDIENCE) {
      btnAnchorLeft.setVisibility(GONE);
      btnAnchorRight.setVisibility(GONE);
      btnAudience.setVisibility(VISIBLE);
    }
  }

  public void refreshGameButtonUI() {
    if (type == ButtonType.ANCHOR) {
      if (getLocalMemberState() == NEMemberState.IDLE) {
        btnAnchorLeft.setText(getContext().getString(R.string.game_join_game_text));
        btnAnchorLeft.setTextColor(Color.WHITE);
        btnAnchorLeft.setBackgroundResource(R.drawable.game_bg_blue_bttton_join_game);
      } else if (getLocalMemberState() == NEMemberState.PREPARING) {
        btnAnchorLeft.setText(getContext().getString(R.string.game_leave_game_text));
        btnAnchorLeft.setTextColor(Color.parseColor("#333333"));
        btnAnchorLeft.setBackgroundResource(R.drawable.game_bg_gray_bttton);
      }
    } else if (type == ButtonType.AUDIENCE) {
      if (getLocalMemberState() == NEMemberState.IDLE) {
        resetAudienceButtonUI();
      } else if (getLocalMemberState() == NEMemberState.PREPARING) {
        // 重新查询一把当前最新麦位状态，确保不在麦上不会显示退出按钮
        NEVoiceRoomKit.getInstance()
            .getSeatInfo(
                new NEVoiceRoomCallback<NEVoiceRoomSeatInfo>() {
                  @Override
                  public void onSuccess(@Nullable NEVoiceRoomSeatInfo neVoiceRoomSeatInfo) {
                    List<NEVoiceRoomSeatItem> seatItems = neVoiceRoomSeatInfo.getSeatItems();
                    if (seatItems == null || seatItems.isEmpty()) {
                      return;
                    }
                    for (NEVoiceRoomSeatItem seatItem : seatItems) {
                      if (seatItem.getUser().equals(UserInfoManager.getSelfUserUuid())) {
                        if (seatItem.getStatus() == NEVoiceRoomSeatItemStatus.TAKEN) {
                          btnAudience.setText(R.string.game_leave_game_text);
                          btnAudience.setTextColor(Color.parseColor("#333333"));
                          btnAudience.setBackgroundResource(R.drawable.game_bg_gray_bttton);
                        } else {
                          resetAudienceButtonUI();
                          NEGameKit.getInstance().leaveGame(null);
                        }
                        break;
                      }
                    }
                  }

                  @Override
                  public void onFailure(int code, @Nullable String msg) {}
                });
      }
    }
  }

  private NEMemberState getLocalMemberState() {
    return NEGameKit.getInstance().getMemberState(UserInfoManager.getSelfUserUuid());
  }

  public void setAnchorButtonClickListener(AnchorButtonClickListener anchorButtonClickListener) {
    this.anchorButtonClickListener = anchorButtonClickListener;
  }

  public void setAudienceButtonClickListener(
      AudienceButtonClickListener audienceButtonClickListener) {
    this.audienceButtonClickListener = audienceButtonClickListener;
  }

  public enum ButtonType {
    ANCHOR,
    AUDIENCE
  }

  public interface AnchorButtonClickListener {
    void onJoinGameClick();

    void onLeaveGameClick();

    void onStartGameClick();
  }

  public interface AudienceButtonClickListener {
    void onJoinGameClick();

    void onLeaveGameClick();
  }
}

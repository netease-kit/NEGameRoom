// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.yunxin.app.chatroom.game.ui.R;
import com.netease.yunxin.app.chatroom.game.ui.constant.GameUIConstant;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.dialog.ConfirmDialog;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.gamekit.api.NEGameCallback2;
import com.netease.yunxin.kit.gamekit.api.NEGameKit;
import com.netease.yunxin.kit.gamekit.api.model.NEGame;
import com.netease.yunxin.kit.gamekit.api.model.NEJoinGameParams;
import com.netease.yunxin.kit.gamekit.api.model.NEStartGameParams;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomCallback;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomKit;
import kotlin.Unit;

/** 游戏视图容器 */
public class GameContainerView extends ConstraintLayout {
  private TextView tvGameTitle;
  private TextView tvGameClose;
  private GameRuleView gameRuleView;
  private GameCustomButtons gameCustomButtons;
  private GameContainerClickListener clickListener;
  private NEGame gameInfo;
  private static final int ALREADY_ON_SEAT = 1036;
  private static final int PEOPLE_INSUFFICIENT = 403;

  public GameContainerView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public GameContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public GameContainerView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    inflate(context, R.layout.game_game_container, this);
    tvGameTitle = findViewById(R.id.tv_game_title);
    gameRuleView = findViewById(R.id.game_rule_view);
    gameCustomButtons = findViewById(R.id.game_custom_buttons);
    tvGameClose = findViewById(R.id.tv_game_close);
    tvGameClose.setOnClickListener(
        v -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          if (clickListener != null) {
            clickListener.onCloseClick(v);
          }
        });
    gameCustomButtons.setAnchorButtonClickListener(
        new GameCustomButtons.AnchorButtonClickListener() {
          @Override
          public void onJoinGameClick() {
            joinGame();
          }

          @Override
          public void onLeaveGameClick() {
            leaveGame();
          }

          @Override
          public void onStartGameClick() {
            startGame();
          }
        });

    gameCustomButtons.setAudienceButtonClickListener(
        new GameCustomButtons.AudienceButtonClickListener() {
          @Override
          public void onJoinGameClick() {
            joinGame();
          }

          @Override
          public void onLeaveGameClick() {
            leaveGame();
          }
        });
  }

  private void startGame() {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(
          getContext()
              .getString(
                  com.netease.yunxin.kit.entertainment.common.R.string.common_network_error));
      return;
    }
    NEGameKit.getInstance()
        .startGame(
            new NEStartGameParams(gameInfo.getGameId()),
            new NEGameCallback2<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit data) {
                super.onSuccess(data);
              }

              @Override
              public void onError(int code, @Nullable String message) {
                super.onError(code, message);
                if (code == PEOPLE_INSUFFICIENT) {
                  ToastX.showShortToast(getContext().getString(R.string.game_people_insufficient));
                } else {
                  ToastX.showShortToast(getContext().getString(R.string.game_start_game_failed));
                }
              }
            });
  }

  private void leaveGame() {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(
          getContext()
              .getString(
                  com.netease.yunxin.kit.entertainment.common.R.string.common_network_error));
      return;
    }
    NEGameKit.getInstance()
        .leaveGame(
            new NEGameCallback2<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit data) {
                super.onSuccess(data);
              }

              @Override
              public void onError(int code, @Nullable String message) {
                super.onError(code, message);
                ToastX.showShortToast(getContext().getString(R.string.game_leave_game_failed));
              }
            });
  }

  private void joinGame() {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(
          getContext()
              .getString(
                  com.netease.yunxin.kit.entertainment.common.R.string.common_network_error));
      return;
    }
    NEGameKit.getInstance()
        .joinGame(
            new NEJoinGameParams(gameInfo.getGameId()),
            new NEGameCallback2<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit data) {
                super.onSuccess(data);
              }

              @Override
              public void onError(int code, @Nullable String message) {
                super.onError(code, message);
                if (code == GameUIConstant.NOT_ON_SEAT_ERROR_CODE) {
                  ConfirmDialog.Companion.show(
                      getContext(),
                      getContext().getString(R.string.voiceroom_tip),
                      getContext()
                          .getString(
                              com.netease
                                  .yunxin
                                  .app
                                  .chatroom
                                  .game
                                  .ui
                                  .R
                                  .string
                                  .game_confirm_submit_seat_request),
                      getContext()
                          .getString(com.netease.yunxin.kit.entertainment.common.R.string.ec_sure),
                      true,
                      false,
                      aBoolean -> {
                        if (aBoolean) {
                          submitSeatRequest();
                        }
                      });
                } else {
                  ToastX.showShortToast(getContext().getString(R.string.game_join_game_failed));
                }
              }
            });
  }

  private void submitSeatRequest() {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(
          getContext()
              .getString(
                  com.netease.yunxin.kit.entertainment.common.R.string.common_network_error));
      return;
    }
    NEVoiceRoomKit.getInstance()
        .submitSeatRequest(
            new NEVoiceRoomCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {}

              @Override
              public void onFailure(int code, @Nullable String msg) {
                if (code == ALREADY_ON_SEAT) {
                  // 已经在麦上
                  joinGame();
                } else {
                  ToastX.showShortToast(msg);
                }
              }
            });
  }

  public void setGameInfo(NEGame gameInfo) {
    this.gameInfo = gameInfo;
    tvGameTitle.setText(gameInfo.getGameName());
    gameRuleView.setRule(gameInfo.getRule());
  }

  public void setType(GameCustomButtons.ButtonType type) {
    gameCustomButtons.setType(type);
    if (type == GameCustomButtons.ButtonType.AUDIENCE) {
      tvGameClose.setVisibility(GONE);
    } else {
      tvGameClose.setVisibility(VISIBLE);
    }
  }

  public void setClickListener(GameContainerClickListener clickListener) {
    this.clickListener = clickListener;
  }

  public void showPreparingGameUI() {
    // 显示游戏规则、自定义按钮，不显示游戏的UI
    gameCustomButtons.setVisibility(VISIBLE);
    gameRuleView.setVisibility(VISIBLE);
    refreshGameButtonUI();
  }

  public void refreshGameButtonUI() {
    gameCustomButtons.refreshGameButtonUI();
  }

  public interface GameContainerClickListener {
    void onCloseClick(View view);
  }
}

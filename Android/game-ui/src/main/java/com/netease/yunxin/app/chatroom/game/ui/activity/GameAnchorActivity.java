// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.chatroom.game.ui.utils.GameUILog;
import com.netease.yunxin.app.chatroom.game.ui.widget.GameContainerView;
import com.netease.yunxin.app.chatroom.game.ui.widget.GameCustomButtons;
import com.netease.yunxin.app.chatroom.game.ui.widget.SelectGameButton;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.entertainment.common.dialog.ConfirmDialog;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomCallback;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomKit;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomMember;
import com.netease.yunxin.kit.voiceroomkit.ui.base.activity.VoiceRoomBaseActivity;
import com.netease.yunxin.kit.voiceroomkit.ui.base.dialog.ChatRoomMoreDialog;
import com.netease.yunxin.kit.voiceroomkit.ui.base.dialog.ChoiceDialog;
import com.netease.yunxin.kit.voiceroomkit.ui.base.viewmodel.AnchorVoiceRoomViewModel;
import com.netease.yunxin.kit.voiceroomkit.ui.base.viewmodel.VoiceRoomViewModel;
import java.util.Arrays;
import kotlin.Unit;

public class GameAnchorActivity extends GameBaseActivity {
  private final ActivityResultLauncher<Intent> launcher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
              Intent intent = result.getData();
              if (intent != null) {
                String gameId = intent.getStringExtra("gameId");
                ReportUtils.report(GameAnchorActivity.this, getPageName(), "game_start");
                gameViewModel.createGame(gameId);
              }
            }
          });

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleGameRoomView();
    AnchorVoiceRoomViewModel anchorVoiceRoomViewModel =
        new ViewModelProvider(this).get(AnchorVoiceRoomViewModel.class);
    anchorVoiceRoomViewModel.localMemberData.observe(
        this,
        new Observer<NEVoiceRoomMember>() {
          @Override
          public void onChanged(NEVoiceRoomMember neVoiceRoomMember) {
            // 加入房间成功
            handleGameEvent();
            gamePreparingView.setType(GameCustomButtons.ButtonType.ANCHOR);
            gamePreparingView.setClickListener(
                new GameContainerView.GameContainerClickListener() {
                  @Override
                  public void onCloseClick(View view) {
                    endGame();
                  }
                });
            observeGameEvent();
          }
        });
  }

  @Override
  protected VoiceRoomViewModel getRoomViewModel() {
    return new ViewModelProvider(this).get(AnchorVoiceRoomViewModel.class);
  }

  private void handleGameRoomView() {
    ivEndGame.setOnClickListener(
        v -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          endGame();
        });
    SelectGameButton selectGameButton = new SelectGameButton(this);
    selectGameButton.setOnClickListener(
        v -> {
          if (ClickUtils.isFastClick()) {
            return;
          }
          ReportUtils.report(GameAnchorActivity.this, getPageName(), "game_choose");
          launcher.launch(new Intent(GameAnchorActivity.this, SelectGameActivity.class));
        });
    FrameLayout.LayoutParams selectGameLayoutParams =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    selectGameLayoutParams.gravity = Gravity.CENTER;
    selectGameLayout.addView(selectGameButton, selectGameLayoutParams);
  }

  private void endGame() {
    ConfirmDialog.Companion.show(
        GameAnchorActivity.this,
        getString(com.netease.yunxin.kit.entertainment.common.R.string.ec_tips),
        getString(com.netease.yunxin.app.chatroom.game.ui.R.string.game_confirm_exit_game_content),
        getString(com.netease.yunxin.kit.entertainment.common.R.string.ec_sure),
        true,
        false,
        aBoolean -> {
          if (Boolean.TRUE.equals(aBoolean)) {
            gameViewModel.endGame(null);
          }
        });
  }

  @Override
  protected void doLeaveRoom() {
    new ChoiceDialog(this)
        .setTitle(
            getString(
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_end_live_title))
        .setContent(
            getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_end_live_tips))
        .setNegative(
            getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_cancel), null)
        .setPositive(
            getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_sure),
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                closeGameRoom();
              }
            })
        .show();
  }

  private void closeGameRoom() {
    GameUILog.i(TAG, "closeGameRoom");
    endRoom();
    if (audioPlay != null) {
      audioPlay.destroy();
    }
    finish();
  }

  private void endRoom() {
    NEVoiceRoomKit.getInstance()
        .endRoom(
            new NEVoiceRoomCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                ALog.i(TAG, "endRoom success");
                ToastUtils.INSTANCE.showShortToast(
                    GameAnchorActivity.this,
                    getString(
                        com.netease
                            .yunxin
                            .kit
                            .voiceroomkit
                            .ui
                            .base
                            .R
                            .string
                            .voiceroom_host_close_room_success));
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "endRoom onFailure");
              }
            });
  }

  @Override
  protected void onResume() {
    super.onResume();
    // 注意：要在此处调用onResume()方法
    //    quickStartGameViewModel.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    // 注意：要在此处调用onPause()方法
    //    quickStartGameViewModel.onPause();
  }

  @Override
  public void onBackPressed() {
    //    quickStartGameViewModel.onDestroy();
    super.onBackPressed();
  }

  @Override
  public void onClickSmallWindow() {}

  @Override
  protected void initDataObserver() {
    super.initDataObserver();
    roomViewModel.applySeatListData.observe(this, this::onApplySeats);
  }

  protected void createMoreItems() {
    moreItems =
        Arrays.asList(
            new ChatRoomMoreDialog.MoreItem(
                VoiceRoomBaseActivity.MORE_ITEM_MICRO_PHONE,
                com.netease
                    .yunxin
                    .kit
                    .voiceroomkit
                    .ui
                    .base
                    .R
                    .drawable
                    .selector_more_micro_phone_status,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_mic)),
            new ChatRoomMoreDialog.MoreItem(
                VoiceRoomBaseActivity.MORE_ITEM_EAR_BACK,
                com.netease
                    .yunxin
                    .kit
                    .voiceroomkit
                    .ui
                    .base
                    .R
                    .drawable
                    .selector_more_ear_back_status,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_earback)),
            new ChatRoomMoreDialog.MoreItem(
                VoiceRoomBaseActivity.MORE_ITEM_MIXER,
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.drawable.icon_room_more_mixer,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_mixer)),
            new ChatRoomMoreDialog.MoreItem(
                VoiceRoomBaseActivity.MORE_ITEM_AUDIO,
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.drawable.icon_room_more_audio,
                getString(
                    com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_audio_effect)),
            new ChatRoomMoreDialog.MoreItem(
                VoiceRoomBaseActivity.MORE_ITEM_REPORT,
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.drawable.icon_room_more_report,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_report)),
            new ChatRoomMoreDialog.MoreItem(
                VoiceRoomBaseActivity.MORE_ITEM_FINISH,
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.drawable.icon_room_more_finish,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_end)));
  }

  @Override
  protected boolean isAnchor() {
    return true;
  }
}

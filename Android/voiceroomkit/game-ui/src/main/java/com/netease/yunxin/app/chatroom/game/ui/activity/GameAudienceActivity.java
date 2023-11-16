// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.chatroom.game.ui.R;
import com.netease.yunxin.app.chatroom.game.ui.widget.GameCustomButtons;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.gamekit.api.NEGameKit;
import com.netease.yunxin.kit.gamekit.api.model.NEMemberState;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomCallback;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomKit;
import com.netease.yunxin.kit.voiceroomkit.ui.base.dialog.ChatRoomMoreDialog;
import com.netease.yunxin.kit.voiceroomkit.ui.base.viewmodel.AudienceVoiceRoomViewModel;
import com.netease.yunxin.kit.voiceroomkit.ui.base.viewmodel.VoiceRoomViewModel;
import java.util.Arrays;
import kotlin.Unit;

public class GameAudienceActivity extends GameBaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ivGift.setVisibility(View.VISIBLE);
    handleGameRoomView();
    AudienceVoiceRoomViewModel audienceVoiceRoomViewModel =
        new ViewModelProvider(this).get(AudienceVoiceRoomViewModel.class);
    audienceVoiceRoomViewModel.selfJoinChatroomLiveData.observe(
        this,
        new Observer<Boolean>() {
          @Override
          public void onChanged(Boolean aBoolean) {
            handleGameEvent();
            gamePreparingView.setType(GameCustomButtons.ButtonType.AUDIENCE);
            observeGameEvent();
          }
        });
  }

  @Override
  protected VoiceRoomViewModel getRoomViewModel() {
    return new ViewModelProvider(this).get(AudienceVoiceRoomViewModel.class);
  }

  private void handleGameRoomView() {
    ImageView iv =
        baseAudioView.findViewById(
            com.netease.yunxin.kit.voiceroomkit.ui.base.R.id.iv_small_window);
    iv.setVisibility(View.GONE);
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

  protected void createMoreItems() {
    moreItems =
        Arrays.asList(
            new ChatRoomMoreDialog.MoreItem(
                MORE_ITEM_MICRO_PHONE,
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
                MORE_ITEM_EAR_BACK,
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
                MORE_ITEM_MIXER,
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.drawable.icon_room_more_mixer,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_mixer)),
            new ChatRoomMoreDialog.MoreItem(
                MORE_ITEM_REPORT,
                com.netease.yunxin.kit.voiceroomkit.ui.base.R.drawable.icon_room_more_report,
                getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_report)));
  }

  @Override
  protected boolean isAnchor() {
    return false;
  }

  @Override
  protected void doLeaveRoom() {
    if (gameViewModel != null) {
      if (NEGameKit.getInstance().getMemberState(UserInfoManager.getSelfUserUuid())
          == NEMemberState.PLAYING) {
        DialogUtil.showECConfirmDialog(
            this,
            getString(com.netease.yunxin.kit.common.ui.R.string.dialog_tips_title),
            getString(R.string.game_leave_seat_confirm),
            getString(R.string.cancel),
            getString(R.string.sure),
            aBoolean -> {
              if (Boolean.TRUE.equals(aBoolean)) {
                gameViewModel.leaveGame(null);
                leaveRoomBySelf();
                finish();
              }
            });
      } else if (NEGameKit.getInstance().getMemberState(UserInfoManager.getSelfUserUuid())
          == NEMemberState.PREPARING) {
        gameViewModel.leaveGame(null);
        leaveRoomBySelf();
        finish();
      } else {
        leaveRoomBySelf();
        finish();
      }
    }
  }

  private void leaveRoomBySelf() {
    NEVoiceRoomKit.getInstance()
        .leaveRoom(
            new NEVoiceRoomCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                ALog.i(TAG, "leaveRoom success");
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "leaveRoom onFailure");
              }
            });
  }
}

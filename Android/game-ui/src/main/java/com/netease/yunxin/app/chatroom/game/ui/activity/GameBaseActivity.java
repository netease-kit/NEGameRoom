// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.chatroom.game.ui.R;
import com.netease.yunxin.app.chatroom.game.ui.constant.GameUIConstant;
import com.netease.yunxin.app.chatroom.game.ui.utils.GameUILog;
import com.netease.yunxin.app.chatroom.game.ui.viewmodel.GameViewModel;
import com.netease.yunxin.app.chatroom.game.ui.widget.GameContainerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.entertainment.common.model.RoomSeat;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.entertainment.common.utils.ViewUtils;
import com.netease.yunxin.kit.entertainment.common.utils.VoiceRoomUtils;
import com.netease.yunxin.kit.gamekit.api.NEGameCallback2;
import com.netease.yunxin.kit.gamekit.api.NEGameKit;
import com.netease.yunxin.kit.gamekit.api.NEGameStatus;
import com.netease.yunxin.kit.gamekit.api.model.NEGame;
import com.netease.yunxin.kit.gamekit.api.model.NEMemberState;
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo;
import com.netease.yunxin.kit.gamekit.api.model.event.GameKeyWordToHitEvent;
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameCreatedEvent;
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameEndedEvent;
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameStartedEvent;
import com.netease.yunxin.kit.roomkit.api.NERoomMember;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomCallback;
import com.netease.yunxin.kit.voiceroomkit.api.NEVoiceRoomKit;
import com.netease.yunxin.kit.voiceroomkit.api.model.NEVoiceRoomMember;
import com.netease.yunxin.kit.voiceroomkit.ui.base.activity.VoiceRoomBaseActivity;
import com.netease.yunxin.kit.voiceroomkit.ui.base.chatroom.ChatRoomMsgCreator;
import com.netease.yunxin.kit.voiceroomkit.ui.base.dialog.ListItemDialog;
import com.netease.yunxin.kit.voiceroomkit.ui.base.view.NESeatListView;
import com.netease.yunxin.kit.voiceroomkit.ui.base.widget.OnItemClickListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kotlin.Unit;

public abstract class GameBaseActivity extends VoiceRoomBaseActivity {

  protected NESeatListView seatListView;
  protected GameViewModel gameViewModel;
  protected GameContainerView gamePreparingView;
  private FrameLayout gamePreparingLayout;
  private FrameLayout gameCanvas;
  private ImageView ivGameCanvasMask;
  private LinearLayout clyAnchorLayout;
  protected ImageView ivEndGame;
  protected FrameLayout selectGameLayout;
  private int gameStatus;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initGameView();
    handleSeatUI();
  }

  private void initGameView() {
    clyAnchorLayout = findViewById(R.id.cly_anchor_layout);
    gameCanvas = findViewById(R.id.custom_container);
    selectGameLayout = findViewById(R.id.select_game_layout);
    selectGameLayout.setVisibility(View.GONE);
    ivEndGame =
        baseAudioView.findViewById(
            com.netease.yunxin.kit.voiceroomkit.ui.base.R.id.iv_small_window);
    ConstraintLayout.LayoutParams layoutParams =
        (ConstraintLayout.LayoutParams) ivEndGame.getLayoutParams();
    layoutParams.topToTop = com.netease.yunxin.kit.voiceroomkit.ui.base.R.id.rl_base_audio_ui;
    layoutParams.topMargin = ViewUtils.getStatusBarHeight(this);
    ivEndGame.setLayoutParams(layoutParams);
    ivEndGame.setVisibility(View.GONE);
    ivEndGame.setImageResource(R.drawable.game_end_game);
    ivOrderSong.setVisibility(View.GONE);
  }

  @Override
  protected void setupBaseView() {
    addSeatView();
    addGamePreparingView();
  }

  @Override
  protected void sendTextMessage() {
    String content = edtInput.getText().toString().trim();
    if (TextUtils.isEmpty(content)) {
      ToastUtils.INSTANCE.showShortToast(
          this,
          getString(
              com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_chat_message_tips));
      return;
    }
    NEVoiceRoomKit.getInstance()
        .sendTextMessage(
            content,
            new NEVoiceRoomCallback<Unit>() {
              @Override
              public void onSuccess(@Nullable Unit unit) {
                CharSequence charSequence =
                    ChatRoomMsgCreator.createText(
                        GameBaseActivity.this,
                        VoiceRoomUtils.isLocalAnchor(),
                        VoiceRoomUtils.getLocalName(),
                        content);
                rcyChatMsgList.appendItem(charSequence);
                charSequenceList.add(charSequence);
                if (NEGameKit.getInstance().getMemberState(UserInfoManager.getSelfUserUuid())
                    == NEMemberState.PLAYING) {
                  GameKeyWordToHitEvent gameKeyWordToHitEvent =
                      gameViewModel.gameKeyWordToHitLiveData.getValue();
                  GameUILog.i(
                      TAG,
                      "sendTextMessage:"
                          + content
                          + ",gameKeyWordToHitEvent:"
                          + gameKeyWordToHitEvent);
                  if (gameKeyWordToHitEvent != null && gameKeyWordToHitEvent.getMessage() != null) {
                    String keyWord = gameKeyWordToHitEvent.getMessage().realWord;
                    if (!TextUtils.isEmpty(keyWord)) {
                      if (keyWord.equals(content)) {
                        gameViewModel.notifyAPPCommonSelfTextHitState(
                            true, keyWord, content, null, null, null);
                      }
                    }
                  }
                }
              }

              @Override
              public void onFailure(int code, @Nullable String msg) {
                ALog.e(TAG, "sendTextMessage failed code = " + code + " msg = " + msg);
              }
            });
  }

  protected void observeGameEvent() {
    gameViewModel.gameCreatedLiveData.observe(
        this,
        new Observer<NEGameCreatedEvent>() {
          @Override
          public void onChanged(NEGameCreatedEvent event) {
            gamePreparingView.setGameInfo(
                new NEGame(event.getGameId(), event.getGameName(), event.getRule()));
            gameViewModel.loadGame(event.getGameId(), GameBaseActivity.this);
            showGameUI(GameUIConstant.GAME_UI_PREPARING);
            refreshSeatUI(GameUIConstant.GAME_UI_PREPARING);
            // TODO: 2023/9/6  主播开启了【你画我猜】
          }
        });
    gameViewModel.gameEndedLiveData.observe(
        this,
        new Observer<NEGameEndedEvent>() {
          @Override
          public void onChanged(NEGameEndedEvent event) {
            showGameUI(GameUIConstant.GAME_UI_NORMAL);
            refreshSeatUI(GameUIConstant.GAME_UI_NORMAL);
          }
        });
    gameViewModel.gameErrorLiveData.observe(
        this,
        new Observer<Boolean>() {
          @Override
          public void onChanged(Boolean aBoolean) {}
        });
    gameViewModel.gameMemberJoinLiveData.observe(
        this,
        new Observer<List<? extends NERoomMember>>() {
          @Override
          public void onChanged(List<? extends NERoomMember> neRoomMembers) {
            gamePreparingView.refreshGameButtonUI();
            List<RoomSeat> items = seatListView.getItems();
            if (items == null || items.isEmpty()) {
              return;
            }
            for (RoomSeat item : items) {
              for (NERoomMember neRoomMember : neRoomMembers) {
                if (item.isOn()
                    && item.getAccount() != null
                    && item.getAccount().equals(neRoomMember.getUuid())) {
                  item.setExt(getString(R.string.game_preparing_status));
                }
              }
            }
            seatListView.refresh(items);
          }
        });
    gameViewModel.gameMemberLeaveLiveData.observe(
        this,
        new Observer<List<? extends NERoomMember>>() {
          @Override
          public void onChanged(List<? extends NERoomMember> neRoomMembers) {
            gamePreparingView.refreshGameButtonUI();
            List<RoomSeat> items = seatListView.getItems();
            if (items == null || items.isEmpty()) {
              return;
            }
            for (RoomSeat item : items) {
              for (NERoomMember neRoomMember : neRoomMembers) {
                if (item.isOn() && item.getAccount().equals(neRoomMember.getUuid())) {
                  item.setExt("");
                }
              }
            }
            seatListView.refresh(items);
          }
        });
    gameViewModel.showGameLiveData.observe(
        this,
        new Observer<NERoomGameInfo>() {
          @Override
          public void onChanged(NERoomGameInfo gameInfo) {
            if (gameInfo.getGameStatus() == NEGameStatus.GAME_PREPARING) {
              gamePreparingView.setGameInfo(
                  new NEGame(gameInfo.getGameId(), gameInfo.getGameName(), gameInfo.getRule()));
              showGameUI(GameUIConstant.GAME_UI_PREPARING);
              refreshSeatUI(GameUIConstant.GAME_UI_PREPARING);
              gameViewModel.loadGame(gameInfo.getGameId(), GameBaseActivity.this);
            } else if (gameInfo.getGameStatus() == NEGameStatus.GAME_PLAYING) {
              showGameUI(GameUIConstant.GAME_UI_PLAYING);
              refreshSeatUI(GameUIConstant.GAME_UI_PLAYING);
              gameViewModel.loadGame(gameInfo.getGameId(), GameBaseActivity.this);
            } else if (gameInfo.getGameStatus() == NEGameStatus.GAME_END) {
              showGameUI(GameUIConstant.GAME_UI_NORMAL);
              refreshSeatUI(GameUIConstant.GAME_UI_NORMAL);
            }
            gameStatus = gameInfo.getGameStatus();
          }
        });
    gameViewModel.gameStartedLiveData.observe(
        this,
        new Observer<NEGameStartedEvent>() {
          @Override
          public void onChanged(NEGameStartedEvent event) {
            showGameUI(GameUIConstant.GAME_UI_PLAYING);
            refreshSeatUI(GameUIConstant.GAME_UI_PLAYING);
          }
        });

    gameViewModel.showNormalUILiveData.observe(
        this,
        new Observer<Boolean>() {
          @Override
          public void onChanged(Boolean aBoolean) {
            showGameUI(GameUIConstant.GAME_UI_NORMAL);
            refreshSeatUI(GameUIConstant.GAME_UI_NORMAL);
          }
        });
  }

  protected void refreshSeatUI(int gameUIStatus) {
    List<RoomSeat> seatList = seatListView.getItems();
    if (seatList == null || seatList.isEmpty()) {
      return;
    }
    for (RoomSeat roomSeat : seatList) {
      NEMemberState memberState = NEGameKit.getInstance().getMemberState(roomSeat.getAccount());
      if (memberState == NEMemberState.PREPARING) {
        roomSeat.setExt(getString(R.string.game_preparing_status));
      } else if (memberState == NEMemberState.PLAYING) {
        roomSeat.setExt(getString(R.string.game_playing_status));
      } else {
        roomSeat.setExt("");
      }
    }
    seatListView.refresh(seatList);
  }

  protected void showGameUI(int gameUIStatus) {
    if (GameUIConstant.GAME_UI_PREPARING == gameUIStatus) {
      gamePreparingLayout.setVisibility(View.VISIBLE);
      if (ivGameCanvasMask != null) {
        ivGameCanvasMask.setVisibility(View.VISIBLE);
      }
      clyAnchorLayout.setVisibility(View.GONE);
      seatGridView.setVisibility(View.GONE);
      seatListView.setVisibility(View.VISIBLE);
      gamePreparingView.showPreparingGameUI();
      gamePreparingView.setVisibility(View.VISIBLE);
      selectGameLayout.setVisibility(View.GONE);
      ivEndGame.setVisibility(View.GONE);
    } else if (GameUIConstant.GAME_UI_PLAYING == gameUIStatus) {
      gamePreparingLayout.setVisibility(View.GONE);
      if (ivGameCanvasMask != null) {
        ivGameCanvasMask.setVisibility(View.GONE);
      }
      clyAnchorLayout.setVisibility(View.GONE);
      seatGridView.setVisibility(View.GONE);
      seatListView.setVisibility(View.VISIBLE);
      gamePreparingView.setVisibility(View.GONE);
      selectGameLayout.setVisibility(View.GONE);
      if (isAnchor()) {
        ivEndGame.setVisibility(View.VISIBLE);
      }
    } else if (GameUIConstant.GAME_UI_NORMAL == gameUIStatus) {
      gamePreparingLayout.setVisibility(View.GONE);
      gameCanvas.removeAllViews();
      if (ivGameCanvasMask != null) {
        ivGameCanvasMask.setVisibility(View.GONE);
      }
      clyAnchorLayout.setVisibility(View.VISIBLE);
      seatGridView.setVisibility(View.VISIBLE);
      seatListView.setVisibility(View.GONE);
      gamePreparingView.setVisibility(View.GONE);
      selectGameLayout.setVisibility(View.VISIBLE);
      ivEndGame.setVisibility(View.GONE);
    } else if (GameUIConstant.GAME_UI_INIT == gameUIStatus) {
      gamePreparingLayout.setVisibility(View.GONE);
      gameCanvas.removeAllViews();
      if (ivGameCanvasMask != null) {
        ivGameCanvasMask.setVisibility(View.GONE);
      }
      clyAnchorLayout.setVisibility(View.GONE);
      seatGridView.setVisibility(View.GONE);
      seatListView.setVisibility(View.GONE);
      gamePreparingView.showPreparingGameUI();
      gamePreparingView.setVisibility(View.GONE);
      selectGameLayout.setVisibility(View.GONE);
      ivEndGame.setVisibility(View.GONE);
    }
  }

  private void addGamePreparingView() {
    gamePreparingLayout = new FrameLayout(this);
    gamePreparingLayout.setId(R.id.game_preparing_view);
    ConstraintLayout.LayoutParams layoutParams =
        new ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(316));
    layoutParams.leftToLeft = R.id.fl_cly_anchor_layout;
    layoutParams.topToBottom = R.id.seat_list_view;
    layoutParams.topMargin = SizeUtils.dp2px(10);
    baseAudioView.addView(gamePreparingLayout, layoutParams);
    gamePreparingLayout.setVisibility(View.GONE);
  }

  protected void handleGameEvent() {
    gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
    gameViewModel.initialize(
        voiceRoomInfo.getRoomUuid(),
        new NEGameCallback2<Unit>() {
          @Override
          public void onSuccess(@Nullable Unit data) {
            super.onSuccess(data);
            selectGameLayout.setVisibility(View.VISIBLE);
          }
        });

    gamePreparingView = new GameContainerView(this);
    gamePreparingLayout.addView(gamePreparingView);
    gameViewModel.gameViewLiveData.observe(
        this,
        new Observer<View>() {
          @Override
          public void onChanged(View view) {
            if (view == null) { // 在关闭游戏时，把游戏View给移除
              gameCanvas.removeAllViews();
            } else {
              // 把游戏View添加到容器内
              FrameLayout.LayoutParams layoutParams =
                  new FrameLayout.LayoutParams(
                      FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
              if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
              }
              gameCanvas.addView(view, layoutParams);
              // 在加载游戏时，view不能隐藏，隐藏之后游戏进程就停止了。这里为了能够达到预加载游戏的效果，由于游戏画布是全屏的，在游戏画布的上方盖了一层跟背景图一模一样的背景图。
              // 通过显示隐藏这个背景图来显示隐藏游戏画布。
              ivGameCanvasMask = new ImageView(GameBaseActivity.this);
              ivGameCanvasMask.setScaleType(ImageView.ScaleType.CENTER_CROP);
              FrameLayout.LayoutParams maskLayoutParams =
                  new FrameLayout.LayoutParams(
                      FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
              ivGameCanvasMask.setLayoutParams(maskLayoutParams);
              ImageLoader.with(GameBaseActivity.this.getApplicationContext())
                  .commonLoad(voiceRoomInfo.getCover(), ivGameCanvasMask);
              gameCanvas.addView(ivGameCanvasMask, maskLayoutParams);
              if (gameStatus == NEGameStatus.GAME_PLAYING) {
                ivGameCanvasMask.setVisibility(View.GONE);
              }
            }
          }
        });
  }

  protected void handleSeatUI() {
    roomViewModel.onSeatListData.observe(
        this,
        seatList -> {
          for (RoomSeat roomSeat : seatList) {
            NEMemberState memberState =
                NEGameKit.getInstance().getMemberState(roomSeat.getAccount());
            if (memberState == NEMemberState.PREPARING) {
              roomSeat.setExt(getString(R.string.game_preparing_status));
            } else if (memberState == NEMemberState.PLAYING) {
              roomSeat.setExt(getString(R.string.game_playing_status));
            } else {
              roomSeat.setExt("");
            }
          }
          seatListView.refresh(seatList);
        });
  }

  protected void addSeatView() {
    seatListView = new NESeatListView(this);
    seatListView.setId(R.id.seat_list_view);
    ConstraintLayout.LayoutParams layoutParams =
        new ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.leftToLeft = R.id.fl_cly_anchor_layout;
    layoutParams.topToBottom = R.id.tv_chat_room_member_count;
    layoutParams.leftMargin = SizeUtils.dp2px(8);
    baseAudioView.addView(seatListView, layoutParams);
    seatListView.setVisibility(View.GONE);

    seatListView.setItemClickListener(this::onGameSeatItemClick);
    List<RoomSeat> seats = roomViewModel.onSeatListData.getValue();
    seatListView.refresh(seats);
  }

  protected void onGameSeatItemClick(RoomSeat seat, int position) {
    if (ClickUtils.isFastClick()) {
      return;
    }
    if (position == 0) {
      // 主播头像屏蔽点击事件
      return;
    }
    if (isAnchor()) {
      if (seat.getStatus() == RoomSeat.Status.APPLY) {
        ToastUtils.INSTANCE.showShortToast(
            this,
            getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_applying_now));
        return;
      }
      OnItemClickListener<String> onItemClickListener = item -> onGameSeatAction(seat, item);
      List<String> items = new ArrayList<>();
      ListItemDialog itemDialog = new ListItemDialog(this);
      switch (seat.getStatus()) {
          // 抱观众上麦（点击麦位）
        case RoomSeat.Status.INIT:
          items.add(
              getString(
                  com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_invite_seat));
          items.add(
              getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_close_seat));
          break;
          // 当前存在有效用户
        case RoomSeat.Status.ON:
          items.add(
              getString(
                  com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_kickout_seat));
          final NEVoiceRoomMember member = seat.getMember();
          if (member != null) {
            items.add(
                member.isAudioBanned()
                    ? getString(
                        com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_unmute_seat)
                    : getString(
                        com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_mute_seat));
          }
          items.add(
              getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_close_seat));
          break;
          // 当前麦位已经被关闭
        case RoomSeat.Status.CLOSED:
          items.add(
              getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_open_seat));
          break;
      }
      items.add(getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_cancel));
      itemDialog.setOnItemClickListener(onItemClickListener).show(items);
    } else {
      switch (seat.getStatus()) {
        case RoomSeat.Status.INIT:
          if (seat.getStatus() == RoomSeat.Status.CLOSED) {
            ToastUtils.INSTANCE.showShortToast(
                this,
                getString(
                    com.netease
                        .yunxin
                        .kit
                        .voiceroomkit
                        .ui
                        .base
                        .R
                        .string
                        .voiceroom_seat_already_closed));
          } else if (roomViewModel.isCurrentUserOnSeat()) {
            ToastUtils.INSTANCE.showShortToast(
                this,
                getString(
                    com.netease
                        .yunxin
                        .kit
                        .voiceroomkit
                        .ui
                        .base
                        .R
                        .string
                        .voiceroom_already_on_seat));
          } else {
            applySeat(seat.getSeatIndex());
          }
          break;
        case RoomSeat.Status.APPLY:
          ToastUtils.INSTANCE.showShortToast(
              this,
              getString(
                  com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_seat_applied));
          break;
        case RoomSeat.Status.ON:
          if (VoiceRoomUtils.isLocal(seat.getAccount())) {
            new ListItemDialog(this)
                .setOnItemClickListener(
                    item -> {
                      if (getString(
                              com.netease
                                  .yunxin
                                  .kit
                                  .voiceroomkit
                                  .ui
                                  .base
                                  .R
                                  .string
                                  .voiceroom_dowmseat)
                          .equals(item)) {
                        if (NEGameKit.getInstance()
                                .getMemberState(UserInfoManager.getSelfUserUuid())
                            == NEMemberState.PLAYING) {
                          DialogUtil.showECConfirmDialog(
                              this,
                              getString(
                                  com.netease.yunxin.kit.common.ui.R.string.dialog_tips_title),
                              getString(R.string.game_leave_seat_confirm),
                              getString(R.string.cancel),
                              getString(R.string.sure),
                              aBoolean -> {
                                if (Boolean.TRUE.equals(aBoolean)) {
                                  leaveSeat();
                                }
                              });
                        } else {
                          leaveSeat();
                        }
                      }
                    })
                .show(
                    Arrays.asList(
                        getString(
                            com.netease
                                .yunxin
                                .kit
                                .voiceroomkit
                                .ui
                                .base
                                .R
                                .string
                                .voiceroom_dowmseat),
                        getString(
                            com.netease
                                .yunxin
                                .kit
                                .voiceroomkit
                                .ui
                                .base
                                .R
                                .string
                                .voiceroom_cancel)));
          } else {
            ToastUtils.INSTANCE.showShortToast(
                this,
                getString(
                    com.netease
                        .yunxin
                        .kit
                        .voiceroomkit
                        .ui
                        .base
                        .R
                        .string
                        .voiceroom_seat_already_taken));
          }
          break;
        case RoomSeat.Status.CLOSED:
          ToastUtils.INSTANCE.showShortToast(
              this,
              getString(
                  com.netease
                      .yunxin
                      .kit
                      .voiceroomkit
                      .ui
                      .base
                      .R
                      .string
                      .voiceroom_seat_already_closed));
          break;
      }
    }
  }

  protected void onGameSeatAction(RoomSeat seat, String item) {
    if (item.equals(
        getString(
            com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_kickout_seat_sure))) {
      new ListItemDialog(this)
          .setOnItemClickListener(
              item1 -> {
                if (getString(
                        com.netease
                            .yunxin
                            .kit
                            .voiceroomkit
                            .ui
                            .base
                            .R
                            .string
                            .voiceroom_kickout_seat_sure)
                    .equals(item1)) {
                  if (NEGameKit.getInstance().getMemberState(seat.getAccount())
                      == NEMemberState.PLAYING) {
                    DialogUtil.showECConfirmDialog(
                        this,
                        getString(com.netease.yunxin.kit.common.ui.R.string.dialog_tips_title),
                        getString(R.string.game_kick_seat_confirm),
                        getString(R.string.cancel),
                        getString(R.string.sure),
                        aBoolean -> {
                          if (Boolean.TRUE.equals(aBoolean)) {
                            kickSeat(seat);
                          }
                        });
                  } else {
                    kickSeat(seat);
                  }
                }
              })
          .show(
              Arrays.asList(
                  getString(
                      com.netease
                          .yunxin
                          .kit
                          .voiceroomkit
                          .ui
                          .base
                          .R
                          .string
                          .voiceroom_kickout_seat_sure),
                  getString(
                      com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_cancel)));
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_close_seat))) {
      if (NEGameKit.getInstance().getMemberState(seat.getAccount()) == NEMemberState.PLAYING) {
        DialogUtil.showECConfirmDialog(
            this,
            getString(com.netease.yunxin.kit.common.ui.R.string.dialog_tips_title),
            getString(R.string.game_close_seat_confirm),
            getString(R.string.cancel),
            getString(R.string.sure),
            aBoolean -> {
              if (Boolean.TRUE.equals(aBoolean)) {
                closeSeat(seat);
              }
            });
      } else {
        closeSeat(seat);
      }
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_invite_seat))) {
      inviteSeat0(seat);
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_kickout_seat))) {
      if (NEGameKit.getInstance().getMemberState(seat.getAccount()) == NEMemberState.PLAYING) {
        DialogUtil.showECConfirmDialog(
            this,
            getString(com.netease.yunxin.kit.common.ui.R.string.dialog_tips_title),
            getString(R.string.game_kick_seat_confirm),
            getString(R.string.cancel),
            getString(R.string.sure),
            aBoolean -> {
              if (Boolean.TRUE.equals(aBoolean)) {
                kickSeat(seat);
              }
            });
      } else {
        kickSeat(seat);
      }
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_mute_seat))) {
      muteSeat(seat);
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_unmute_seat))) {
      unmuteSeat(seat);
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_open_seat))) {
      openSeat(seat);
    } else if (item.equals(
        getString(com.netease.yunxin.kit.voiceroomkit.ui.base.R.string.voiceroom_leave_room))) {
      leaveRoom();
    }
  }

  @Override
  protected String getPageName() {
    return GameUIConstant.TAG_REPORT_PAGE_GAME_ROOM;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //    quickStartGameViewModel.onDestroy();
  }
}

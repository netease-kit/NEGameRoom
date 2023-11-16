// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.viewmodel;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.app.chatroom.game.ui.utils.GameUILog;
import com.netease.yunxin.app.chatroom.game.ui.utils.GameUtil;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.entertainment.common.utils.VoiceRoomUtils;
import com.netease.yunxin.kit.gamekit.api.NEGameCallback;
import com.netease.yunxin.kit.gamekit.api.NEGameCallback2;
import com.netease.yunxin.kit.gamekit.api.NEGameErrorCode;
import com.netease.yunxin.kit.gamekit.api.NEGameKit;
import com.netease.yunxin.kit.gamekit.api.NEGameListener;
import com.netease.yunxin.kit.gamekit.api.model.NECreateGameParams;
import com.netease.yunxin.kit.gamekit.api.model.NEGameKitOptions;
import com.netease.yunxin.kit.gamekit.api.model.NEGameLoginInfo;
import com.netease.yunxin.kit.gamekit.api.model.NELoadGameParams;
import com.netease.yunxin.kit.gamekit.api.model.NERoomGameInfo;
import com.netease.yunxin.kit.gamekit.api.model.event.GameKeyWordToHitEvent;
import com.netease.yunxin.kit.gamekit.api.model.event.GameMemberPaintingEvent;
import com.netease.yunxin.kit.gamekit.api.model.event.GamePublicMessageEvent;
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameCreatedEvent;
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameEndedEvent;
import com.netease.yunxin.kit.gamekit.api.model.roomevent.NEGameStartedEvent;
import com.netease.yunxin.kit.roomkit.api.NERoomContext;
import com.netease.yunxin.kit.roomkit.api.NERoomKit;
import com.netease.yunxin.kit.roomkit.api.NERoomListener;
import com.netease.yunxin.kit.roomkit.api.NERoomListenerAdapter;
import com.netease.yunxin.kit.roomkit.api.NERoomMember;
import com.netease.yunxin.kit.roomkit.api.model.NERoomConnectType;
import com.netease.yunxin.kit.voiceroomkit.ui.base.viewmodel.VoiceRoomViewModel;
import java.util.List;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel;

// 游戏业务相关的ViewModel,初始化时机：加入房间成功后
public class GameViewModel extends VoiceRoomViewModel {
  private static final String TAG = "GameViewModel";
  private final NEGameKit gameKit = NEGameKit.getInstance();
  private String gameId = "";
  public final MutableLiveData<View> gameViewLiveData = new MutableLiveData<>();
  public final MutableLiveData<NEGameCreatedEvent> gameCreatedLiveData = new MutableLiveData<>();
  public final MutableLiveData<NEGameStartedEvent> gameStartedLiveData = new MutableLiveData<>();
  public final MutableLiveData<NEGameEndedEvent> gameEndedLiveData = new MutableLiveData<>();
  public final MutableLiveData<Boolean> gameErrorLiveData = new MutableLiveData<>();
  public final MutableLiveData<List<? extends NERoomMember>> gameMemberJoinLiveData =
      new MutableLiveData<>();
  public final MutableLiveData<List<? extends NERoomMember>> gameMemberLeaveLiveData =
      new MutableLiveData<>();
  public final MutableLiveData<NERoomGameInfo> showGameLiveData = new MutableLiveData<>();
  public final MutableLiveData<Boolean> showNormalUILiveData = new MutableLiveData<>();
  public final MutableLiveData<GameKeyWordToHitEvent> gameKeyWordToHitLiveData =
      new MutableLiveData<>();
  private final NERoomListener roomListener =
      new NERoomListenerAdapter() {
        @Override
        public void onRoomConnectStateChanged(@NonNull NERoomConnectType state) {
          super.onRoomConnectStateChanged(state);
          GameUILog.i(TAG, "onRoomConnectStateChanged state:" + state);
          if (state == NERoomConnectType.Reconnect) {
            // 房间重新连接时，重新获取游戏信息
            getGameInfo();
          }
        }
      };
  private final NEGameListener gameListener =
      new NEGameListener() {

        @Override
        public void onMemberLeaveGame(@NonNull List<? extends NERoomMember> members) {
          gameMemberLeaveLiveData.setValue(members);
        }

        public void onMemberJoinGame(@NonNull List<? extends NERoomMember> members) {
          gameMemberJoinLiveData.setValue(members);
        }

        @Override
        public void onMemberPainting(@NonNull GameMemberPaintingEvent event) {}

        @Override
        public void onGameKeyWordToHit(@NonNull GameKeyWordToHitEvent event) {
          gameKeyWordToHitLiveData.setValue(event);
        }

        @Override
        public void onGameSelfClickGameSettleClose() {
          GameUILog.i(TAG, "onGameSelfClickGameSettleClose");
          // 一局游戏结束后，先恢复成普通语聊房样式
          showNormalUILiveData.setValue(true);
          // 主播去关闭当前游戏，再重开一局游戏
          if (VoiceRoomUtils.isLocalAnchor()) {
            startNewRoundOfGame();
          }
        }

        @Override
        public void getGameRect(@NonNull GameViewInfoModel gameViewInfoModel) {
          gameViewInfoModel.view_game_rect.top = SizeUtils.dp2px(130);
          gameViewInfoModel.view_game_rect.left = SizeUtils.dp2px(18);
          gameViewInfoModel.view_game_rect.right = SizeUtils.dp2px(18);
          gameViewInfoModel.view_game_rect.bottom = SizeUtils.dp2px(160);
        }

        @Override
        public void onGamePublicMessage(@NonNull GamePublicMessageEvent event) {}

        @Override
        public void onGameError(int code, @NonNull String msg) {
          gameErrorLiveData.setValue(true);
        }

        @Override
        public void onGameEnded(@NotNull NEGameEndedEvent event) {
          gameId = "";
          gameEndedLiveData.setValue(event);
        }

        @Override
        public void onGameStarted(@NotNull NEGameStartedEvent event) {
          gameId = event.getGameId();
          gameStartedLiveData.setValue(event);
        }

        @Override
        public void onGameCreated(@NotNull NEGameCreatedEvent event) {
          gameId = event.getGameId();
          gameCreatedLiveData.setValue(event);
        }
      };
  private NERoomContext roomContext;

  private void startNewRoundOfGame() {
    GameUILog.i(TAG, "startNewRoundOfGame");
    createGame(gameId);
  }

  public void initialize(String roomUuid, NEGameCallback2<Unit> callback) {
    roomContext = NERoomKit.getInstance().getRoomService().getRoomContext(roomUuid);
    if (roomContext != null) {
      roomContext.addRoomListener(roomListener);
    }
    GameUtil.setRoomUuid(roomUuid);
    NEGameKitOptions options =
        new NEGameKitOptions(GameUtil.getAppKey(), roomUuid, GameUtil.getBaseUrl());
    NEGameLoginInfo loginInfo =
        new NEGameLoginInfo(UserInfoManager.getSelfUserUuid(), UserInfoManager.getSelfUserToken());
    gameKit.initialize(
        options,
        loginInfo,
        new NEGameCallback2<Unit>() {
          @Override
          public void onSuccess(@Nullable Unit data) {
            super.onSuccess(data);
            addGameListener();
            callback.onSuccess(null);
            getGameInfo();
          }

          @Override
          public void onError(int code, @Nullable String message) {
            super.onError(code, message);
            callback.onError(code, message);
          }
        });
  }

  private void getGameInfo() {
    gameKit.getRoomGameInfo(
        new NEGameCallback2<NERoomGameInfo>() {
          @Override
          public void onSuccess(@Nullable NERoomGameInfo data) {
            super.onSuccess(data);
            if (data != null) {
              gameId = data.getGameId();
              GameUILog.i(TAG, "getGameInfo data:" + data);
              if (!TextUtils.isEmpty(gameId)) {
                showGameLiveData.setValue(data);
              }
            }
          }

          @Override
          public void onError(int code, @Nullable String message) {
            super.onError(code, message);
            GameUILog.e(TAG, "getGameInfo onError,code:" + code + ",message:" + message);
            showNormalUILiveData.setValue(true);
          }
        });
  }

  private void addGameListener() {
    gameKit.addGameListener(gameListener);
  }

  public void createGame(String gameId) {
    this.gameId = gameId;
    gameKit.createGame(
        new NECreateGameParams(gameId, null),
        new NEGameCallback2<Unit>() {
          @Override
          public void onSuccess(@Nullable Unit data) {
            super.onSuccess(data);
            GameUILog.i(TAG, "createGame onSuccess");
          }

          @Override
          public void onError(int code, @Nullable String message) {
            super.onError(code, message);
            GameUILog.i(TAG, "createGame onError,code:" + code + ",message:" + message);
          }
        });
  }

  public void endGame(NEGameCallback<Unit> callback) {
    gameKit.endGame(
        new NEGameCallback2<Unit>() {
          @Override
          public void onSuccess(@Nullable Unit data) {
            super.onSuccess(data);
            gameId = "";
            if (callback != null) {
              callback.onResult(NEGameErrorCode.SUCCESS, "", null);
            }
          }

          @Override
          public void onError(int code, @Nullable String message) {
            super.onError(code, message);
            if (callback != null) {
              callback.onResult(code, message, null);
            }
          }
        });
  }

  public void leaveGame(NEGameCallback<Unit> callback) {
    gameKit.leaveGame(callback);
  }

  public void loadGame(String gameId, Activity activity) {
    gameKit.loadGame(
        new NELoadGameParams(gameId, "zh-CN", activity),
        new NEGameCallback2<View>() {
          @Override
          public void onSuccess(@Nullable View data) {
            super.onSuccess(data);
            gameViewLiveData.postValue(data);
          }

          @Override
          public void onError(int code, @Nullable String message) {
            super.onError(code, message);
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    GameUILog.i(TAG, "onCleared");
    gameId = "";
    gameKit.removeGameListener(gameListener);
    gameKit.destroy();
    if (roomContext != null) {
      roomContext.removeRoomListener(roomListener);
    }
  }

  public void notifyAPPCommonSelfTextHitState(
      boolean isHit,
      String keyWord,
      String text,
      String wordType,
      List<String> keyWordList,
      List<Integer> numberList) {
    gameKit.notifyAPPCommonSelfTextHitState(
        isHit, keyWord, text, wordType, keyWordList, numberList);
  }
}

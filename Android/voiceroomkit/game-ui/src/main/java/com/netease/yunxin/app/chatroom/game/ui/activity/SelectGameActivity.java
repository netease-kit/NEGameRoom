// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.app.chatroom.game.ui.R;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.gamekit.api.NEGameCallback2;
import com.netease.yunxin.kit.gamekit.api.NEGameKit;
import com.netease.yunxin.kit.gamekit.api.model.NEGame;
import com.netease.yunxin.kit.gamekit.api.model.NEGameList;
import java.util.ArrayList;
import java.util.List;

public class SelectGameActivity extends BaseActivity {
  public static final String TAG = "SelectGameActivity";
  private GameAdapter gameAdapter;

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_game_select);
    RecyclerView recyclerView = findViewById(R.id.rv);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(
        new RecyclerView.ItemDecoration() {
          @Override
          public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull RecyclerView.State state) {
            int dp20 = SizeUtils.dp2px(20f);
            int dp16 = SizeUtils.dp2px(16f);
            outRect.set(dp20, 0, dp20, dp16);
          }
        });
    gameAdapter = new GameAdapter();
    recyclerView.setAdapter(gameAdapter);
    gameAdapter.setGameListener(
        new OnGameSelectListener() {
          @Override
          public void onGameSelect(NEGame game) {
            setResult(RESULT_OK, new Intent().putExtra("gameId", game.getGameId()));
            SelectGameActivity.this.finish();
          }
        });
    getGameList();
  }

  private void getGameList() {
    NEGameKit.getInstance()
        .getGameList(
            new NEGameCallback2<NEGameList>() {
              @Override
              public void onSuccess(@Nullable NEGameList data) {
                super.onSuccess(data);
                gameAdapter.setData(data.getData());
              }

              @Override
              public void onError(int code, @Nullable String message) {
                super.onError(code, message);
              }
            });
  }

  private static class GameAdapter extends RecyclerView.Adapter<GameViewHolder> {
    private List<NEGame> gameList = new ArrayList<>();
    private OnGameSelectListener listener;

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item, parent, false);
      return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
      GameViewHolder topicViewHolder = (GameViewHolder) holder;
      NEGame neGame = gameList.get(position);
      ImageLoader.with(XKitUtils.getApplicationContext())
          .commonLoad(neGame.getThumbnail(), topicViewHolder.ivGameIcon);
      topicViewHolder.tvGameName.setText(neGame.getGameName());
      topicViewHolder.tvGameDesc.setText(neGame.getGameDesc());
      topicViewHolder.itemView.setOnClickListener(
          v -> {
            ALog.i(TAG, "select game: " + neGame);
            if (listener != null) {
              listener.onGameSelect(neGame);
            }
          });
    }

    @Override
    public int getItemCount() {
      return gameList.size();
    }

    public void setData(List<NEGame> gameList) {
      this.gameList = gameList;
      notifyDataSetChanged();
    }

    public void setGameListener(OnGameSelectListener listener) {
      this.listener = listener;
    }
  }

  private static class GameViewHolder extends RecyclerView.ViewHolder {
    private ImageView ivGameIcon;
    private TextView tvGameName;
    private TextView tvGameDesc;

    public GameViewHolder(@NonNull View itemView) {
      super(itemView);
      ivGameIcon = itemView.findViewById(R.id.iv_game_icon);
      tvGameName = itemView.findViewById(R.id.tv_game_name);
      tvGameDesc = itemView.findViewById(R.id.tv_game_desc);
    }
  }

  public interface OnGameSelectListener {
    void onGameSelect(NEGame game);
  }
}

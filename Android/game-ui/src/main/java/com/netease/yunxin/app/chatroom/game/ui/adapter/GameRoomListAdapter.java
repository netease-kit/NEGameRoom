// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.game.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.entertainment.common.adapter.RoomListAdapter;
import com.netease.yunxin.kit.entertainment.common.databinding.ItemVoiceRoomListBinding;
import com.netease.yunxin.kit.entertainment.common.model.RoomModel;

public class GameRoomListAdapter extends RoomListAdapter {

  public GameRoomListAdapter(Context context) {
    super(context);
  }

  @NonNull
  @Override
  public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemVoiceRoomListBinding binding =
        ItemVoiceRoomListBinding.inflate(LayoutInflater.from(context), parent, false);
    return new VoiceRoomViewHolder(binding, context);
  }

  public static class VoiceRoomViewHolder extends RoomViewHolder {

    public VoiceRoomViewHolder(ItemVoiceRoomListBinding binding, Context context) {
      super(binding, context);
    }

    @Override
    public void setData(RoomModel info) {
      super.setData(info);
      binding.ivType.setVisibility(View.GONE);
      if (TextUtils.isEmpty(info.getGameName())) {
        binding.tvRightTopTag.setVisibility(android.view.View.GONE);
      } else {
        binding.tvRightTopTag.setVisibility(android.view.View.VISIBLE);
        binding.tvRightTopTag.setText(info.getGameName());
      }
    }
  }
}

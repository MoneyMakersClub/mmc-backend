package com.mmc.bookduck.domain.friend.dto.response;

import com.mmc.bookduck.domain.friend.dto.common.FriendUnitDto;

import java.util.List;

public record FriendListResponseDto(
        List<FriendUnitDto> friendList,
        int friendCount
) {
    public static FriendListResponseDto from(List<FriendUnitDto> friendList){
      return new FriendListResponseDto(friendList, friendList.size());
    }
}

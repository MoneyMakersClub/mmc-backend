package com.mmc.bookduck.domain.friend.dto.response;

import com.mmc.bookduck.domain.friend.dto.common.FriendRequestUnitDto;

import java.util.List;

public record FriendRequestListResponseDto(
    List<FriendRequestUnitDto> requestList,
    int requestCount
) {
    public static FriendRequestListResponseDto from(List<FriendRequestUnitDto> requestList) {
        return new FriendRequestListResponseDto(requestList, requestList.size());
    }
}


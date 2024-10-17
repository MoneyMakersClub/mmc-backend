package com.mmc.bookduck.domain.friend.dto.common;

import com.mmc.bookduck.domain.friend.entity.FriendRequest;
import com.mmc.bookduck.domain.friend.entity.FriendRequestStatus;

public record FriendRequestUnitDto(
        Long requestId,
        String receiverNickname,
        FriendRequestStatus friendRequestStatus
) {
    public static FriendRequestUnitDto from(FriendRequest friendRequest) {
        return new FriendRequestUnitDto(
                friendRequest.getRequestId(),
                friendRequest.getReceiver().getNickname(),
                friendRequest.getFriendRequestStatus()
        );
    }
}

package com.mmc.bookduck.domain.friend.dto.request;

import com.mmc.bookduck.domain.friend.entity.FriendRequest;
import com.mmc.bookduck.domain.friend.entity.FriendRequestStatus;
import jakarta.validation.constraints.NotNull;
import com.mmc.bookduck.domain.user.entity.User;

public record FriendRequestDto(
        @NotNull Long receiverId
) {
    public FriendRequest toEntity(User sender, User receiver) {
        return FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .friendRequestStatus(FriendRequestStatus.PENDING)
                .build();
    }
}

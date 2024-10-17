package com.mmc.bookduck.domain.friend.dto.request;

import com.mmc.bookduck.domain.friend.entity.Friend;
import com.mmc.bookduck.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;

public record FriendCreateRequestDto(
        @NotNull Long friendRequestId) {
    public Friend toEntity(User sender, User receiver) {
        return Friend.builder()
                .user1(receiver)
                .user2(sender)
                .build();
    }
}

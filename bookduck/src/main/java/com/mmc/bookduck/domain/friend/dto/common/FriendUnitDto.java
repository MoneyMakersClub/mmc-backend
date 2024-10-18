package com.mmc.bookduck.domain.friend.dto.common;

import com.mmc.bookduck.domain.friend.entity.Friend;
import com.mmc.bookduck.domain.item.dto.common.UserItemEquippedDto;

public record FriendUnitDto(
        Long friendId, // 친구 삭제 기능
        Long userId,
        String nickname,
        UserItemEquippedDto userItemEquipped
) {
    public static FriendUnitDto from(Friend friend, UserItemEquippedDto userItemEquipped) {
        return new FriendUnitDto(
                friend.getFriendId(),
                friend.getUser2().getUserId(),
                friend.getUser2().getNickname(),
                userItemEquipped
        );
    }
}

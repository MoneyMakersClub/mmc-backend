package com.mmc.bookduck.domain.friend.dto.common;

import com.mmc.bookduck.domain.friend.entity.Friend;
import com.mmc.bookduck.domain.skin.dto.common.UserSkinEquippedDto;

public record FriendUnitDto(
        Long friendId, // 친구 삭제 기능
        Long userId,
        String nickname,
        UserSkinEquippedDto userSkinEquipped
) {
    public static FriendUnitDto from(Friend friend, UserSkinEquippedDto userSkinEquipped) {
        return new FriendUnitDto(
                friend.getFriendId(),
                friend.getUser2().getUserId(),
                friend.getUser2().getNickname(),
                userSkinEquipped
        );
    }
}

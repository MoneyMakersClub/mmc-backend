package com.mmc.bookduck.domain.item.dto.common;

import com.mmc.bookduck.domain.item.entity.UserItem;

public record UserItemEquippedDto(
        Long userItemId,
        Long itemId
) {
    public static UserItemEquippedDto from(UserItem userItem){
        return new UserItemEquippedDto(
                userItem.getUserItemId(),
                userItem.getItem().getItemId()
        );
    }
}

package com.mmc.bookduck.domain.skin.dto.common;

import com.mmc.bookduck.domain.skin.entity.UserSkin;

public record UserSkinEquippedDto(
        Long userSkinId,
        Long skinId
) {
    public static UserSkinEquippedDto from(UserSkin userSkin){
        return new UserSkinEquippedDto(
                userSkin.getUserSkinId(),
                userSkin.getSkin().getSkinId()
        );
    }
}

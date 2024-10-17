package com.mmc.bookduck.domain.skin.service;

import com.mmc.bookduck.domain.skin.dto.common.UserSkinEquippedDto;
import com.mmc.bookduck.domain.skin.repository.UserSkinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSkinService {
    private final UserSkinRepository userSkinRepository;

    // 장착된 스킨 조회, 기본 스킨 처리
    public UserSkinEquippedDto getEquippedSkinOrDefault(Long userId) {
        return userSkinRepository.findByUserUserIdAndIsEquippedTrue(userId)
                .map(UserSkinEquippedDto::from)
                .orElseGet(() -> new UserSkinEquippedDto(0L, 0L));  // 기본 스킨(=아무 장착도 하지 않은 상태)의 ID에 0 부여
    }
}

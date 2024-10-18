package com.mmc.bookduck.domain.item.service;

import com.mmc.bookduck.domain.item.dto.common.UserItemEquippedDto;
import com.mmc.bookduck.domain.item.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserItemService {
    private final UserItemRepository userItemRepository;

    // 장착된 스킨 조회, 기본 스킨 처리
    public UserItemEquippedDto getEquippedItemOrDefault(Long userId) {
        return userItemRepository.findByUserUserIdAndIsEquippedTrue(userId)
                .map(UserItemEquippedDto::from)
                .orElseGet(() -> new UserItemEquippedDto(0L, 0L));  // 기본 스킨(=아무 장착도 하지 않은 상태)의 ID에 0 부여
    }
}

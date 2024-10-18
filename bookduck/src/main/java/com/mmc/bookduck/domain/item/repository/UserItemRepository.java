package com.mmc.bookduck.domain.item.repository;

import com.mmc.bookduck.domain.item.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    Optional<UserItem> findByUserUserIdAndIsEquippedTrue(Long userId);
}

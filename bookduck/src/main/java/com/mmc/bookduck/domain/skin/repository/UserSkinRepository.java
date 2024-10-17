package com.mmc.bookduck.domain.skin.repository;

import com.mmc.bookduck.domain.skin.entity.UserSkin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSkinRepository extends JpaRepository<UserSkin, Long> {
    Optional<UserSkin> findByUserUserIdAndIsEquippedTrue(Long userId);
}

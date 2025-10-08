package com.mmc.bookduck.domain.bookclub.repository;

import com.mmc.bookduck.domain.bookclub.entity.ClubInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubInviteRepository extends JpaRepository<ClubInvite, Long> {
    Optional<ClubInvite> findByInviteCode(String inviteCode);
}

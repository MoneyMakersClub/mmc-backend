package com.mmc.bookduck.domain.club.repository;

import com.mmc.bookduck.domain.club.entity.ClubMember;
import com.mmc.bookduck.domain.club.entity.ClubMemberReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubMemberReadStatusRepository extends JpaRepository<ClubMemberReadStatus, Long> {
    Optional<ClubMemberReadStatus> findByClubMember(ClubMember clubMember);
}
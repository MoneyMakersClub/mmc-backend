package com.mmc.bookduck.domain.bookclub.repository;

import com.mmc.bookduck.domain.bookclub.entity.ClubMember;
import com.mmc.bookduck.domain.bookclub.entity.ClubMemberReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubMemberReadStatusRepository extends JpaRepository<ClubMemberReadStatus, Long> {
    Optional<ClubMemberReadStatus> findByClubMember(ClubMember clubMember);
}
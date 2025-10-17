package com.mmc.bookduck.domain.club.repository;

import com.mmc.bookduck.domain.club.entity.ClubMember;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.club.entity.Club;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    boolean existsByClubAndUser(Club club, User user);
    Optional<ClubMember> findByClubAndUser(Club club, User user);
    List<ClubMember> findByUser(User user);
    List<ClubMember> findByClub(Club club);
    // 락 걸지 않음
    long countByClub(Club club);
}

package com.mmc.bookduck.domain.bookclub.repository;

import com.mmc.bookduck.domain.bookclub.entity.ClubMember;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.bookclub.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    boolean existsByClubAndUser(Club club, User user);
    Optional<ClubMember> findByClubAndUser(Club club, User user);
    long countByClub(Club club);
    List<ClubMember> findByUser(User user);
    List<ClubMember> findByClub(Club club);
}

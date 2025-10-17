package com.mmc.bookduck.domain.club.repository;

import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query("SELECT c FROM Club c " +
           "JOIN c.bookInfo b " +
           "WHERE (:status IS NULL OR c.clubStatus = :status) " +
           "AND (LOWER(c.clubName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY " +
           "CASE WHEN LOWER(c.clubName) = LOWER(:keyword) THEN 1 " +
           "     WHEN LOWER(c.clubName) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2 " +
           "     WHEN LOWER(b.title) = LOWER(:keyword) THEN 3 " +
           "     WHEN LOWER(b.title) LIKE LOWER(CONCAT(:keyword, '%')) THEN 4 " +
           "     WHEN LOWER(b.author) = LOWER(:keyword) THEN 5 " +
           "     WHEN LOWER(b.author) LIKE LOWER(CONCAT(:keyword, '%')) THEN 6 " +
           "     ELSE 7 END, " +
           "SIZE(c.members) DESC, " +
           "c.createdTime DESC, " +
           "c.activeEndAt ASC")
    Page<Club> searchClubs(@Param("keyword") String keyword, 
                          @Param("status") ClubStatus status, 
                          Pageable pageable);

    @Query("SELECT c FROM Club c WHERE c.clubStatus = :status AND c.activeEndAt < :now")
    List<Club> findByClubStatusAndActiveEndAtBefore(@Param("status") ClubStatus status,
                                                    @Param("now") LocalDateTime now);

    // 최신순
    @Query("SELECT c FROM Club c " +
           "WHERE c.clubStatus = :status " +
           "AND (SELECT COUNT(cm) FROM ClubMember cm WHERE cm.club = c) < c.maxMember " +
           "ORDER BY c.createdTime DESC")
    Page<Club> findByClubStatusOrderByCreatedTimeDesc(@Param("status") ClubStatus status, Pageable pageable);

    // 인기순
    @Query(value = """
    SELECT * FROM club c
    WHERE c.club_status = :status
    AND (SELECT COUNT(*) FROM club_member cm WHERE cm.club_id = c.club_id) < c.max_member
    ORDER BY (SELECT COUNT(*) FROM club_member cm WHERE cm.club_id = c.club_id) * 1.0 / c.max_member DESC,
             c.created_time DESC
    """, nativeQuery = true)
    Page<Club> findByClubStatusOrderByPopularityDesc(@Param("status") String status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Club c where c.clubId = :clubId")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Club> findByIdForUpdate(@Param("clubId") Long clubId);
}

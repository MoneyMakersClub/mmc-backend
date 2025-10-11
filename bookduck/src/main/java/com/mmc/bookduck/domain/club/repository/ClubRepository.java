package com.mmc.bookduck.domain.club.repository;

import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query("SELECT c FROM Club c " +
           "JOIN c.bookInfo b " +
           "WHERE c.clubStatus = :status " +
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
    List<Club> findByStatusAndActiveEndAtBefore(@Param("status") ClubStatus status,
                                                @Param("now") LocalDateTime now);

    Page<Club> findByStatusOrderByCreatedAtDesc(ClubStatus status, Pageable pageable);
}

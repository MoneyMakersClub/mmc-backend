package com.mmc.bookduck.domain.archive.repository;

import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExcerptRepository extends JpaRepository<Excerpt, Long> {

    long countByUser(User user);

    long countByUserAndCreatedTimeBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Excerpt e WHERE e.user = :user AND YEAR(e.createdTime) = :currentYear")
    long countByUserAndCreatedTimeThisYear(@Param("user") User user, @Param("currentYear") int currentYear);

    List<Excerpt> findExcerptByUserBookOrderByCreatedTimeDesc(UserBook userBook);

    @Query("SELECT e FROM Excerpt e WHERE e.userBook = :userBook AND (e.visibility = 'PUBLIC') ORDER BY e.createdTime DESC")
    List<Excerpt> findExcerptsByUserBookWithPublic(@Param("userBook") UserBook userBook);

    List<Excerpt> findAllByUserAndCreatedTimeAfter(User user, LocalDateTime createdTime);

    @Query("SELECT e FROM Excerpt e " +
            "JOIN e.userBook ub " +
            "WHERE (e.excerptContent LIKE %:keyword% " +
            "OR ub.bookInfo.title LIKE %:keyword% " +
            "OR ub.bookInfo.author LIKE %:keyword%) " +
            "AND e.user = :user " +
            "ORDER BY e.createdTime DESC")
    Page<Excerpt> searchAllByExcerptContentOrBookInfoTitleOrAuthorByUserAndCreatedTimeDesc(
            @Param("keyword") String keyword,
            @Param("user") User user,
            Pageable pageable);


    @Query("SELECT e FROM Excerpt e WHERE e.user.userId = :userId")
    List<Excerpt> findByUserId(@Param("userId") Long userId);

    List<Excerpt> findTop30ByUserOrderByCreatedTimeDesc(User user);

    @Query("SELECT COUNT(e) FROM Excerpt e " +
            "WHERE e.user = :user " +
            "AND YEAR(e.createdTime) = :year " +
            "AND ((:isFirstHalf = true AND MONTH(e.createdTime) BETWEEN 1 AND 6) " +
            "OR (:isFirstHalf = false AND MONTH(e.createdTime) BETWEEN 7 AND 12))")
    long countByUserAndCreatedInYearAndHalf(@Param("user") User user, @Param("year") int year, @Param("isFirstHalf") boolean isFirstHalf);

    List<Excerpt> findAllByUserBook(UserBook userBook);

    @Query("""
        SELECT e FROM Excerpt e
        WHERE e.user.userId IN :userIds
          AND e.userBook.bookInfo.bookInfoId = :bookInfoId
          AND e.createdTime BETWEEN :start AND :end
          AND (e.visibility = 'PUBLIC' OR e.user.userId = :currentUserId)
    """)
    List<Excerpt> findClubExcerpts(Long bookInfoId, List<Long> userIds,
                                   LocalDateTime start, LocalDateTime end, Long currentUserId);
}
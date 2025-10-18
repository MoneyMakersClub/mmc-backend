package com.mmc.bookduck.domain.archive.repository;

import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUserAndReviewTitleStartingWith(User user, String reviewTitle);

    long countByUser(User user);

    long countByUserAndCreatedTimeBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.user = :user AND YEAR(r.createdTime) = :currentYear")
    long countByUserAndCreatedTimeThisYear(@Param("user") User user, @Param("currentYear") int currentYear);

    List<Review> findReviewByUserBookOrderByCreatedTimeDesc(UserBook userBook);


    @Query("SELECT r FROM Review r WHERE r.userBook = :userBook AND (r.visibility = 'PUBLIC') ORDER BY r.createdTime DESC")
    List<Review> findReviewsByUserBookWithPublic(@Param("userBook") UserBook userBook);

    List<Review> findTop30ByUserOrderByCreatedTimeDesc(User user);


    List<Review> findAllByUserAndCreatedTimeAfter(User user, LocalDateTime createdTime);

    @Query("SELECT e FROM Review e WHERE e.user.userId = :userId")
    List<Review> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.user = :user " +
            "AND YEAR(r.createdTime) = :year " +
            "AND ((:isFirstHalf = true AND MONTH(r.createdTime) BETWEEN 1 AND 6) " +
            "OR (:isFirstHalf = false AND MONTH(r.createdTime) BETWEEN 7 AND 12))")
    long countByUserAndCreatedInYearAndHalf(@Param("user") User user, @Param("year") int year, @Param("isFirstHalf") boolean isFirstHalf);

    List<Review> findAllByUserBook(UserBook userBook);

    @Query("""
        SELECT r FROM Review r
        WHERE r.user.userId IN :userIds
          AND r.userBook.bookInfo.bookInfoId = :bookInfoId
          AND r.createdTime BETWEEN :start AND :end
          AND (r.visibility = 'PUBLIC' OR r.user.userId = :currentUserId)
    """)
    List<Review> findClubReviews(Long bookInfoId, List<Long> userIds,
                                 LocalDateTime start, LocalDateTime end, Long currentUserId);
}
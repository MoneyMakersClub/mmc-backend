package com.mmc.bookduck.domain.friend.repository;

import com.mmc.bookduck.domain.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f WHERE (f.user1.userId = :senderId AND f.user2.userId = :receiverId) " +
            "OR (f.user1.userId = :receiverId AND f.user2.userId = :senderId)")
    Optional<Friend> findFriendBetweenUsers(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    List<Friend> findByUser1UserId(Long user1Id);
}

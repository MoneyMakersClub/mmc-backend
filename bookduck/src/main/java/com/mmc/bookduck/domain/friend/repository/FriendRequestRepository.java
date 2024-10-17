package com.mmc.bookduck.domain.friend.repository;

import com.mmc.bookduck.domain.friend.entity.FriendRequest;
import com.mmc.bookduck.domain.friend.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // 특정한 한 케이스이므로 Optional
    @Query("SELECT fr FROM FriendRequest fr WHERE (fr.sender.userId = :userId1 AND fr.receiver.userId = :userId2 AND fr.friendRequestStatus = :status) " +
            "OR (fr.sender.userId = :userId2 AND fr.receiver.userId = :userId1 AND fr.friendRequestStatus = :status)")
    Optional<FriendRequest> findFriendRequestBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2, @Param("status") FriendRequestStatus status);
    List<FriendRequest> findByReceiverUserIdAndFriendRequestStatus(Long receiverId, FriendRequestStatus status);
    List<FriendRequest> findBySenderUserIdAndFriendRequestStatus(Long receiverId, FriendRequestStatus status);
}

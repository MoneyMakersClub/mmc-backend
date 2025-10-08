package com.mmc.bookduck.domain.bookclub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubMemberReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clubMemberReadStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id", nullable = false)
    private ClubMember clubMember;

    @Column(nullable = false)
    private LocalDateTime lastReadAt; // 마지막으로 클럽 게시물을 읽은 시점

    // 생성자
    public ClubMemberReadStatus(ClubMember clubMember, LocalDateTime lastReadAt) {
        this.clubMember = clubMember;
        this.lastReadAt = lastReadAt;
    }

    // 읽음 시점 업데이트 메서드
    public void updateLastReadAt(LocalDateTime now) {
        this.lastReadAt = now;
    }
}

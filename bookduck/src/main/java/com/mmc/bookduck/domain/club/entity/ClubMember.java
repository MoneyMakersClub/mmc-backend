package com.mmc.bookduck.domain.club.entity;

import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ClubMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long clubMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubMemberRole clubMemberRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @OneToMany(mappedBy = "clubMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ClubMemberReadStatus> readStatuses = new ArrayList<>();

    @Builder
    public ClubMember(Club club, User user, ClubMemberRole clubMemberRole) {
        this.club = club;
        this.user = user;
        this.clubMemberRole = clubMemberRole;
    }

    public void updateRole(ClubMemberRole newRole) {
        this.clubMemberRole = newRole;
    }
}

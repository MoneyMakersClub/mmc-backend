package com.mmc.bookduck.domain.bookclub.entity;

import com.mmc.bookduck.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ClubInvite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long clubInviteId;

    @Column(unique = true, nullable = false, length = 100)
    private String inviteCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer useCount;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Builder
    public ClubInvite(String inviteCode, LocalDateTime expiresAt, Integer useCount, Boolean isActive, Club club) {
        this.inviteCode = inviteCode;
        this.expiresAt = expiresAt;
        this.useCount = useCount != null ? useCount : 0;
        this.isActive = isActive != null ? isActive : true;
        this.club = club;
    }

    public void incrementUseCount() {
        this.useCount++;
    }

    public void deactivate() {
        this.isActive = false;
    }
}

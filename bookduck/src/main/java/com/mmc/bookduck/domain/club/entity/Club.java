package com.mmc.bookduck.domain.club.entity;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Club extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long clubId;

    @Column(nullable = false)
    private String clubName;

    private String password;

    private String description;

    @Column(nullable = false)
    private LocalDateTime activeStartAt;

    @Column(nullable = false)
    private LocalDateTime activeEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubStatus clubStatus;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer maxMember;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean allowJoin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_id", nullable = false)
    private BookInfo bookInfo;

    @OneToMany(mappedBy = "club", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ClubMember> members = new ArrayList<>();

    @Builder
    public Club(String clubName, String password, String description,
                LocalDateTime activeStartAt, LocalDateTime activeEndAt,
                ClubStatus clubStatus, int maxMember, Boolean allowJoin, BookInfo bookInfo) {
        this.clubName = clubName;
        this.password = password;
        this.description = description;
        this.activeStartAt = activeStartAt;
        this.activeEndAt = activeEndAt;
        this.clubStatus = clubStatus;
        this.maxMember = maxMember;
        this.allowJoin = allowJoin != null ? allowJoin : true;
        this.bookInfo = bookInfo;
    }

    public void updateStatus(ClubStatus clubStatus) {
        this.clubStatus = clubStatus;
    }

    public void updateAllowJoin(Boolean allowJoin) {
        this.allowJoin = allowJoin;
    }
}

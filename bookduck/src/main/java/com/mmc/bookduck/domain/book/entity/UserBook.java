package com.mmc.bookduck.domain.book.entity;

import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.excerpt.entity.Excerpt;
import com.mmc.bookduck.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserBook extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long userBookId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReadStatus readStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE) // 다대일 단방향이므로 설정
    private User user;

    @Builder
    public UserBook(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }

    public void changeReadStaus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }
}
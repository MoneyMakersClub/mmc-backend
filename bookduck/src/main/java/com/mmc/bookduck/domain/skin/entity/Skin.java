package com.mmc.bookduck.domain.skin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Skin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long skinId;

    @NotNull
    private String skinName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SkinType skinType;

    @NotNull
    private String description;

    @NotNull
    private String unlockCondition; // 추후 수정 필요할 수 있음

    @Builder
    public Skin(String skinName, SkinType skinType, String description, String unlockCondition) {
        this.skinName = skinName;
        this.skinType = skinType;
        this.description = description;
        this.unlockCondition = unlockCondition;
    }
}

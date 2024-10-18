package com.mmc.bookduck.domain.item.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long itemId;

    @NotNull
    private String itemName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ItemType itemType;

    @NotNull
    private String description;

    @NotNull
    private String unlockCondition; // 추후 수정 필요할 수 있음

    @Builder
    public Item(String itemName, ItemType itemType, String description, String unlockCondition) {
        this.itemName = itemName;
        this.itemType = itemType;
        this.description = description;
        this.unlockCondition = unlockCondition;
    }
}

package com.mmc.bookduck.domain.item.repository;

import com.mmc.bookduck.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

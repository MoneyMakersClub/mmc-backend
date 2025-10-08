package com.mmc.bookduck.domain.bookclub.repository;

import com.mmc.bookduck.domain.bookclub.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {
}

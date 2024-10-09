package com.mmc.bookduck.domain.book.repository;

import com.mmc.bookduck.domain.book.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

}

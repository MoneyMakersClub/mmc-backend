package com.mmc.bookduck.domain.folder.repository;

import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.folder.entity.Folder;
import com.mmc.bookduck.domain.folder.entity.FolderBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderBookRepository extends JpaRepository<FolderBook, Long> {
    boolean existsByUserBookAndFolder(UserBook userBook, Folder folder);
}

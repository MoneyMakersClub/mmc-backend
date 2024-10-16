package com.mmc.bookduck.domain.folder.repository;

import com.mmc.bookduck.domain.folder.entity.Folder;
import com.mmc.bookduck.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findById(Long folderId);

    List<Folder> findAllByUser(User user);
}

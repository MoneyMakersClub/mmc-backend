package com.mmc.bookduck.domain.folder.dto.response;

import com.mmc.bookduck.domain.folder.entity.Folder;

public record FolderResponseDto(Long folderId, String folderName, Long userId) {
    public static FolderResponseDto from(Folder savedFolder) {
        return new FolderResponseDto(savedFolder.getFolderId(), savedFolder.getFolderName(), savedFolder.getUser().getUserId());
    }
}

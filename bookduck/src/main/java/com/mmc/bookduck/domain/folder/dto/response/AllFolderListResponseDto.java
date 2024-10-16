package com.mmc.bookduck.domain.folder.dto.response;

import com.mmc.bookduck.domain.folder.dto.common.FolderBookCoverListDto;

import java.util.List;

public record AllFolderListResponseDto(int folderCount, List<FolderBookCoverListDto> allFolderList) {
    public AllFolderListResponseDto(List<FolderBookCoverListDto> allFolderList) {
        this(allFolderList.size(), allFolderList);
    }
}

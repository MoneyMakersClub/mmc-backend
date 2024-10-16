package com.mmc.bookduck.domain.folder.dto.common;

import com.mmc.bookduck.domain.folder.entity.Folder;
import com.mmc.bookduck.domain.folder.entity.FolderBook;
import com.mmc.bookduck.domain.folder.dto.common.FolderBookCoverDto;

import java.util.ArrayList;
import java.util.List;

public record FolderBookCoverListDto(Long folderId, String folderName, List<FolderBookCoverDto> folderBookCoverList, int folderBookCount) {
    public static FolderBookCoverListDto from(Folder folder, List<FolderBook> folderBooks) {
        List<FolderBookCoverDto> coverList = new ArrayList<>();

        for (FolderBook book : folderBooks) {
            coverList.add(new FolderBookCoverDto(
                    book.getFolderBookId(),
                    book.getUserBook().getUserBookId(),
                    book.getUserBook().getBookInfo().getImgPath()
            ));
        }

        return new FolderBookCoverListDto(
                folder.getFolderId(),
                folder.getFolderName(),
                coverList,
                coverList.size()
        );
    }
}

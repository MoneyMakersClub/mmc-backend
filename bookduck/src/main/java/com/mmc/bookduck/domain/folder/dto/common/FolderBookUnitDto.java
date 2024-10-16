package com.mmc.bookduck.domain.folder.dto.common;

import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.folder.entity.FolderBook;

public record FolderBookUnitDto(Long folderBookId, Long userBookId, String title, String author, String imgPath, ReadStatus readStatus) {
    public static FolderBookUnitDto from(FolderBook folderBook) {
        return new FolderBookUnitDto(
                folderBook.getFolderBookId(),
                folderBook.getUserBook().getUserBookId(),
                folderBook.getUserBook().getBookInfo().getTitle(),
                folderBook.getUserBook().getBookInfo().getAuthor(),
                folderBook.getUserBook().getBookInfo().getImgPath(),
                folderBook.getUserBook().getReadStatus()
        );
    }
}

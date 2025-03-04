package com.mmc.bookduck.domain.book.dto.common;

import com.mmc.bookduck.domain.book.entity.BookInfo;

public record BookCoverImageUnitDto(
        Long bookInfoId,
        String imgPath,
        String title
){
    public static BookCoverImageUnitDto from(BookInfo bookInfo) {
        return new BookCoverImageUnitDto(
                bookInfo.getBookInfoId(),
                bookInfo.getImgPath(),
                bookInfo.getTitle()
        );
    }
}

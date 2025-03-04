package com.mmc.bookduck.domain.book.dto.common;

import com.mmc.bookduck.domain.book.entity.BookInfo;

public record BookCoverWithIsCustomUnitDto(
        Long bookInfoId,
        String imgPath,
        String title,
        boolean isCustom
){
    public static BookCoverWithIsCustomUnitDto from(BookInfo bookInfo, boolean isCustom) {
        return new BookCoverWithIsCustomUnitDto(
                bookInfo.getBookInfoId(),
                bookInfo.getImgPath(),
                bookInfo.getTitle(),
                isCustom
        );
    }
}

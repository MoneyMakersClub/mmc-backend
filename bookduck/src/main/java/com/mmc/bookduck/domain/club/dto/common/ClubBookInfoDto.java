package com.mmc.bookduck.domain.club.dto.common;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClubBookInfoDto (
        @Schema(description = "책 정보 ID") Long bookInfoId,
        @Schema(description = "책 표지 이미지 경로") String bookImgPath,
        @Schema(description = "책 제목") String bookTitle,
        @Schema(description = "책 저자") String bookAuthor,
        @Schema(description = "커스텀책 여부") Boolean isCustom
) {
    /**
     * Create a ClubBookInfoDto from a BookInfo by mapping its fields and deriving the `isCustom` flag.
     *
     * @param bookInfo the source BookInfo to map values from
     * @return a ClubBookInfoDto with `bookInfoId`, `bookImgPath`, `bookTitle`, and `bookAuthor` copied from the source; `isCustom` is `true` if `bookInfo.getCreatedUserId()` is not null, `false` otherwise
     */
    public static ClubBookInfoDto from(BookInfo bookInfo) {
        return ClubBookInfoDto.builder()
                .bookInfoId(bookInfo.getBookInfoId())
                .bookImgPath(bookInfo.getImgPath())
                .bookTitle(bookInfo.getTitle())
                .bookAuthor(bookInfo.getAuthor())
                .isCustom(bookInfo.getCreatedUserId()!=null)
                .build();
    }
}
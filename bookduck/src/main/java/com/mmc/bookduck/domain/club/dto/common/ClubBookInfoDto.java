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
        @Schema(description = "커스텀책 여부") Boolean isCustom,
        @Schema(description = "외부 API 제공 ID (커스텀책은 null)") String providerId
) {
    public static ClubBookInfoDto from(BookInfo bookInfo) {
        return ClubBookInfoDto.builder()
                .bookInfoId(bookInfo.getBookInfoId())
                .bookImgPath(bookInfo.getImgPath())
                .bookTitle(bookInfo.getTitle())
                .bookAuthor(bookInfo.getAuthor())
                .isCustom(bookInfo.getCreatedUserId()!=null)
                .providerId(bookInfo.getProviderId())
                .build();
    }
}

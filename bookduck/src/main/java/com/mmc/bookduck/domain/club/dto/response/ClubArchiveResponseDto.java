package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.archive.dto.response.ExcerptResponseDto;
import com.mmc.bookduck.domain.archive.dto.response.ReviewResponseDto;
import com.mmc.bookduck.domain.archive.entity.ArchiveType;
import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;

public record ClubArchiveResponseDto(
        @Schema(description = "게시물 타입: EXCERPT / REVIEW") ArchiveType type,
        @Schema(description = "작성자 ID") Long userId,
        @Schema(description = "작성자 닉네임") String nickname,
        @Schema(description = "읽지 않은 게시물 여부") boolean isUnread,
        @Schema(description = "게시물 상세 정보 (ExcerptResponseDto 또는 ReviewResponseDto)") Object data
) {
    public static ClubArchiveResponseDto fromExcerpt(Excerpt e, boolean isUnread) {
        return new ClubArchiveResponseDto(
                ArchiveType.EXCERPT,
                e.getUser().getUserId(),
                e.getUser().getNickname(),
                isUnread,
                ExcerptResponseDto.from(e)
        );
    }

    public static ClubArchiveResponseDto fromReview(Review r, boolean isUnread) {
        return new ClubArchiveResponseDto(
                ArchiveType.REVIEW,
                r.getUser().getUserId(),
                r.getUser().getNickname(),
                isUnread,
                ReviewResponseDto.from(r)
        );
    }
}

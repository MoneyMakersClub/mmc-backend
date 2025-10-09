package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClubArchiveResponseDto(
        @Schema(description = "게시물 ID (excerptId or reviewId)") Long id,
        @Schema(description = "게시물 타입: EXCERPT / REVIEW") String type,
        @Schema(description = "작성자 ID") Long userId,
        @Schema(description = "작성자 닉네임") String nickname,
        @Schema(description = "내용 (발췌 또는 리뷰)") String content,
        @Schema(description = "리뷰 제목 (발췌는 null)") String title,
        @Schema(description = "작성 시각") LocalDateTime createdTime,
        @Schema(description = "읽지 않은 게시물 여부") boolean isUnread,
        @Schema(description = "공개 여부: PUBLIC / PRIVATE") String visibility
) {
    public static ClubArchiveResponseDto fromExcerpt(Excerpt e, boolean isUnread) {
        return ClubArchiveResponseDto.builder()
                .id(e.getExcerptId())
                .type("EXCERPT")
                .userId(e.getUser().getUserId())
                .nickname(e.getUser().getNickname())
                .content(e.getExcerptContent())
                .title(null)
                .createdTime(e.getCreatedTime())
                .isUnread(isUnread)
                .visibility(e.getVisibility().name())
                .build();
    }

    public static ClubArchiveResponseDto fromReview(Review r, boolean isUnread) {
        return ClubArchiveResponseDto.builder()
                .id(r.getReviewId())
                .type("REVIEW")
                .userId(r.getUser().getUserId())
                .nickname(r.getUser().getNickname())
                .content(r.getReviewContent())
                .title(r.getReviewTitle())
                .createdTime(r.getCreatedTime())
                .isUnread(isUnread)
                .visibility(r.getVisibility().name())
                .build();
    }
}

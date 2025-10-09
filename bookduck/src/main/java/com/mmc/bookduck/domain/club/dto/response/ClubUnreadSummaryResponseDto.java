package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.global.common.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClubUnreadSummaryResponseDto(
        @Schema(description = "읽지 않은 게시물 개수") int unreadCount,
        @Schema(description = "최근 게시물 타입") String latestType,
        @Schema(description = "최근 게시물 ID") Long latestId,
        @Schema(description = "최근 게시물 내용") String latestContent,
        @Schema(description = "최근 게시물 작성 시각") LocalDateTime latestCreatedAt
) {
    public static ClubUnreadSummaryResponseDto from(int unreadCount, BaseTimeEntity latest) {
        if (latest instanceof Excerpt e) {
            return new ClubUnreadSummaryResponseDto(
                    unreadCount, "EXCERPT", e.getExcerptId(),
                    e.getExcerptContent(), e.getCreatedTime()
            );
        } else if (latest instanceof Review r) {
            return new ClubUnreadSummaryResponseDto(
                    unreadCount, "REVIEW", r.getReviewId(),
                    r.getReviewContent(), r.getCreatedTime()
            );
        } else {
            return new ClubUnreadSummaryResponseDto(unreadCount, null, null, null, null);
        }
    }
}

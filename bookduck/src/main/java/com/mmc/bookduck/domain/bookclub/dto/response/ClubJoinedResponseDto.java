package com.mmc.bookduck.domain.bookclub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClubJoinedResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "읽을 책 제목") String bookTitle,
        @Schema(description = "현재 클럽 인원수") long memberCount,
        @Schema(description = "읽지 않은 게시물 개수") int unreadCount,
        @Schema(description = "최근 게시물 정보") LatestPost latestPost
) {
    @Builder
    public record LatestPost(
            @Schema(description = "게시물 타입") String type,
            @Schema(description = "게시물 ID") Long id,
            @Schema(description = "게시물 내용") String content,
            @Schema(description = "작성 시간") LocalDateTime createdAt
    ) {}
}

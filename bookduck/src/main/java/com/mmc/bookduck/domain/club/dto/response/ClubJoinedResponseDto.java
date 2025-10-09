package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.club.dto.common.ClubBookInfoDto;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClubJoinedResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "책") ClubBookInfoDto clubBookInfoDto,
        @Schema(description = "현재 가입 인원") long memberCount,
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

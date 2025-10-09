package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.domain.bookclub.entity.Club;
import com.mmc.bookduck.domain.bookclub.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 검색 응답 DTO")
public record ClubSearchResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "현재 가입 인원") Integer currentMemberCount,
        @Schema(description = "최대 가입 인원") Integer maxMember,
        @Schema(description = "활동 시작 시각") LocalDateTime activeStartAt,
        @Schema(description = "활동 종료 시각") LocalDateTime activeEndAt,
        @Schema(description = "책 정보 ID") Long bookInfoId,
        @Schema(description = "책 표지 이미지 경로") String bookImgPath,
        @Schema(description = "책 제목") String bookTitle,
        @Schema(description = "책 저자") String bookAuthor
) {
    public static ClubSearchResponseDto from(Club club) {
        return ClubSearchResponseDto.builder()
                .clubId(club.getClubId())
                .clubStatus(club.getClubStatus())
                .clubName(club.getClubName())
                .currentMemberCount(club.getMembers().size())
                .maxMember(club.getMaxMember())
                .activeStartAt(club.getActiveStartAt())
                .activeEndAt(club.getActiveEndAt())
                .bookInfoId(club.getBookInfo().getBookInfoId())
                .bookImgPath(club.getBookInfo().getImgPath())
                .bookTitle(club.getBookInfo().getTitle())
                .bookAuthor(club.getBookInfo().getAuthor())
                .build();
    }
}


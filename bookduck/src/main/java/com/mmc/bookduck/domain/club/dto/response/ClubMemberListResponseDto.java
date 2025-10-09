package com.mmc.bookduck.domain.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "클럽 멤버 목록 응답 DTO")
public record ClubMemberListResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "총 멤버 수") Integer totalMembers,
        @Schema(description = "멤버 목록") List<ClubMemberResponseDto> members
) {
    public static ClubMemberListResponseDto from(Long clubId, String clubName, List<ClubMemberResponseDto> members) {
        return ClubMemberListResponseDto.builder()
                .clubId(clubId)
                .clubName(clubName)
                .totalMembers(members.size())
                .members(members)
                .build();
    }
}

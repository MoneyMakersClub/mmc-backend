package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.domain.bookclub.entity.Club;
import com.mmc.bookduck.domain.bookclub.entity.ClubMember;
import com.mmc.bookduck.domain.bookclub.entity.ClubMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 가입 응답 DTO")
public record ClubJoinResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "클럽 소개") String description,
        @Schema(description = "멤버 역할") ClubMemberRole role,
        @Schema(description = "가입 시각") LocalDateTime joinedAt
) {
    public static ClubJoinResponseDto from(Club club, ClubMember member) {
        return ClubJoinResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .description(club.getDescription())
                .role(member.getClubMemberRole())
                .joinedAt(member.getCreatedTime())
                .build();
    }
}

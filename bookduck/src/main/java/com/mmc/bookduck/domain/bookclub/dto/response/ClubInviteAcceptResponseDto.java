package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.domain.bookclub.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "클럽 초대 수락 응답 DTO")
public record ClubInviteAcceptResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "가입한 역할 (LEADER/MEMBER)") String role
) {
    public static ClubInviteAcceptResponseDto from(Club club, ClubMember member) {
        return ClubInviteAcceptResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .role(member.getClubMemberRole().name())
                .build();
    }
}

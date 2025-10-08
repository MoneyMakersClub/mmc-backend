package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.domain.bookclub.entity.Club;
import com.mmc.bookduck.domain.bookclub.entity.ClubInvite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "초대 코드 검증 응답 DTO")
public record ClubInviteValidationResponseDto(
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "클럽 소개") String description,
        @Schema(description = "현재 인원 수") int currentMemberCount,
        @Schema(description = "최대 인원") int maxMember,
        @Schema(description = "초대 활성 상태") Boolean isActive
) {
    public static ClubInviteValidationResponseDto from(Club club, ClubInvite invite) {
        return ClubInviteValidationResponseDto.builder()
                .clubName(club.getClubName())
                .description(club.getDescription())
                .currentMemberCount(club.getMembers().size())
                .maxMember(club.getMaxMember())
                .isActive(invite.getIsActive())
                .build();
    }
}

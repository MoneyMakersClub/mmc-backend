package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.domain.bookclub.entity.Club;
import com.mmc.bookduck.domain.bookclub.entity.ClubInvite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
@Schema(description = "클럽 생성 응답 DTO")
public record ClubCreateResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "초대 코드") String inviteCode,
        @Schema(description = "초대 만료 시각") LocalDateTime expiresAt
) {
    public static ClubCreateResponseDto from(Club club, ClubInvite invite) {
        return ClubCreateResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .inviteCode(invite.getInviteCode())
                .expiresAt(invite.getExpiresAt())
                .build();
    }
}

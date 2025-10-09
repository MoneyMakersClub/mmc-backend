package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.club.entity.Club;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 생성 응답 DTO")
public record ClubCreateResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "클럽 소개") String description,
        @Schema(description = "가입 허용 여부") Boolean allowJoin,
        @Schema(description = "최대 인원") Integer maxMember,
        @Schema(description = "생성 시각") LocalDateTime createdAt
) {
    public static ClubCreateResponseDto from(Club club) {
        return ClubCreateResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .description(club.getDescription())
                .allowJoin(club.getAllowJoin())
                .maxMember(club.getMaxMember())
                .createdAt(club.getCreatedTime())
                .build();
    }
}

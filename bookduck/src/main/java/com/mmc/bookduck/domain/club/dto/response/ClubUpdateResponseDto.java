package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "클럽 정보 수정 응답 DTO")
public record ClubUpdateResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "클럽 소개") String description,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "최대 가입 인원") Integer maxMember,
        @Schema(description = "활동 시작 날짜") LocalDate activeStartDate,
        @Schema(description = "활동 종료 날짜") LocalDate activeEndDate,
        @Schema(description = "가입 허용 여부") Boolean allowJoin,
        @Schema(description = "비밀번호 설정 여부") Boolean hasPassword
) {
    public static ClubUpdateResponseDto from(Club club) {
        return ClubUpdateResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .description(club.getDescription())
                .clubStatus(club.getClubStatus())
                .maxMember(club.getMaxMember())
                .activeStartDate(LocalDate.from(club.getActiveStartAt()))
                .activeEndDate(LocalDate.from(club.getActiveEndAt()))
                .allowJoin(club.getAllowJoin())
                .hasPassword(club.getPassword() != null && !club.getPassword().isEmpty())
                .build();
    }
}

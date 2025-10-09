package com.mmc.bookduck.domain.club.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 정보 수정 요청 DTO")
public record ClubUpdateRequestDto(
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "비밀번호 (선택)") String password,
        @Schema(description = "클럽 소개 (선택)") String description,
        @Schema(description = "활동 시작 시각") LocalDateTime activeStartAt,
        @Schema(description = "활동 종료 시각") LocalDateTime activeEndAt,
        @Schema(description = "최대 인원") Integer maxMember,
        @Schema(description = "가입 허용 여부") Boolean allowJoin
) {}

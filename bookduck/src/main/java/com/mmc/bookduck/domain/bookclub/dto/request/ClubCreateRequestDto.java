package com.mmc.bookduck.domain.bookclub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 생성 요청 DTO")
public record ClubCreateRequestDto(
        @Schema(description = "클럽명", example = "이달의 책 모임") @NotBlank String clubName,
        @Schema(description = "비밀번호 (선택)") String password,
        @Schema(description = "클럽 소개 (선택)") String description,
        @Schema(description = "활동 시작 시각") @NotNull LocalDateTime activeStartAt,
        @Schema(description = "활동 종료 시각") @NotNull LocalDateTime activeEndAt,
        @Schema(description = "책 정보 ID") @NotNull Long bookInfoId,
        @Schema(description = "최대 인원", example = "10") @Min(1) int maxMember
) {}

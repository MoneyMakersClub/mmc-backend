package com.mmc.bookduck.domain.bookclub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "클럽 가입 요청 DTO")
public record ClubJoinRequestDto(
        @Schema(description = "클럽 비밀번호 (클럽에 비밀번호가 설정된 경우 필수)") String password
) {}

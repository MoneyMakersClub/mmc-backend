package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.global.common.PaginatedResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record ClubArchiveListResponseDto(
        @Schema(description = "북클럽 내 게시물 목록") PaginatedResponseDto<ClubArchiveResponseDto> archives
) {
    public static ClubArchiveListResponseDto from(Page<ClubArchiveResponseDto> page) {
        return ClubArchiveListResponseDto.builder()
                .archives(PaginatedResponseDto.from(page))
                .build();
    }
}

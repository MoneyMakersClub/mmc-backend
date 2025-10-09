package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.global.common.PaginatedResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
@Schema(description = "클럽 검색 목록 응답 DTO")
public record ClubSearchListResponseDto(
        @Schema(description = "검색된 클럽 목록") PaginatedResponseDto<ClubSearchResponseDto> clubs
) {
    public static ClubSearchListResponseDto from(Page<ClubSearchResponseDto> clubPage) {
        return ClubSearchListResponseDto.builder()
                .clubs(PaginatedResponseDto.from(clubPage))
                .build();
    }
}


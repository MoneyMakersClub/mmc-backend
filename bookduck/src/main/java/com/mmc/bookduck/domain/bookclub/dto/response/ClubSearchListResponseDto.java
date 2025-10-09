package com.mmc.bookduck.domain.bookclub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
@Schema(description = "클럽 검색 목록 응답 DTO")
public record ClubSearchListResponseDto(
        @Schema(description = "검색된 클럽 목록") Page<ClubSearchResponseDto> clubs,
        @Schema(description = "전체 검색 결과 수") long totalElements,
        @Schema(description = "전체 페이지 수") int totalPages,
        @Schema(description = "현재 페이지 번호") int currentPage,
        @Schema(description = "페이지 크기") int pageSize
) {
    public static ClubSearchListResponseDto from(Page<ClubSearchResponseDto> clubPage) {
        return ClubSearchListResponseDto.builder()
                .clubs(clubPage)
                .totalElements(clubPage.getTotalElements())
                .totalPages(clubPage.getTotalPages())
                .currentPage(clubPage.getNumber())
                .pageSize(clubPage.getSize())
                .build();
    }
}


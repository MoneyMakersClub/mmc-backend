package com.mmc.bookduck.domain.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "가입한 클럽 목록 응답 DTO")
public record ClubJoinedListResponseDto(
        @Schema(description = "클럽 목록") List<ClubJoinedResponseDto> clubs
){
    public static ClubJoinedListResponseDto from(List<ClubJoinedResponseDto> result) {
        return ClubJoinedListResponseDto.builder().clubs(result).build();
    }
}

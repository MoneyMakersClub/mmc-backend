package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.club.dto.common.ClubBookInfoDto;
import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "클럽 검색 응답 DTO")
public record ClubSearchResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "책") ClubBookInfoDto clubBookInfo,
        @Schema(description = "현재 가입 인원") Integer memberCount,
        @Schema(description = "최대 가입 인원") Integer maxMember,
        @Schema(description = "활동 시작 날짜") LocalDate activeStartDate,
        @Schema(description = "활동 종료 날짜") LocalDate activeEndDate,
        @Schema(description = "현재 사용자 가입 여부") Boolean isJoined
) {
    public static ClubSearchResponseDto from(Club club, BookInfo bookInfo, Integer clubMemberCount, Boolean isJoined) {
        return new ClubSearchResponseDto(
                club.getClubId(),
                club.getClubStatus(),
                club.getClubName(),
                ClubBookInfoDto.from(bookInfo),
                clubMemberCount,
                club.getMaxMember(),
                LocalDate.from(club.getActiveStartAt()),
                LocalDate.from(club.getActiveEndAt()),
                isJoined
        );
    }
}


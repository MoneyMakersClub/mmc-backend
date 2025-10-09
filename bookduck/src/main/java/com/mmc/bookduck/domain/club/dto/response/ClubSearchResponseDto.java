package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.club.dto.common.ClubBookInfoDto;
import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubMember;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.awt.print.Book;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "클럽 검색 응답 DTO")
public record ClubSearchResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "책") ClubBookInfoDto clubBookInfoDto,
        @Schema(description = "현재 가입 인원") Integer memberCount,
        @Schema(description = "최대 가입 인원") Integer maxMember,
        @Schema(description = "활동 시작 날짜") LocalDate activeStartDate,
        @Schema(description = "활동 종료 날짜") LocalDate activeEndDate
) {
    public static ClubSearchResponseDto from(Club club,  BookInfo bookInfo, Integer clubMemberCount) {
        return ClubSearchResponseDto.builder()
                .clubId(club.getClubId())
                .clubStatus(club.getClubStatus())
                .clubName(club.getClubName())
                .memberCount(clubMemberCount)
                .maxMember(club.getMaxMember())
                .activeStartDate(LocalDate.from(club.getActiveStartAt()))
                .activeEndDate(LocalDate.from(club.getActiveEndAt()))
                .clubBookInfoDto(ClubBookInfoDto.from(bookInfo))
                .build();
    }
}


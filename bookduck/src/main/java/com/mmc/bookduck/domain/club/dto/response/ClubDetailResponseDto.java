package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.club.dto.common.ClubBookInfoDto;
import com.mmc.bookduck.domain.club.dto.common.ClubMemberSummaryDto;
import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClubDetailResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "클럽 소개") String description,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "클럽 가입 인원") Integer memberCount,
        @Schema(description = "최대 가입 인원") Integer maxMember,
        @Schema(description = "활동 시작 날짜") LocalDate activeStartDate,
        @Schema(description = "활동 종료 날짜") LocalDate activeEndDate,
        @Schema(description = "가입 허용 여부") Boolean allowJoin,
        @Schema(description = "비밀번호 설정 여부") Boolean hasPassword,
        @Schema(description = "클럽 생성 시각") LocalDateTime createdAt,
        @Schema(description = "책") ClubBookInfoDto clubBookInfo,
        @Schema(description = "현재 사용자의 멤버 여부") Boolean isMember,
        @Schema(description = "현재 사용자의 멤버 역할") String memberRole,
        @Schema(description = "현재 사용자의 UserBook ID (없으면 null)") Long userBookId,
        @Schema(description = "현재 사용자의 책 읽기 상태") ReadStatus readStatus,
        @Schema(description = "클럽 멤버 목록 (ID, 닉네임)") List<ClubMemberSummaryDto> members
) {
    public static ClubDetailResponseDto from(Club club, BookInfo bookInfo, Integer memberCount, boolean isMember, String memberRole, Long userBookId, ReadStatus readStatus, List<ClubMemberSummaryDto> members) {
        return new ClubDetailResponseDto(
                club.getClubId(),
                club.getClubName(),
                club.getDescription(),
                club.getClubStatus(),
                memberCount,
                club.getMaxMember(),
                LocalDate.from(club.getActiveStartAt()),
                LocalDate.from(club.getActiveEndAt()),
                club.getAllowJoin(),
                club.getPassword() != null && !club.getPassword().isEmpty(),
                club.getCreatedTime(),
                ClubBookInfoDto.from(bookInfo),
                isMember,
                memberRole,
                userBookId,
                readStatus,
                members
        );
    }
}

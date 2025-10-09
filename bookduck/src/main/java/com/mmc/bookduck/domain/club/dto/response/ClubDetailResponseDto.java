package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 상세 정보 응답 DTO")
public record ClubDetailResponseDto(
        @Schema(description = "클럽 ID") Long clubId,
        @Schema(description = "클럽명") String clubName,
        @Schema(description = "클럽 소개") String description,
        @Schema(description = "클럽 상태") ClubStatus clubStatus,
        @Schema(description = "현재 가입 인원") Integer currentMemberCount,
        @Schema(description = "최대 가입 인원") Integer maxMember,
        @Schema(description = "활동 시작 시각") LocalDateTime activeStartAt,
        @Schema(description = "활동 종료 시각") LocalDateTime activeEndAt,
        @Schema(description = "가입 허용 여부") Boolean allowJoin,
        @Schema(description = "비밀번호 설정 여부") Boolean hasPassword,
        @Schema(description = "클럽 생성 시각") LocalDateTime createdAt,
        @Schema(description = "책 정보 ID") Long bookInfoId,
        @Schema(description = "책 표지 이미지 경로") String bookImgPath,
        @Schema(description = "책 제목") String bookTitle,
        @Schema(description = "책 저자") String bookAuthor,
        @Schema(description = "책 출판사") String bookPublisher,
        @Schema(description = "책 출간일") String bookPublishDate,
        @Schema(description = "책 설명") String bookDescription,
        @Schema(description = "현재 사용자의 멤버 여부") Boolean isMember,
        @Schema(description = "현재 사용자의 멤버 역할") String memberRole
) {
    public static ClubDetailResponseDto from(Club club, boolean isMember, String memberRole) {
        return ClubDetailResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .description(club.getDescription())
                .clubStatus(club.getClubStatus())
                .currentMemberCount(club.getMembers().size())
                .maxMember(club.getMaxMember())
                .activeStartAt(club.getActiveStartAt())
                .activeEndAt(club.getActiveEndAt())
                .allowJoin(club.getAllowJoin())
                .hasPassword(club.getPassword() != null && !club.getPassword().isEmpty())
                .createdAt(club.getCreatedTime())
                .bookInfoId(club.getBookInfo().getBookInfoId())
                .bookImgPath(club.getBookInfo().getImgPath())
                .bookTitle(club.getBookInfo().getTitle())
                .bookAuthor(club.getBookInfo().getAuthor())
                .bookPublisher(club.getBookInfo().getPublisher())
                .bookPublishDate(club.getBookInfo().getPublishDate())
                .bookDescription(club.getBookInfo().getDescription())
                .isMember(isMember)
                .memberRole(memberRole)
                .build();
    }
}

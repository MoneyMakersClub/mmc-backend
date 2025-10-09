package com.mmc.bookduck.domain.club.dto.response;

import com.mmc.bookduck.domain.club.entity.ClubMember;
import com.mmc.bookduck.domain.club.entity.ClubMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "클럽 멤버 정보 응답 DTO")
public record ClubMemberResponseDto(
        @Schema(description = "멤버 ID") Long memberId,
        @Schema(description = "사용자 ID") Long userId,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "멤버 역할") ClubMemberRole role,
        @Schema(description = "가입 시각") LocalDateTime joinedAt
) {
    public static ClubMemberResponseDto from(ClubMember member) {
        return ClubMemberResponseDto.builder()
                .memberId(member.getClubMemberId())
                .userId(member.getUser().getUserId())
                .nickname(member.getUser().getNickname())
                .role(member.getClubMemberRole())
                .joinedAt(member.getCreatedTime())
                .build();
    }
}

package com.mmc.bookduck.domain.club.dto.common;

import com.mmc.bookduck.domain.club.entity.ClubMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클럽 멤버 요약 정보")
public record ClubMemberSummaryDto(
        @Schema(description = "멤버 ID") Long memberId,
        @Schema(description = "닉네임") String nickname
) {
    public static ClubMemberSummaryDto from(ClubMember member) {
        return new ClubMemberSummaryDto(
                member.getClubMemberId(),
                member.getUser().getNickname()
        );
    }
}

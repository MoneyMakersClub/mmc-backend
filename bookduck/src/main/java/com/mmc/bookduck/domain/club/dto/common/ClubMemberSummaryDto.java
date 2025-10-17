package com.mmc.bookduck.domain.club.dto.common;

import com.mmc.bookduck.domain.club.entity.ClubMember;

public record ClubMemberSummaryDto(
        Long memberId,
         String nickname
) {
    public static ClubMemberSummaryDto from(ClubMember member) {
        return new ClubMemberSummaryDto(
                member.getClubMemberId(),
                member.getUser().getNickname()
        );
    }
}


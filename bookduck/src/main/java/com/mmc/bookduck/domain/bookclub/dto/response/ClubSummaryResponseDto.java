package com.mmc.bookduck.domain.bookclub.dto.response;

import com.mmc.bookduck.domain.bookclub.entity.Club;
import lombok.Builder;

@Builder
public record ClubSummaryResponseDto(
        Long clubId,
        String clubName,
        String bookTitle,
        String author,
        String thumbnail,
        int unreadCount
) {
    public static ClubSummaryResponseDto from(Club club, Long unreadCount) {
        return ClubSummaryResponseDto.builder()
                .clubId(club.getClubId())
                .clubName(club.getClubName())
                .bookTitle(club.getBookInfo().getTitle())
                .author(club.getBookInfo().getAuthor())
                .thumbnail(club.getBookInfo().getImgPath())
                .unreadCount(unreadCount.intValue())
                .build();
    }
}

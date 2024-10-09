package com.mmc.bookduck.domain.book.dto.common;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookRatingUnitDto {
    private String nickname;
    private double rating;
    private String oneLine;

    @Builder
    public BookRatingUnitDto(String nickname, double rating, String oneLine){
        this.nickname = nickname;
        this.rating = rating;
        this.oneLine = oneLine;
    }
}

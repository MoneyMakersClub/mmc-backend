package com.mmc.bookduck.domain.book.dto.response;

import com.mmc.bookduck.domain.book.dto.common.AdditionalBookInfoDto;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasicBookInfoResponseDto {
    private String title;
    private List<String> author;
    private String imgPath;
    private String providerId;

    private Double ratingAverage;
    private ReadStatus readStatus;

    private String myOneLine;
    private Double myRating;

    private AdditionalBookInfoDto additionalBookInfoDto;


    @Builder
    public BasicBookInfoResponseDto(String title, List<String> author, String imgPath, String providerId, Double ratingAverage,
                                    String myOneLine, Double myRating, ReadStatus readStatus, AdditionalBookInfoDto additionalBookInfoDto) {
        this.title = title;
        this.author = author;
        this.imgPath = imgPath;
        this.providerId = providerId;
        this.ratingAverage = ratingAverage;
        this.myRating = myRating;
        this.myOneLine = myOneLine;
        this.readStatus = readStatus;
        this.additionalBookInfoDto = additionalBookInfoDto;
    }

}

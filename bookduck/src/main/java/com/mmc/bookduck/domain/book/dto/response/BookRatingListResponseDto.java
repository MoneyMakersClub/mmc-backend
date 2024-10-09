package com.mmc.bookduck.domain.book.dto.response;

import com.mmc.bookduck.domain.book.dto.common.BookRatingUnitDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookRatingListResponseDto {

    List<BookRatingUnitDto> list;

    @Builder
    public BookRatingListResponseDto(List<BookRatingUnitDto> list){
        this.list = list;
    }
}

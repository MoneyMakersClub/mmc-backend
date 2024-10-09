package com.mmc.bookduck.domain.book.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBookListResponseDto {
    private int bookCount;
    private List<UserBookResponseDto> bookList;

    @Builder
    public UserBookListResponseDto(List<UserBookResponseDto> bookList){
        this.bookList = bookList;
        this.bookCount = (bookList != null) ? bookList.size() : 0;
    }
}

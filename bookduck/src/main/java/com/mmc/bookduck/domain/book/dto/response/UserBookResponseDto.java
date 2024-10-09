package com.mmc.bookduck.domain.book.dto.response;

import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBookResponseDto {
    private Long userBookId;
    private String title;
    private String author;
    private String imgPath;
    private ReadStatus readStatus;
    private Long bookInfoId;

    @Builder
    public UserBookResponseDto(Long userBookId, String title, String author,
                               String imgPath, ReadStatus readStatus, Long bookInfoId){
        this.userBookId = userBookId;
        this.title = title;
        this.author = author;
        this.imgPath = imgPath;
        this.readStatus = readStatus;
        this.bookInfoId = bookInfoId;
    }

    public static UserBookResponseDto from(UserBook userBook) {
        return UserBookResponseDto.builder()
                .userBookId(userBook.getUserBookId())
                .title(userBook.getBookInfo().getTitle())
                .author(userBook.getBookInfo().getAuthor())
                .imgPath(userBook.getBookInfo().getImgPath())
                .readStatus(userBook.getReadStatus())
                .bookInfoId(userBook.getBookInfo().getBookInfoId())
                .build();
    }
}

package com.mmc.bookduck.domain.book.dto.request;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.Genre;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBookRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private List<String> authors;

    private String publisher; // null 가능

    private String publishDate; // null 가능, 연도만 제공될 수 있음

    private String description;

    private List<String> category;

    private Long pageCount;

    private String imgPath;

    private String language;

    @NotBlank
    private String providerId;

    public UserBook toEntity(User user, BookInfo bookInfo, ReadStatus readStatus) {
        return UserBook.builder()
                .readStatus(readStatus)
                .user(user)
                .bookInfo(bookInfo)
                .build();
    }

    public BookInfo toEntity(String author, Genre genre){
        return BookInfo.builder()
                .providerId(this.getProviderId())
                .title(this.getTitle())
                .author(author)
                .publisher(this.getPublisher())
                .publishDate(this.getPublishDate())
                .description(this.getDescription())
                .category(this.getCategory().getFirst())
                .pageCount(this.getPageCount())
                .imgPath(this.getImgPath())
                .language(this.getLanguage())
                .genre(genre)
                .build();
    }
}


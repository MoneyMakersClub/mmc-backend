package com.mmc.bookduck.domain.book.dto.common;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookListInfoUnitDto {
    @NotNull
    private String title;

    private List<String> author;

    private String imgPath;

    private String providerId;

    @Builder
    public BookListInfoUnitDto(String title, List<String> author, String imgPath, String providerId) {
        this.title = title;
        this.author = author;
        this.imgPath = imgPath;
        this.providerId = providerId;
    }
}

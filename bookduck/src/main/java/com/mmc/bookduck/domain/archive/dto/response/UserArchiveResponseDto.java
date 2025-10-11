package com.mmc.bookduck.domain.archive.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

import com.mmc.bookduck.domain.archive.entity.ArchiveType;
import org.springframework.data.domain.Page;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserArchiveResponseDto(
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        List<ArchiveWithType> archiveList,
        List<ArchiveWithoutTitleAuthor> userBookArchiveList
) {
    public record ArchiveWithType(
            ArchiveType type, // EXCERPT, REVIEW
            Object data, // ExcerptResponseDto, ReviewResponseDto
            Long archiveId,
            String title,
            String author
    ) {}
    public record ArchiveWithoutTitleAuthor(
            ArchiveType type, // EXCERPT, REVIEW
            Object data, // ExcerptResponseDto, ReviewResponseDto
            Long archiveId
    ) {}
    public static UserArchiveResponseDto from(Page<ArchiveWithType> archivePage) {
        return new UserArchiveResponseDto(
                archivePage.getNumber(),
                archivePage.getSize(),
                archivePage.getTotalElements(),
                archivePage.getTotalPages(),
                archivePage.getContent(),
                null
        );
    }
    public static UserArchiveResponseDto fromWithoutTitleAuthor(Page<ArchiveWithoutTitleAuthor> archivePage) {
        return new UserArchiveResponseDto(
                archivePage.getNumber(),
                archivePage.getSize(),
                archivePage.getTotalElements(),
                archivePage.getTotalPages(),
                null,
                archivePage.getContent()
        );
    }
}

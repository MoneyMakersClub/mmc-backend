package com.mmc.bookduck.domain.book.repository;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {
    BookInfo findByProviderId(String providerId);

    // providerId가 null이고, createdUserId와 제목 또는 저자가 검색어를 포함하는 책 검색 쿼리
    @Query("SELECT b FROM BookInfo b WHERE b.providerId IS NULL AND b.createdUserId = :createdUserId AND (b.title LIKE %:keyword% OR b.author LIKE %:keyword%)")
    List<BookInfo> searchByCreatedUserIdAndKeyword(@Param("createdUserId") Long createdUserId, @Param("keyword") String keyword);
}

package com.mmc.bookduck.domain.book.repository;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByBookInfo(BookInfo bookInfo);

    UserBook findByUserAndBookInfo(User user, BookInfo bookInfo);
}

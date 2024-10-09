package com.mmc.bookduck.domain.book.controller;

import com.mmc.bookduck.domain.book.dto.request.UserBookRequestDto;
import com.mmc.bookduck.domain.book.dto.response.UserBookResponseDto;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.service.UserBookService;
import com.mmc.bookduck.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class UserBookController {

    private final UserBookService userBookService;

    // 서재에 책 추가
    @PostMapping
    public ResponseEntity<UserBookResponseDto> addUserBook(@RequestBody UserBookRequestDto dto,
                                                           @RequestParam(name="status") final String status){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userBookService.addUserBook(dto, status));
    }

    // 서재에서 책 삭제
    @DeleteMapping("/{userBookId}")
    public ResponseEntity<String> deleteUserBook(@PathVariable final Long userBookId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.deleteUserBook(userBookId));
    }

    // 서재 책 상태 변경
    @PatchMapping("/{userBookId}")
    public ResponseEntity<UserBookResponseDto> updateUserBookStatus(@PathVariable final Long userBookId,
                                                                    @RequestParam(name = "status") final String status){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.updateUserBookStatus(userBookId, status));
    }
}


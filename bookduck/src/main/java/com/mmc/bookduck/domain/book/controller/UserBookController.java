package com.mmc.bookduck.domain.book.controller;

import com.mmc.bookduck.domain.book.dto.common.BookCoverWithIsCustomUnitDto;
import com.mmc.bookduck.domain.book.dto.request.AddCustomBookRequestDto;
import com.mmc.bookduck.domain.book.dto.request.RatingRequestDto;
import com.mmc.bookduck.domain.book.dto.response.BookListResponseDto;
import com.mmc.bookduck.domain.book.dto.response.CustomBookResponseDto;
import com.mmc.bookduck.domain.book.dto.response.RatingResponseDto;
import com.mmc.bookduck.domain.book.dto.response.UserBookListResponseDto;
import com.mmc.bookduck.domain.book.dto.response.UserBookResponseDto;
import com.mmc.bookduck.domain.book.service.UserBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Books", description = "Books 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class UserBookController {

    private final UserBookService userBookService;

    @Operation(summary = "서재에서 책 삭제", description = "사용자의 서재에서 책을 삭제합니다.")
    @DeleteMapping("/{userBookId}")
    public ResponseEntity<Void> deleteUserBook(@PathVariable final Long userBookId){
        userBookService.deleteUserBook(userBookId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @Operation(summary = "서재 책 상태 변경", description = "사용자의 서재의 책 상태를 변경합니다.")
    @PatchMapping("/{userBookId}")
    public ResponseEntity<UserBookResponseDto> updateUserBookStatus(@PathVariable final Long userBookId,
                                                                    @RequestParam(name = "status") final String status){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.updateUserBookStatus(userBookId, status));
    }


    @Operation(summary = "서재 책 목록 조회", description = "사용자의 서재 책 전체 목록을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<UserBookListResponseDto> getAllUserBook(@RequestParam(name = "sort") final String sort){

        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.getAllUserBook(sort));
    }

    @Operation(summary = "상태별 서재 책 목록 조회", description = "사용자의 서재 책 목록을 상태별로 조회합니다.")
    @GetMapping("/filter")
    public ResponseEntity<UserBookListResponseDto> getStatusUserBook(@RequestParam(name = "status") final List<String> statusList,
                                                                     @RequestParam(name = "sort") final String sort){

        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.getStatusUserBook(statusList, sort));
    }


    /*
    @Operation(summary = "서재 책 상세-기본 정보 조회", description = "사용자의 서재 책의 기본 정보를 상세 조회합니다.(책 기본정보 + 현재 사용자의 별점&한줄평)")
    @GetMapping("/{userbookId}")
    public ResponseEntity<BookInfoBasicResponseDto> getUserBookInfoBasic(@PathVariable(name = "userbookId") final Long userbookId){

        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.getUserBookInfoBasic(userbookId));
    }


    @Operation(summary = "서재 책 상세-추가 정보 조회", description = "사용자의 서재 책의 추가 정보를 상세 조회합니다.(현재 책에 대한 다른 사용자들의 별점&한줄평 목록 3개)")
    @GetMapping("/{userbookId}/additional")
    public ResponseEntity<BookInfoAdditionalResponseDto> getUserBookInfoAdditional(@PathVariable(name = "userbookId") final Long userbookId){

        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.getUserBookInfoAdditional(userbookId));
    }
    */


    @Operation(summary = "책 직접 등록", description = "사용자가 책을 직접 등록합니다.")
    @PostMapping(value = "/custom", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CustomBookResponseDto> createCustomBook(@Valid @ModelAttribute final AddCustomBookRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body((userBookService.createCustomBook(requestDto)));
    }


    //별점 등록
    @Operation(summary = "별점 등록(수정)", description = "서재 책의 별점을 등록(수정)합니다.")
    @PatchMapping("/{userbookId}/rating")
    public ResponseEntity<RatingResponseDto> ratingUserBook(@PathVariable(name = "userbookId") final Long userbookId,
                                                            @Valid @RequestBody final RatingRequestDto dto){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.ratingUserBook(userbookId, dto));
    }

    //별점 삭제
    @Operation(summary = "별점 삭제", description = "서재 책의 별점을 삭제합니다.")
    @DeleteMapping("/{userbookId}/rating")
    public ResponseEntity<Void> deleteRating(@PathVariable(name = "userbookId") final Long userbookId){
        userBookService.deleteRating(userbookId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "검색페이지-최근 기록한책 목록 조회", description = "검색페이지 - 최근 기록한 책 3개를 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<BookListResponseDto<BookCoverWithIsCustomUnitDto>> getRecentRecordBooks(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userBookService.getRecentRecordBooks());
    }
}
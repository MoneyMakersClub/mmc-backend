package com.mmc.bookduck.domain.book.service;

import com.mmc.bookduck.domain.book.dto.request.UserBookRequestDto;
import com.mmc.bookduck.domain.book.dto.response.UserBookResponseDto;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.book.repository.UserBookRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.repository.UserRepository;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBookService {

    private final BookInfoService bookInfoService;
    private final UserBookRepository userBookRepository;


    // 임시 User
    private final UserRepository userRepository;

    public User findUser(){
        User user = userRepository.findById(1L)
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));
        return user;
    }
    //


    // 서재에 책 추가
    public UserBookResponseDto addUserBook(UserBookRequestDto dto) {
        try {
            ReadStatus.valueOf(dto.getReadStatus());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }

        Optional<BookInfo> bookInfo = bookInfoService.findBookInfoByProviderId(dto.getProviderId());

        if(bookInfo.isPresent()){
            Optional<UserBook> userBook = userBookRepository.findByUserAndBookInfo(findUser(), bookInfo.get());

            if(userBook.isPresent()){
                throw new CustomException(ErrorCode.USERBOOK_ALREADY_EXISTS);
            }
            UserBook newUserBook = dto.toEntity(findUser(), bookInfo.get(), ReadStatus.valueOf(dto.getReadStatus()));
            UserBook savedUserBook = userBookRepository.save(newUserBook);
            return UserBookResponseDto.from(savedUserBook);
        }
        else{
            // bookInfo 없으면 먼저 bookInfo 저장
            BookInfo newBookInfo = bookInfoService.saveApiBookInfo(dto);

            UserBook newUserBook = dto.toEntity(findUser(),newBookInfo, ReadStatus.valueOf(dto.getReadStatus()));
            UserBook savedUserBook = userBookRepository.save(newUserBook);
            return UserBookResponseDto.from(savedUserBook);
        }
    }

    // 서재에서 책 삭제
    public String deleteUserBook(Long userBookId) {
        UserBook userBook = findUserBookById(userBookId);

        //임시 User
        Long userId = findUser().getUserId();

        // 권한체크
        if(userBook.getUser().getUserId().equals(userId)){

            BookInfo bookInfo = userBook.getBookInfo();
            Long createdUserId = bookInfo.getCreatedUserId();
            // 사용자가 직접 등록한 책이면 bookInfo도 같이 삭제
            if(createdUserId != null && createdUserId.equals(userId)){
                bookInfoService.deleteBookInfo(bookInfo.getBookInfoId());
            }
            userBookRepository.delete(userBook);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
        return "서재에서 책이 삭제되었습니다.";
    }

    // 서재 책 상태 변경
    public UserBookResponseDto updateUserBookStatus(Long userBookId, String status) {
        try {
            ReadStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }

        UserBook userBook = findUserBookById(userBookId);

        //임시 User
        Long userId = findUser().getUserId();

        // 권한체크
        if(userBook.getUser().getUserId().equals(userId)){
            userBook.changeReadStatus(ReadStatus.valueOf(status));

            return UserBookResponseDto.from(userBook);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }

    public UserBook findUserBookById(Long userBookId){
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(()-> new CustomException(ErrorCode.USERBOOK_NOT_FOUND));
        return userBook;
    }
}


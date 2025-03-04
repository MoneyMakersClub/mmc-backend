package com.mmc.bookduck.domain.book.service;

import com.mmc.bookduck.domain.archive.dto.request.ArchiveCreateRequestDto;
import com.mmc.bookduck.domain.archive.dto.request.ExcerptCreateRequestDto;
import com.mmc.bookduck.domain.archive.dto.request.ReviewCreateRequestDto;
import com.mmc.bookduck.domain.archive.entity.Archive;
import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.domain.archive.repository.ArchiveRepository;
import com.mmc.bookduck.domain.archive.repository.ExcerptRepository;
import com.mmc.bookduck.domain.archive.repository.ReviewRepository;
import com.mmc.bookduck.domain.book.dto.common.BookCoverWithIsCustomUnitDto;
import com.mmc.bookduck.domain.book.dto.request.AddCustomBookRequestDto;
import com.mmc.bookduck.domain.book.dto.request.RatingRequestDto;
import com.mmc.bookduck.domain.book.dto.response.*;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.book.repository.UserBookRepository;
import com.mmc.bookduck.domain.folder.service.FolderBookService;
import com.mmc.bookduck.domain.oneline.entity.OneLine;
import com.mmc.bookduck.domain.oneline.repository.OneLineRepository;
import com.mmc.bookduck.domain.onelineLike.entity.OneLineLike;
import com.mmc.bookduck.domain.onelineLike.repository.OneLineLikeRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.common.BaseTimeEntity;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserBookService {
    private final BookInfoService bookInfoService;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final ExcerptRepository excerptRepository;
    private final ReviewRepository reviewRepository;
    private final FolderBookService folderBookService;
    private final ArchiveRepository archiveRepository;
    private final OneLineRepository oneLineRepository;
    private final OneLineLikeRepository oneLineLikeRepository;

    //customBook 추가
    public UserBook createCustomBookEntity(AddCustomBookRequestDto requestDto) {
        User user = userService.getCurrentUser();
        BookInfo bookInfo = bookInfoService.saveCustomBookInfo(requestDto, user);
        UserBook userBook = new UserBook(ReadStatus.NOT_STARTED, user, bookInfo);
        return userBookRepository.save(userBook);
    }

    public CustomBookResponseDto createCustomBook(AddCustomBookRequestDto requestDto) {
        UserBook userBook = createCustomBookEntity(requestDto);
        return new CustomBookResponseDto(userBook, 0.0,null, null, true);
    }

    private UserBookResponseDto convertToUserBookResponseDto(UserBook userBook) {
        boolean isCustomBook = (userBook.getBookInfo().getCreatedUserId() != null);
        return new UserBookResponseDto(userBook, isCustomBook);
    }

    public UserBook getUserBookOrAdd(ExcerptCreateRequestDto excerptDto, ReviewCreateRequestDto reviewDto, ArchiveCreateRequestDto archiveDto, String providerId) {
        // ExcerptDto 또는 ReviewDto에서 userBookId를 확인
        Long userBookId = (excerptDto != null && excerptDto.getUserBookId() != null)
                ? excerptDto.getUserBookId()
                : (reviewDto != null ? reviewDto.getUserBookId() : null);
        if (userBookId != null) {
            return getUserBookById(userBookId);
        }
        if (archiveDto.getUserBook() != null && providerId != null) {
            return bookInfoService.addBookByProviderId(providerId, archiveDto.getUserBook());
        } else if (archiveDto.getCustomBook() != null) {
            return createCustomBookEntity(archiveDto.getCustomBook());
        } else {
            throw new CustomException(ErrorCode.USERBOOK_NOT_FOUND);
        }
    }

    // 서재에서 책 삭제
    public void deleteUserBook(Long userBookId) {
        UserBook userBook = getUserBookById(userBookId);
        User user = userService.getCurrentUser();

        // 권한체크
        if(userBook.getUser().getUserId().equals(user.getUserId())){

            BookInfo bookInfo = userBook.getBookInfo();
            Long createdUserId = bookInfo.getCreatedUserId();

            //폴더북 삭제
            folderBookService.deleteFolderBookByUserBook(userBook);

            //review 삭제
            List<Review> reviewList = reviewRepository.findAllByUserBook(userBook);
            for(Review review : reviewList){
                Optional<Archive> archive = archiveRepository.findByReview_ReviewId(review.getReviewId());
                archive.ifPresent(archiveRepository::delete);
                reviewRepository.delete(review);
            }
            //excerpt 삭제
            List<Excerpt> excerptList = excerptRepository.findAllByUserBook(userBook);
            for(Excerpt excerpt : excerptList){
                Optional<Archive> archive = archiveRepository.findByExcerpt_ExcerptId(excerpt.getExcerptId());
                archive.ifPresent(archiveRepository::delete);
                excerptRepository.delete(excerpt);
            }

            //한줄평 삭제
            Optional<OneLine> oneLine = oneLineRepository.findByUserBook(userBook);
            oneLine.ifPresent(line -> {
                List<OneLineLike> oneLineLikes = oneLineLikeRepository.findAllByOneLine(line);

                if (!oneLineLikes.isEmpty()) {
                    oneLineLikeRepository.deleteAll(oneLineLikes); // 좋아요 삭제
                }

                oneLineRepository.delete(line);
            });

            userBookRepository.delete(userBook);
            // 사용자가 직접 등록한 책이면 bookInfo도 같이 삭제
            if(createdUserId != null && createdUserId.equals(user.getUserId())){
                bookInfoService.deleteCustomBookInfo(bookInfo.getBookInfoId());
            }
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }

    // 서재 책 상태 변경
    public UserBookResponseDto updateUserBookStatus(Long userBookId, String status) {
        try {
            ReadStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }

        UserBook userBook = getUserBookById(userBookId);

        User user = userService.getCurrentUser();

        // 권한체크
        if (!userBook.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
        userBook.changeReadStatus(ReadStatus.valueOf(status));
        // 경험치, 뱃지
        bookInfoService.checkExpAndBadgeForFinishedBook(userBook);
        return convertToUserBookResponseDto(userBook);
    }

    @Transactional(readOnly = true)
    public UserBook getUserBookById(Long userBookId){
        return userBookRepository.findById(userBookId)
                .orElseThrow(()-> new CustomException(ErrorCode.USERBOOK_NOT_FOUND));
    }

    // 서재 책 전체 조회
    public UserBookListResponseDto getAllUserBook(String sort) {
        User user = userService.getCurrentUser();
        List<UserBook> userBookList = sortUserBook(user, sort);

        List<UserBookResponseDto> dtos = userBookList.stream()
                .map(this::convertToUserBookResponseDto)
                .toList();

        return new UserBookListResponseDto(dtos);
    }

    // 서재 책 상태별 조회
    public UserBookListResponseDto getStatusUserBook(List<String> statusList, String sort) {

        if (statusList.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }

        List<ReadStatus> readStatusList = validateReadStatus(statusList);
        User user = userService.getCurrentUser();

        // 필터링 및 정렬된 책 리스트 생성
        List<UserBook> userBookList = sortUserBook(user, sort).stream()
                .filter(userBook -> readStatusList.contains(userBook.getReadStatus())) // 상태 필터링
                .toList();

        // DTO 변환
        List<UserBookResponseDto> dtos = userBookList.stream()
                .map(this::convertToUserBookResponseDto)
                .toList();

        return new UserBookListResponseDto(dtos);
    }

    /*
    // 서재책 상세보기 - 기본정보
    @Transactional(readOnly = true)
    public BookInfoBasicResponseDto getUserBookInfoBasic(Long userBookId) {
        UserBook userBook = getUserBookById(userBookId);

        String koreanGenreName = genreService.genreNameToKorean(userBook.getBookInfo().getGenre());
        BookInfoDetailDto detailDto = new BookInfoDetailDto(userBook.getBookInfo(), koreanGenreName);

        OneLine oneLine = oneLineRepository.findByUserBook(userBook)
                .orElse(null);
        Double ratingAverage = bookInfoService.getRatingAverage(userBook.getBookInfo());

        if(oneLine != null){
            BookUnitDto unitDto = BookUnitDto.from(userBook);
            return BookInfoBasicResponseDto.from(userBook, ratingAverage, oneLine.getOneLineContent(), detailDto);
        }
        else{
            BookUnitDto unitDto = BookUnitDto.from(userBook);
            return BookInfoBasicResponseDto.from(userBook, ratingAverage, null, detailDto);
        }
    }
    */


    /*
    // 서재 책 상세보기 - 추가정보
    @Transactional(readOnly = true)
    public BookInfoAdditionalResponseDto getUserBookInfoAdditional(Long userBookId) {
        UserBook userBook = getUserBookById(userBookId);
        List<UserBook> sameBookInfo_userBookList = findAllUserBookByBookInfo(userBook.getBookInfo());

        List<BookRatingUnitDto> oneLineList = new ArrayList<>();
        if (!sameBookInfo_userBookList.isEmpty()) {
            for (UserBook book : sameBookInfo_userBookList) {
                //내 userBook 제외
                if (!book.equals(userBook)) {
                    Optional<OneLine> oneLine =  oneLineRepository.findByUserBook(book);
                    if(oneLine.isPresent()){
                        oneLineList.add(new BookRatingUnitDto(oneLine.get(), book));
                    }
                    if(oneLineList.size() == 3){
                        break;
                    }
                }
            }
        }
        return new BookInfoAdditionalResponseDto(oneLineList);
    }
    */


    @Transactional(readOnly = true)
    public List<UserBook> sortUserBook(User user, String sort){

        if(sort.equals("latest")){
            return userBookRepository.findAllByUserOrderByCreatedTimeDesc(user);
        }else if(sort.equals("rating_high")){
            return userBookRepository.findByUserOrderByRatingDesc(user);
        }else if(sort.equals("rating_low")){
            return userBookRepository.findByUserOrderByRatingAsc(user);
        }else if(sort.equals("title")){
            return userBookRepository.findAllByUserOrderByTitle(user);
        }else{
            throw new CustomException(ErrorCode.ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<UserBook> findAllUserBookByBookInfo(BookInfo bookInfo){
        return userBookRepository.findAllByBookInfo(bookInfo);
    }

    @Transactional(readOnly = true)
    public List<UserBook> findAllByUser(User user) {
        return userBookRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public long countFinishedUserBooksByUser(User user) {
        return userBookRepository.countByUserAndReadStatus(user, ReadStatus.FINISHED);
    }

    @Transactional
    public RatingResponseDto ratingUserBook(Long userbookId, RatingRequestDto dto) {
        if ((dto.rating() % 0.5) != 0.0){
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        UserBook userBook = getUserBookById(userbookId);
        userBook.changeRating(dto.rating());

        return RatingResponseDto.from(userBook);
    }

    @Transactional
    public void deleteRating(Long userbookId) {
        UserBook userBook = getUserBookById(userbookId);
        userBook.changeRating(0.0);
    }

    @Transactional(readOnly = true)
    public BookListResponseDto<BookCoverWithIsCustomUnitDto> getRecentRecordBooks() {
        User user = userService.getCurrentUser();
        LocalDateTime monthsAgo = LocalDateTime.now().minusMonths(3);

        // 세 달 이내
        List<Excerpt> excerpts = excerptRepository.findAllByUserAndCreatedTimeAfter(user, monthsAgo);
        List<Review> reviews = reviewRepository.findAllByUserAndCreatedTimeAfter(user, monthsAgo);

        List<BaseTimeEntity> allItems = new ArrayList<>();
        allItems.addAll(excerpts);
        allItems.addAll(reviews);

        allItems.sort((item1, item2) -> item2.getCreatedTime().compareTo(item1.getCreatedTime()));

        List<UserBook> userBookList = new ArrayList<>();
        for (Object item : allItems) {
            if (userBookList.size() > 3 || userBookList.size() == 3) break;

            if (item instanceof Excerpt excerpt) {
                UserBook userBook = excerpt.getUserBook();
                if (!userBookList.contains(userBook)) {
                    userBookList.add(userBook);
                }
            }
            else if (item instanceof Review review) {
                UserBook userBook = review.getUserBook();
                if (!userBookList.contains(userBook)) {
                    userBookList.add(userBook);
                }
            }
        }
        List<BookCoverWithIsCustomUnitDto> coverList = new ArrayList<>();
        for(UserBook userBook : userBookList){
            BookInfo bookInfo = userBook.getBookInfo();
            boolean isCustom = bookInfo.getCreatedUserId() != null;
            coverList.add(BookCoverWithIsCustomUnitDto.from(userBook.getBookInfo(), isCustom));
        }
        return new BookListResponseDto<>(coverList);
    }

    @Transactional(readOnly = true)
    public void validateUserBookOwner(UserBook userBook) {
        User currentUser = userService.getCurrentUser();
        if(!userBook.getUser().getUserId().equals(currentUser.getUserId())){
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }

    public List<ReadStatus> validateReadStatus(List<String> statusList){
        List<ReadStatus> readStatusList = new ArrayList<>();
        try {
            for(String status : statusList){
                readStatusList.add(ReadStatus.valueOf(status));
            }
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }
        return readStatusList;
    }
}
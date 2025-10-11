package com.mmc.bookduck.domain.book.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmc.bookduck.domain.archive.dto.response.ExcerptResponseDto;
import com.mmc.bookduck.domain.archive.dto.response.ReviewResponseDto;
import com.mmc.bookduck.domain.archive.dto.response.UserArchiveResponseDto;
import com.mmc.bookduck.domain.archive.dto.response.UserArchiveResponseDto.ArchiveWithoutTitleAuthor;
import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.domain.archive.repository.ExcerptRepository;
import com.mmc.bookduck.domain.archive.repository.ReviewRepository;
import com.mmc.bookduck.domain.archive.service.ArchiveService;
import com.mmc.bookduck.domain.badge.service.BadgeUnlockService;
import com.mmc.bookduck.domain.book.dto.common.BookCoverImageUnitDto;
import com.mmc.bookduck.domain.book.dto.common.BookUnitParseDto;
import com.mmc.bookduck.domain.book.dto.common.MyRatingOneLineReadStatusDto;
import com.mmc.bookduck.domain.book.dto.request.AddUserBookRequestDto;
import com.mmc.bookduck.domain.book.dto.request.AddCustomBookRequestDto;
import com.mmc.bookduck.domain.book.dto.request.CustomBookUpdateDto;
import com.mmc.bookduck.domain.book.dto.response.AddUserBookResponseDto;
import com.mmc.bookduck.domain.book.dto.common.BookUnitDto;
import com.mmc.bookduck.domain.book.dto.common.BookInfoDetailDto;
import com.mmc.bookduck.domain.book.dto.response.BookInfoBasicResponseDto;
import com.mmc.bookduck.domain.book.dto.response.BookListResponseDto;
import com.mmc.bookduck.domain.book.dto.response.BookUnitResponseDto;
import com.mmc.bookduck.domain.book.dto.response.CustomBookResponseDto;
import com.mmc.bookduck.domain.book.dto.common.CustomBookUnitDto;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.Genre;
import com.mmc.bookduck.domain.book.entity.GenreName;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.book.repository.BookInfoRepository;
import com.mmc.bookduck.domain.book.repository.UserBookRepository;
import com.mmc.bookduck.domain.friend.repository.FriendRepository;
import com.mmc.bookduck.domain.item.service.ItemUnlockService;
import com.mmc.bookduck.domain.oneline.dto.response.OneLineRatingListResponseDto;
import com.mmc.bookduck.domain.oneline.dto.response.OneLineRatingUnitDto;
import com.mmc.bookduck.domain.oneline.entity.OneLine;
import com.mmc.bookduck.domain.oneline.repository.OneLineRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserGrowthService;
import com.mmc.bookduck.global.S3.S3Service;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import com.mmc.bookduck.global.google.GoogleBooksApiService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.mmc.bookduck.domain.archive.entity.ArchiveType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookInfoService {
    private final BookInfoRepository bookInfoRepository;
    private final UserBookRepository userBookRepository;
    private final ReviewRepository reviewRepository;
    private final ExcerptRepository excerptRepository;
    private final ArchiveService archiveService;
    private final GenreService genreService;
    private final GoogleBooksApiService googleBooksApiService;
    private final UserService userService;
    private final OneLineRepository oneLineRepository;
    private final S3Service s3Service;
    private final FriendRepository friendRepository;
    private final BadgeUnlockService badgeUnlockService;
    private final UserGrowthService userGrowthService;
    private final ItemUnlockService itemUnlockService;

    // api 도서 목록 조회
    public BookListResponseDto<BookUnitResponseDto> searchBookList(String keyword, Long page, Long size) {
        User user = userService.getCurrentUser();
        String responseBody = googleBooksApiService.searchBookList(keyword, page, size);
        int totalBooks = parseTotalBooks(responseBody);

        List<BookUnitParseDto> bookInfoList = parseBookInfo(responseBody);
        List<BookUnitResponseDto> bookResponseList = new ArrayList<>();
        for(BookUnitParseDto bookUnit : bookInfoList){
            BookInfo bookInfo = findBookInfoByProviderId(bookUnit.providerId())
                    .orElse(null);

            if(bookInfo != null){
                MyRatingOneLineReadStatusDto myRatingOneLine = getMyRatingOneLineReadStatus(bookInfo, user);
                BookUnitDto unitDto = BookUnitDto.from(bookUnit, myRatingOneLine, bookInfo.getBookInfoId());
                BookUnitResponseDto responseDto = new BookUnitResponseDto(bookUnit.providerId(), unitDto);
                bookResponseList.add(responseDto);
            }
            else{
                BookUnitDto unitDto = BookUnitDto.from(bookUnit);
                BookUnitResponseDto responseDto = new BookUnitResponseDto(bookUnit.providerId(), unitDto);
                bookResponseList.add(responseDto);
            }
        }
        int totalPage = (int) Math.ceil((double) totalBooks / size);
        return new BookListResponseDto<>(bookResponseList, totalPage, page);
    }

    // 목록 정보 파싱
    public List<BookUnitParseDto> parseBookInfo(String apiResult){
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(apiResult);
            JsonNode itemsNode = rootNode.path("items");

            List<BookUnitParseDto> bookList = new ArrayList<>();

            for(JsonNode itemNode : itemsNode) {
                // providerId
                String providerId = itemNode.get("id").asText();

                JsonNode info = itemNode.get("volumeInfo");
                // title
                String title = getTextNode(info, "title");
                // author
                JsonNode authorsNode = info.get("authors");
                String author = null;
                if (authorsNode != null && authorsNode.isArray() && authorsNode.size() > 0) {
                    author = authorsNode.get(0).asText();
                }
                // image
                String imgPath;
                JsonNode imageLink = info.get("imageLinks");
                if (imageLink != null && imageLink.has("thumbnail")) {
                    imgPath = imageLink.get("thumbnail").asText();
                } else if (imageLink != null && imageLink.has("smallThumbnail")){
                    imgPath = imageLink.get("smallThumbnail").asText();
                } else {
                    imgPath = null;
                }
                bookList.add(new BookUnitParseDto(title, author, imgPath, providerId));
            }
            return bookList;

        }catch(Exception e){
            throw new CustomException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    public int parseTotalBooks(String apiResult){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(apiResult);
            int totalItems = rootNode.path("totalItems").asInt();
            return totalItems;
        }catch(Exception e){
            throw new CustomException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    private String getTextNode(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asText();
        }
        return null; // 필드가 없을 경우 null
    }

    //api 도서 기본 정보 조회
    public BookInfoBasicResponseDto getApiBookBasicByProviderId(String providerId) {
        String responseBody = googleBooksApiService.searchOneBook(providerId);
        BookInfoDetailDto additional = parseBookDetail(responseBody);

        Optional<BookInfo> bookInfo = bookInfoRepository.findByProviderId(providerId);
        if(bookInfo.isPresent()){
            return getApiBookBasicByBookInfoId(bookInfo.get().getBookInfoId());
        }
        else{
            BookUnitDto bookUnitDto = parseBookBasic(responseBody);
            return BookInfoBasicResponseDto.from(providerId, bookUnitDto, additional);
        }
    }

    // 기본 정보 파싱
    private BookInfoDetailDto parseBookDetail(String responseBody) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode info = rootNode.get("volumeInfo");

            // publisher
            String publisher = getTextNode(info, "publisher");
            // publishedDate
            String publishedDate = getTextNode(info, "publishedDate");
            // description
            String description = getTextNode(info, "description");
            // page
            Long page = info.get("pageCount").asLong(0);

            // categories
            JsonNode cateNode = info.get("categories");
            List<String> cate = new ArrayList<>();
            if (cateNode != null && cateNode.isArray()) {
                for (JsonNode c : cateNode) {
                    cate.add(c.asText());
                }
            } else {
                //카테고리가 없으면, null로 설정
                cate = null;
            }
            //장르 매칭
            Genre genre = genreService.matchGenre(cate);
            String koreanGenre = genreService.genreNameToKorean(genre);

            String language = getTextNode(info, "language");
            return new BookInfoDetailDto(publisher, publishedDate, description, page, cate, genre.getGenreId(), koreanGenre, language);

        }catch(Exception e){
            throw new CustomException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    private BookUnitDto parseBookBasic(String responseBody) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode info = rootNode.get("volumeInfo");

            // title
            String title = getTextNode(info, "title");
            // authors
            JsonNode authorsNode = info.get("authors");
            String author = null;
            if (authorsNode != null && authorsNode.isArray() && authorsNode.size() > 0) {
                author = authorsNode.get(0).asText();
            }
            // image
            String imgPath;
            JsonNode imageLink = info.get("imageLinks");
            if (imageLink != null && imageLink.has("thumbnail")) {
                imgPath = imageLink.get("thumbnail").asText();
            } else if (imageLink != null && imageLink.has("smallThumbnail")){
                imgPath = imageLink.get("smallThumbnail").asText();
            } else {
                imgPath = null;
            }
            return new BookUnitDto(null, null, title, author, imgPath, null, null);

        }catch(Exception e){
            throw new CustomException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Optional<BookInfo> findBookInfoByProviderId(String providerId) {
        return bookInfoRepository.findByProviderId(providerId);
    }

    // custom bookInfo 삭제
    public void deleteCustomBookInfo(Long bookInfoId) {
        BookInfo bookInfo = getBookInfoById(bookInfoId);
        if(bookInfo.getImgPath() != null){
            s3Service.deleteFile(bookInfo.getImgPath());
        }
        bookInfoRepository.delete(bookInfo);
    }

    public BookInfo saveCustomBookInfo (AddCustomBookRequestDto dto, User user) {
        String imgPath = null;
        if(dto.coverImage() != null){
            imgPath = s3Service.uploadFile(dto.coverImage());
        }
        Genre genre = genreService.findOrCreateGenreByGenreName(GenreName.valueOf("OTHERS"));
        BookInfo bookInfo = dto.toEntity(imgPath, genre, user.getUserId());
        return bookInfoRepository.save(bookInfo);
    }

    // custom book 목록 검색
    @Transactional(readOnly = true)
    public BookListResponseDto<CustomBookUnitDto> searchCustomBookList(String keyword) {

        User user = userService.getCurrentUser();
        List<BookInfo> bookInfoList = bookInfoRepository.searchByCreatedUserIdAndKeyword(user.getUserId(), keyword);

        List<CustomBookUnitDto> dtos = new ArrayList<>();
        for(BookInfo bookInfo : bookInfoList){
            MyRatingOneLineReadStatusDto dto = getMyRatingOneLineReadStatus(bookInfo, user);
            dtos.add(CustomBookUnitDto.from(bookInfo, dto));
        }
        return new BookListResponseDto<>(dtos);
    }

    @Transactional(readOnly = true)
    public BookInfoBasicResponseDto getApiBookBasicByBookInfoId(Long bookInfoId) {
        User user = userService.getCurrentUser();

        BookInfo bookInfo = getBookInfoById(bookInfoId);
        if(bookInfo.getProviderId() == null){
            throw new CustomException(ErrorCode.BOOKINFO_BAD_REQUEST);
        }
        Optional<UserBook> userBook = userBookRepository.findByUserAndBookInfo(user, bookInfo);

        MyRatingOneLineReadStatusDto my = getMyRatingOneLineReadStatus(bookInfo, user);
        String koreanGenreName = genreService.genreNameToKorean(bookInfo.getGenre());
        BookInfoDetailDto additional = new BookInfoDetailDto(bookInfo, koreanGenreName);
        BookUnitDto bookUnitDto = BookUnitDto.from(bookInfo, my);

        if(userBook.isPresent()){
            return BookInfoBasicResponseDto.from(bookInfo.getProviderId(), bookUnitDto, getRatingAverage(bookInfo),my.oneLineId(), my.myOneLine(), additional);
        }else{
            return new BookInfoBasicResponseDto(bookInfo.getProviderId(), bookUnitDto, getRatingAverage(bookInfo), null,null, additional);
        }
    }

    // custom 기본 정보
    @Transactional(readOnly = true)
    public CustomBookResponseDto getCustomBookBasic(Long bookInfoId) {
        User user = userService.getCurrentUser();

        BookInfo bookInfo = getBookInfoById(bookInfoId);
        if(bookInfo.getCreatedUserId() == null){
            throw new CustomException(ErrorCode.CUSTOM_BOOKINFO_NOT_FOUND);
        }
        User bookInfoCreaterUser = userService.getActiveUserByUserId(bookInfo.getCreatedUserId());
        UserBook userBook = getUserBookByUserAndBookInfo(bookInfo, bookInfoCreaterUser);

        if(bookInfo.getCreatedUserId().equals(user.getUserId())){ // 내 customBook
            MyRatingOneLineReadStatusDto myRatingOneLine = getMyRatingOneLineReadStatus(bookInfo, user);
            return new CustomBookResponseDto(userBook, myRatingOneLine.myRating(),myRatingOneLine.oneLineId(), myRatingOneLine.myOneLine(), true);
        }else if(isFriend(user,bookInfoCreaterUser)){ //친구 customBook
            return new CustomBookResponseDto(userBook, false);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST); //내것도 아니고 친구것도 아닌 경우
        }
    }

    @Transactional(readOnly = true)
    public Double getRatingAverage(BookInfo bookInfo) {
        double totalRating = 0.0;
        int count = 0;

        List<UserBook> userBookList = userBookRepository.findAllByBookInfo(bookInfo);
        if(userBookList.isEmpty()){
            return null;
        }

        for(UserBook book : userBookList){
            if (book.getRating() != 0.0) {
                totalRating += book.getRating();
                count++;
            }
        }

        if(count > 0){
            double average = totalRating / count;
            BigDecimal roundedAverage = new BigDecimal(average).setScale(1, RoundingMode.HALF_UP);
            return roundedAverage.doubleValue();
        }else{
            return null;
        }
    }

    @Transactional
    public CustomBookResponseDto updateCustomBookInfo(Long bookInfoId, CustomBookUpdateDto dto) {
        BookInfo bookInfo = getBookInfoById(bookInfoId);
        User user = userService.getCurrentUser();

        if(bookInfo.getCreatedUserId() != null){
            if(bookInfo.getCreatedUserId().equals(user.getUserId())){
                if (dto.title() != null) {
                    bookInfo.setTitle(dto.title());
                }
                if (dto.author() != null) {
                    bookInfo.setAuthor(dto.author());
                }
                if(dto.coverImage() != null){
                    String newImgPath = s3Service.uploadFile(dto.coverImage());
                    s3Service.deleteFile(bookInfo.getImgPath());
                    bookInfo.setImgPath(newImgPath);
                }
            }else{
                throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
            }
        }else{
            throw new CustomException(ErrorCode.CUSTOM_BOOKINFO_NOT_FOUND);
        }

        UserBook userBook = userBookRepository.findByUserAndBookInfo(user, bookInfo)
                .orElseThrow(()-> new CustomException(ErrorCode.USERBOOK_NOT_FOUND));

        MyRatingOneLineReadStatusDto ratingDto = getMyRatingOneLineReadStatus(bookInfo, user);
        return new CustomBookResponseDto(userBook, ratingDto.myRating(), ratingDto.oneLineId(),ratingDto.myOneLine(), true);
    }

    @Transactional(readOnly = true)
    public MyRatingOneLineReadStatusDto getMyRatingOneLineReadStatus(BookInfo bookInfo, User user) {
        UserBook userBook = userBookRepository.findByUserAndBookInfo(user, bookInfo)
                .orElse(null);
        if (userBook == null) {
            return MyRatingOneLineReadStatusDto.defaultInstance();
        } else {
            OneLine oneLine = oneLineRepository.findByUserBook(userBook)
                    .orElse(null);
            if (oneLine == null) {
                return MyRatingOneLineReadStatusDto.from(userBook);
            } else {
                return MyRatingOneLineReadStatusDto.from(userBook, oneLine);
            }
        }
    }

    @Transactional(readOnly = true)
    public BookInfo getBookInfoById(Long bookInfoId){
        BookInfo bookInfo = bookInfoRepository.findById(bookInfoId)
                .orElseThrow(()-> new CustomException(ErrorCode.BOOKINFO_NOT_FOUND));
        return bookInfo;
    }

    // 한줄평&별점 조회
    @Transactional(readOnly = true)
    public OneLineRatingListResponseDto getOneLineList(Long bookInfoId, String orderBy, Pageable pageable) {
        BookInfo bookInfo = getBookInfoById(bookInfoId);
        Page<OneLine> oneLinePage;
        switch (orderBy.toLowerCase()) {
            case "likes":
                oneLinePage = oneLineRepository.findByBookInfoOrderByOneLineLikesDesc(bookInfo, pageable);
                break;
            case "latest":
                oneLinePage = oneLineRepository.findByBookInfoOrderByCreatedTimeDesc(bookInfo, pageable);
                break;
            case "highest":
                oneLinePage = oneLineRepository.findByBookInfoOrderByRatingDesc(bookInfo, pageable);
                break;
            case "lowest":
                oneLinePage = oneLineRepository.findByBookInfoOrderByRatingAsc(bookInfo, pageable);
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        User currentUser = userService.getCurrentUser();
        Page<OneLineRatingUnitDto> dtoPage = oneLinePage.map(oneLine -> {
            Boolean isLiked = oneLine.getOneLineLikes().stream()
                    .anyMatch(like -> like.getUser().getUserId().equals(currentUser.getUserId()));
            String nickname = oneLine.getUser().getNickname();
            String safeNickname = nickname != null ? nickname : "알 수 없는 사용자";
            return new OneLineRatingUnitDto(oneLine, isLiked, safeNickname);
        });
        return OneLineRatingListResponseDto.from(bookInfoId, dtoPage);
    }

    @Transactional(readOnly = true)
    public BookListResponseDto<BookCoverImageUnitDto> getMostReadBooks() {
        LocalDateTime monthsAgo = LocalDateTime.now().minusMonths(3);
        List<UserBook> userBookList = userBookRepository.findAllByCreatedTimeAfter(monthsAgo);

        Map<Long, Integer> bookInfoCountMap = new HashMap<>();
        for (UserBook userBook : userBookList) {
            if( userBook.getBookInfo().getCreatedUserId() != null){
                continue;
            }
            Long bookInfoId = userBook.getBookInfo().getBookInfoId();
            bookInfoCountMap.put(bookInfoId, bookInfoCountMap.getOrDefault(bookInfoId, 0) + 1);
        }

        List<Long> bookInfoIdList=  bookInfoCountMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(12)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<BookCoverImageUnitDto> coverList = new ArrayList<>();
        for(Long bookInfoId : bookInfoIdList){
            BookInfo bookInfo = getBookInfoById(bookInfoId);
            coverList.add(BookCoverImageUnitDto.from(bookInfo));
        }
        return new BookListResponseDto<>(coverList);
    }

    @Transactional(readOnly = true)
    public UserArchiveResponseDto getAllUserBookArchive(Long bookInfoId, Long userId, Pageable pageable) {
        User bookUser = userService.getActiveUserByUserId(userId);
        User currentUser = userService.getCurrentUser();

        if(!isFriend(currentUser,bookUser)){
            throw new CustomException(ErrorCode.FRIENDSHIP_REQUIRED);
        }
        BookInfo bookInfo = getBookInfoById(bookInfoId);
        UserBook userBook = getUserBookByUserAndBookInfo(bookInfo, bookUser);

        List<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> archiveList = new ArrayList<>();

        List<Excerpt> excerpts = excerptRepository.findExcerptsByUserBookWithPublic(userBook);
        List<Review> reviews = reviewRepository.findReviewsByUserBookWithPublic(userBook);
        for(Excerpt excerpt : excerpts){
            Long archiveId = archiveService.findArchiveByType(excerpt.getExcerptId(), EXCERPT).getArchiveId();
            archiveList.add(new UserArchiveResponseDto.ArchiveWithoutTitleAuthor(EXCERPT, ExcerptResponseDto.from(excerpt), archiveId));
        }
        for(Review review : reviews){
            Long archiveId = archiveService.findArchiveByType(review.getReviewId(), REVIEW).getArchiveId();
            archiveList.add(new UserArchiveResponseDto.ArchiveWithoutTitleAuthor(REVIEW, ReviewResponseDto.from(review), archiveId));
        }

        List<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> sortedArchiveList = sortByCreatedTime(archiveList);
        Page<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> dtoPage = new PageImpl<>(sortedArchiveList, pageable, sortedArchiveList.size());
        return UserArchiveResponseDto.fromWithoutTitleAuthor(dtoPage);
    }

    @Transactional(readOnly = true)
    public boolean isFriend(User currentUser, User otherUser){
        return friendRepository.findFriendBetweenUsers(currentUser.getUserId(), otherUser.getUserId()).isPresent();
    }

    @Transactional(readOnly = true)
    public UserArchiveResponseDto getAllMyBookArchive(Long bookInfoId, Pageable pageable) {
        User user = userService.getCurrentUser();
        BookInfo bookInfo = getBookInfoById(bookInfoId);
        UserBook userBook  = getUserBookByUserAndBookInfo(bookInfo, user);

        List<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> archiveList = new ArrayList<>();

        List<Excerpt> excerpts = excerptRepository.findExcerptByUserBookOrderByCreatedTimeDesc(userBook);
        List<Review> reviews = reviewRepository.findReviewByUserBookOrderByCreatedTimeDesc(userBook);
        for(Excerpt excerpt : excerpts){
            Long archiveId = archiveService.findArchiveByType(excerpt.getExcerptId(), EXCERPT).getArchiveId();
            archiveList.add(new UserArchiveResponseDto.ArchiveWithoutTitleAuthor(EXCERPT, ExcerptResponseDto.from(excerpt), archiveId));
        }
        for(Review review : reviews){
            Long archiveId = archiveService.findArchiveByType(review.getReviewId(), REVIEW).getArchiveId();
            archiveList.add(new UserArchiveResponseDto.ArchiveWithoutTitleAuthor(REVIEW, ReviewResponseDto.from(review), archiveId));
        }

        List<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> sortedArchiveList = sortByCreatedTime(archiveList);
        Page<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> dtoPage = new PageImpl<>(sortedArchiveList, pageable, sortedArchiveList.size());
        return UserArchiveResponseDto.fromWithoutTitleAuthor(dtoPage);
    }

    // Archive 최신순으로 정렬
    public List<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> sortByCreatedTime(List<UserArchiveResponseDto.ArchiveWithoutTitleAuthor> archiveList) {
        archiveList.sort((a1, a2) -> {
                    LocalDateTime createdTime1 = getCreatedTime(a1);
                    LocalDateTime createdTime2 = getCreatedTime(a2);
                    return createdTime2.compareTo(createdTime1); // 최신순 정렬
                });
        return archiveList;
    }

    private LocalDateTime getCreatedTime(ArchiveWithoutTitleAuthor a) {
        if (a.data() instanceof ExcerptResponseDto) {
            return ((ExcerptResponseDto) a.data()).createdTime();
        }
        else{
            return ((ReviewResponseDto) a.data()).createdTime();
        }
    }

    @Transactional(readOnly = true)
    public UserBook getUserBookByUserAndBookInfo(BookInfo bookInfo, User user){
        return userBookRepository.findByUserAndBookInfo(user, bookInfo)
                .orElseThrow(()-> new CustomException(ErrorCode.USERBOOK_NOT_FOUND));
    }

    public UserBook addBookByProviderId(String providerId, AddUserBookRequestDto requestDto) {
        User user = userService.getCurrentUser();
        Optional<BookInfo> bookInfo = findBookInfoByProviderId(providerId);

        UserBook savedUserBook;
        if(bookInfo.isPresent()){
            Optional<UserBook> existingUserBook = userBookRepository.findByUserAndBookInfo(user, bookInfo.get());
            if(existingUserBook.isPresent()){
                throw new CustomException(ErrorCode.USERBOOK_ALREADY_EXISTS);
            }
            UserBook userBook = new UserBook(ReadStatus.NOT_STARTED, user, bookInfo.get());
            savedUserBook = userBookRepository.save(userBook);
        }
        else{
            String responseBody = googleBooksApiService.searchOneBook(providerId);
            BookInfoDetailDto additional = parseBookDetail(responseBody);
            Genre genre = genreService.matchGenre(additional.category());

            BookInfo newBookInfo = additional.toEntity(providerId, requestDto, genre);
            bookInfoRepository.save(newBookInfo);

            UserBook userBook = requestDto.toEntity(user, newBookInfo, ReadStatus.NOT_STARTED);
            savedUserBook = userBookRepository.save(userBook);
        }
        checkExpAndBadgeForFinishedBook(savedUserBook);
        return savedUserBook;
    }

    // 경험치 획득, READ 뱃지 unlock 확인
    public void checkExpAndBadgeForFinishedBook(UserBook userBook) {
        userGrowthService.gainExpForFinishedBook(userBook);
        badgeUnlockService.checkAndUnlockBadges(userBook.getUser());
    }

    public AddUserBookResponseDto convertToAddUserBookResponseDto(String providerId, AddUserBookRequestDto requestDto) {
        UserBook userBook = addBookByProviderId(providerId, requestDto);
        return new AddUserBookResponseDto(userBook);
    }

    // 연관 추천 도서 조회
    @Transactional(readOnly = true)
    public BookListResponseDto<BookCoverImageUnitDto> getRelatedBooks(Long bookInfoId) {
        List<BookInfo> relatedBooks = bookInfoRepository.findRelatedBooksByBookInfoId(bookInfoId);
        List<BookCoverImageUnitDto> topSixBooks = relatedBooks.stream()
                .limit(6)
                .map(BookCoverImageUnitDto::from)
                .collect(Collectors.toList());
        return new BookListResponseDto<>(topSixBooks);
    }

    // 사용자 customBook 삭제
    public void deleteUserCustomBook(User user) {
        List<BookInfo> customBookList = bookInfoRepository.findCustomBookByCreatedUserId(user.getUserId());
        for(BookInfo customBook : customBookList){
            Optional<UserBook> customUserBook = userBookRepository.findByUserAndBookInfo(user, customBook);
            if(customUserBook.isPresent()){
                userBookRepository.delete(customUserBook.get());
            }
            bookInfoRepository.delete(customBook);
        }
    }
}

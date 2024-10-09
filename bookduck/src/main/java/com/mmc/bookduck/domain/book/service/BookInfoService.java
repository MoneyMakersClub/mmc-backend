package com.mmc.bookduck.domain.book.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmc.bookduck.domain.book.dto.common.BookInfoUnitDto;
import com.mmc.bookduck.domain.book.dto.request.UserBookRequestDto;
import com.mmc.bookduck.domain.book.dto.response.ApiBookBasicResponseDto;
import com.mmc.bookduck.domain.book.dto.response.BookListResponseDto;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.Genre;
import com.mmc.bookduck.domain.book.repository.BookInfoRepository;
import com.mmc.bookduck.domain.book.repository.GenreRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import com.mmc.bookduck.global.google.GoogleBooksApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookInfoService {
    private final BookInfoRepository bookInfoRepository;
    private final GenreRepository genreRepository;
    private final GoogleBooksApiService googleBooksApiService;


    // api 도서 목록 조회
    public BookListResponseDto searchBookList(String keyword, Long page, Long size) {
        String responseBody = googleBooksApiService.searchBookList(keyword, page, size);
        return parseBookInfo(responseBody);
    }

    // 목록 정보 파싱
    public BookListResponseDto parseBookInfo(String apiResult){
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(apiResult);
            JsonNode itemsNode = rootNode.path("items");

            List<BookInfoUnitDto> bookList = new ArrayList<>();

            for(JsonNode itemNode : itemsNode) {
                // providerId
                String providerId = itemNode.get("id").asText();

                JsonNode info = itemNode.get("volumeInfo");
                // title
                String title = getTextNode(info, "title");
                // authors
                JsonNode authorsNode = info.get("authors");
                List<String> authors = new ArrayList<>();
                if (authorsNode != null && authorsNode.isArray()) {
                    for (JsonNode authorNode : authorsNode) {
                        authors.add(authorNode.asText());
                    }
                } else {
                    // authors가 없으면, null로 설정
                    authors = null;
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
                bookList.add(new BookInfoUnitDto(title, authors, imgPath, providerId));
            }
            return new BookListResponseDto(bookList);

        }catch(Exception e){
            throw new CustomException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    private Long extractYear(String publishedDate) {
        if (publishedDate == null || publishedDate.isEmpty()) {
            return null;
        }
        else {
            return Long.parseLong(publishedDate.substring(0, 4));  //네자리 연도로 추출
        }
    }

    private String getTextNode(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asText();
        }
        return null; // 필드가 없을 경우 null
    }

    //api 도서 기본 정보 조회
    public ApiBookBasicResponseDto getApiBookBasic(String providerId) {
        String responseBody = googleBooksApiService.searchOneBook(providerId);
        return parseBookDetail(responseBody);
    }

    // 기본 정보 파싱
    private ApiBookBasicResponseDto parseBookDetail(String responseBody) {

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode info = rootNode.get("volumeInfo");

            // subtitle
            String subtitle = getTextNode(info, "subtitle");
            // publisher
            String publisher = getTextNode(info, "publisher");
            // publishedYear
            String publishedDate = getTextNode(info, "publishedDate");
            Long publishedYear = extractYear(publishedDate);
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
            String language = getTextNode(info, "language");
            return new ApiBookBasicResponseDto(description, page, cate, language);

        }catch(Exception e){
            throw new CustomException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    // api bookInfo 저장
    @Transactional
    public BookInfo saveApiBookInfo (UserBookRequestDto dto) {

        String saveAuthor = dto.getAuthors().getFirst();
        Genre genre = genreRepository.findById(1l)
                .orElseThrow(()->new CustomException(ErrorCode.GENRE_NOT_FOUND));

        BookInfo bookInfo = BookInfo.builder()
                .providerId(dto.getProviderId())
                .title(dto.getTitle())
                .author(saveAuthor)
                .publisher(dto.getPublisher())
                .publishDate(dto.getPublishDate())
                .description(dto.getDescription())
                .category(dto.getCategory().getFirst())
                .pageCount(dto.getPageCount())
                .imgPath(dto.getImgPath())
                .language(dto.getLanguage())
                .genre(genre)
                .build();

        return bookInfoRepository.save(bookInfo);
    }

    @Transactional(readOnly = true)
    public BookInfo findBookInfoByProviderId(String providerId) {
        return bookInfoRepository.findByProviderId(providerId);
    }

    // bookInfo 삭제
    @Transactional
    public void deleteBookInfo(Long bookInfoId) {
        BookInfo bookInfo = bookInfoRepository.findById(bookInfoId)
                .orElseThrow(()-> new CustomException(ErrorCode.BOOKINFO_NOT_FOUND));
        bookInfoRepository.delete(bookInfo);
    }

    /*
    // 직접 등록한 책 검색
    public BookListResponseDto searchCustomBookList(String keyword, Long page, Long size) {

        // 먼저 user 검색
        User user = userRepository.findById(userId);

        List<BookInfo> bookInfos = bookInfoRepository.searchByCreatedUserIdAndKeyword(user.getUserId(), keyword);
        if(bookInfos != null){

            List<BookInfoUnitDto> bookList = new ArrayList<>();

            for(BookInfo bookinfo = bookInfos){
                Long publishedYear = extractYear(bookinfo.getPublishDate());
                // 저자를 리스트로 변환
                List<String> authors = Collections.singletonList(bookinfo.getAuthor());
                bookList.add(new BookInfoUnitDto(bookinfo.getTitle(), authors, bookinfo.getPublisher(), publishedYear, bookinfo.getImgPath(), bookinfo.getProviderId()));
            }
            return new BookListResponseDto(bookList);
        }else{
            // 검색결과 없는 경우
            return new BookListResponseDto(null);
        }
    }
    */
    
}

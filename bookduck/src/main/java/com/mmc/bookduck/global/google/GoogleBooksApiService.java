package com.mmc.bookduck.global.google;

import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@RequiredArgsConstructor
public class GoogleBooksApiService {

    RestTemplate restTemplate = new RestTemplate();

    @Value("${google.books.api.key}")
    private String apiKey;

    // 목록 검색
    public String searchBookList(String keyword, Long page, Long size) {
        try {
            String url = "https://www.googleapis.com/books/v1/volumes?q=" + keyword + "&startIndex=" + (page - 1)
                    + "&maxResults=" + size + "&key=" + apiKey;

            // API GET 요청
            ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            return apiResponse.getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    // 책 상세 검색
    public String searchOneBook(String providerId){
        try {
            String url = "https://www.googleapis.com/books/v1/volumes/" + providerId + "?key=" + apiKey;
            // API GET 요청
            ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            return apiResponse.getBody();
        }catch(Exception e){
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

}

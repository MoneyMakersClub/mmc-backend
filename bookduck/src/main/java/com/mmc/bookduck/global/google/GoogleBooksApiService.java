package com.mmc.bookduck.global.google;

import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import com.mmc.bookduck.global.redis.RedisService;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleBooksApiService {

    private final RedisService redisService;
    RestTemplate restTemplate = new RestTemplate();

    @Value("${google.books.api.key}")
    private String apiKey;

    // 목록 검색
    public String searchBookList(String keyword, Long page, Long size) {
        String cacheKey = "googlebooks:search_cache:" + keyword;
        Object cached = redisService.getValues(cacheKey);
        if (cached != null) {
            // log.info("redis cache hit");
            return cached.toString();
        }
        try {
            // log.info("redis cache miss");
            String url = "https://www.googleapis.com/books/v1/volumes?q=" + keyword + "&startIndex=" + (page * size)
                    + "&maxResults=" + size + "&key=" + apiKey;

            // API GET 요청
            ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            String result = apiResponse.getBody();
            // 6시간 캐싱
            redisService.setValuesWithTimeout(cacheKey, result, Duration.ofHours(6));
            return result;
        } catch (RedisException e) {
            throw new CustomException(ErrorCode.REDIS_ERROR);
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

package com.mmc.bookduck.domain.export.service;

import com.mmc.bookduck.domain.archive.dto.response.ExcerptExportResponseDto;
import com.mmc.bookduck.domain.archive.dto.response.ReviewExportResponseDto;
import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.domain.archive.repository.ExcerptRepository;
import com.mmc.bookduck.domain.archive.repository.ReviewRepository;
import com.mmc.bookduck.domain.archive.service.ExcerptService;
import com.mmc.bookduck.domain.archive.service.ReviewService;
import com.mmc.bookduck.domain.book.entity.GenreName;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.repository.UserBookRepository;
import com.mmc.bookduck.domain.export.dto.ExportCharResponseDto;
import com.mmc.bookduck.domain.export.dto.ExportStatsResponseDto;
import com.mmc.bookduck.domain.item.dto.common.ItemEquippedUnitDto;
import com.mmc.bookduck.domain.item.service.UserItemService;
import com.mmc.bookduck.domain.user.dto.response.UserKeywordResponseDto;
import com.mmc.bookduck.domain.user.dto.response.UserStatisticsResponseDto;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserReadingReportService;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportService {
    private final UserService userService;
    private final UserReadingReportService userReadingReportService;
    private final UserItemService userItemService;
    private final ExcerptService excerptService;
    private final ReviewService reviewService;
    private final ExcerptRepository excerptRepository;
    private final ReviewRepository reviewRepository;
    private final UserBookRepository userBookRepository;

    // 캐릭터 내보내기
    public ExportCharResponseDto getCharExportInfo(){
        User user = userService.getCurrentUser();
        long userBookCount = userBookRepository.countByUser(user);
        if (userBookCount == 0) {
            throw new CustomException(ErrorCode.READINGREPORT_NOT_VIEWABLE);
        }
        String nickname = user.getNickname();
        // UserStatisticsResponseDto를 통해 duckTitle 재사용
        UserStatisticsResponseDto userStatistics = userReadingReportService.getUserStatistics(user.getUserId());
        String duckTitle = userStatistics.duckTitle();
        UserKeywordResponseDto keywordResponse = userReadingReportService.getUserKeywordWithLimit(user.getUserId(), 3);
        List<ItemEquippedUnitDto> userItemEquipped = userItemService.getUserItemEquippedListOfUser(user);
        return new ExportCharResponseDto(nickname, duckTitle, keywordResponse, userItemEquipped);
    }

    // 통계 요약 내보내기
    public ExportStatsResponseDto getStatsExportInfo(){
        User user = userService.getCurrentUser();
        String nickname = user.getNickname();
        // 시즌 계산
        int month = LocalDate.now().getMonthValue();
        String season = calculateSeason(month);
        LocalDate[] seasonDates = getSeasonDates(month);
        LocalDate startDate = seasonDates[0];
        LocalDate endDate = seasonDates[1];
        // 다 읽은 책, 발췌, 감상, TOTAL
        long finishedBookCount = userBookRepository.countByUserAndReadStatusAndCreatedTimeBetween(
                user, ReadStatus.FINISHED, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        long excerptCount = excerptRepository.countByUserAndCreatedTimeBetween(
                user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        long reviewCount = reviewRepository.countByUserAndCreatedTimeBetween(
                user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        // 선호하는 작가, 선호하는 장르, 기록 키워드
        List<GenreName> genres = userBookRepository.findTopGenreByUserAndCreatedTimeBetween(
                user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        GenreName mostReadGenre = genres.isEmpty() ? null : genres.getFirst();

        List<String> authors = userBookRepository.findTopAuthorByUserAndCreatedTimeBetween(
                user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        String mostReadAuthor = authors.isEmpty() ? null : authors.getFirst();

        UserKeywordResponseDto keywordResponse = userReadingReportService.getUserKeywordWithLimit(user.getUserId(), 1);
        return new ExportStatsResponseDto(
                nickname,
                season,
                LocalDate.now(),
                finishedBookCount,
                mostReadGenre,
                mostReadAuthor,
                keywordResponse,
                excerptCount,
                reviewCount,
                excerptCount + reviewCount
        );

    }

    public String calculateSeason(int month){
        if (month == 1 || month == 2){
            return "겨울";
        } else if (month >= 3 && month <= 5) {
            return "봄";
        } else if (month >= 6 && month <= 8) {
            return "봄";
        } else if (month >= 9 && month <= 11) {
            return "가을";
        } return "연말결산"; // 12월
    }

    public LocalDate[] getSeasonDates(int month) {
        int year = LocalDate.now().getYear();
        Map<String, int[]> seasons = Map.of(
                // n월, m월
                "겨울", new int[]{1,2},
                "봄", new int[]{3,5},
                "여름", new int[]{6,8},
                "가을", new int[]{9,11},
                "연말결산", new int[]{1,12}
        );
        String season = calculateSeason(month);
        int[]  range = seasons.get(season);
        LocalDate startDate = LocalDate.of(year, range[0], 1); // n월 1일
        LocalDate endDate = LocalDate.of(year, range[1], LocalDate.of(year, range[1], 1).lengthOfMonth()); // m월 마지막 날
        return new LocalDate[]{startDate, endDate};
    }

    // 발췌 카드 공유하기
    public ExcerptExportResponseDto getExcerptCardInfo(Long excerptId) {
        Excerpt excerpt = excerptService.getExcerptById(excerptId);
        return ExcerptExportResponseDto.from(excerpt);
    }

    // 감상평 카드 공유하기
    public ReviewExportResponseDto getReviewCardInfo(Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ReviewExportResponseDto.from(review);
    }

}

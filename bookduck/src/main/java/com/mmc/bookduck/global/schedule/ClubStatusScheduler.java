package com.mmc.bookduck.global.schedule;

import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import com.mmc.bookduck.domain.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubStatusScheduler {

    private final ClubRepository clubRepository;

    // 매일 자정에 실행 (크론 표현식: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateExpiredClubs() {
        try {
            List<Club> expiredClubs = clubRepository.findByClubStatusAndActiveEndAtBefore(
                    ClubStatus.ACTIVE, LocalDateTime.now());
            if (expiredClubs.isEmpty()) {
                log.info("종료된 클럽 없음.");
                return;
            }
            expiredClubs.forEach(club -> club.updateStatus(ClubStatus.ENDED));
            log.info("종료된 클럽 {}개 상태 갱신 완료", expiredClubs.size());
        } catch (Exception e) {
            log.error("클럽 상태 갱신 중 오류 발생", e);
        }
    }
}

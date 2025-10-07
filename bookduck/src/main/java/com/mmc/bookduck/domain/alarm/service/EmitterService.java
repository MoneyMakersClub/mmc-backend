package com.mmc.bookduck.domain.alarm.service;

import com.mmc.bookduck.domain.alarm.dto.ssedata.AlarmDefaultDataDto;
import com.mmc.bookduck.domain.alarm.dto.ssedata.BadgeModalInfo;
import com.mmc.bookduck.domain.alarm.entity.Alarm;
import com.mmc.bookduck.domain.alarm.entity.AlarmType;
import com.mmc.bookduck.domain.alarm.repository.AlarmRepository;
import com.mmc.bookduck.domain.alarm.repository.EmitterRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmitterService {
    private final EmitterRepository emitterRepository;
    private final AlarmRepository alarmRepository;
    private final UserService userService;

    private static final Long DEFAULT_TIMEOUT = 5L * 60 * 1000;  // 5분

    public SseEmitter subscribe() {
        User user = userService.getCurrentUser();
        Long userId = user.getUserId();

        // 이미 존재하는 Emitter가 있는지 확인
        SseEmitter emitter = Optional.ofNullable(emitterRepository.get(userId))
                .orElseGet(() -> registerEmitter(userId));

        sendToClientDefaultAlarm(user);
        return emitter;
    }

    // 기본 알림 전송
    public void sendToClientDefaultAlarm(User user) {
        boolean isCommonAlarmChecked = !alarmRepository.existsByReceiverAndIsReadFalse(user);
        AlarmDefaultDataDto alarmDataDto = AlarmDefaultDataDto
                .fromDefault(isCommonAlarmChecked, user.getIsAnnouncementChecked(), true);
        sendToClient(user.getUserId(), alarmDataDto, "new sse alarm exists");
    }

    // 아이템 획득 알림 전송
    public void sendToClientItemUnlockedAlarm(User user) {
        boolean isCommonAlarmChecked = !alarmRepository.existsByReceiverAndIsReadFalse(user);
        AlarmDefaultDataDto alarmDataDto = AlarmDefaultDataDto
                .fromDefault(isCommonAlarmChecked, user.getIsAnnouncementChecked(), false);
        sendToClient(user.getUserId(), alarmDataDto, "item unlocked alarm exists");
    }

    // 레벨업 알림 전송
    public void sendToClientLevelUpAlarm(User user, int level) {
        boolean isCommonAlarmChecked = !alarmRepository.existsByReceiverAndIsReadFalse(user);
        AlarmDefaultDataDto alarmDataDto = AlarmDefaultDataDto
                .fromLevelUp(isCommonAlarmChecked, user.getIsAnnouncementChecked(), level);
        sendToClient(user.getUserId(), alarmDataDto, "level up alarm exists");
    }

    // 뱃지 획득 알림 전송
    public void sendToClientBadgeUnlockedAlarm(User user, BadgeModalInfo badgeModalInfo) {
        boolean isCommonAlarmChecked = !alarmRepository.existsByReceiverAndIsReadFalse(user);
        AlarmDefaultDataDto alarmDataDto = AlarmDefaultDataDto
                .fromBadgeUnlocked(isCommonAlarmChecked, user.getIsAnnouncementChecked(), badgeModalInfo);
        sendToClient(user.getUserId(), alarmDataDto, "badge unlocked alarm exists");
    }

    private SseEmitter registerEmitter(Long memberId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(memberId));
        emitter.onTimeout(() -> emitterRepository.delete(memberId));

        return emitter;
    }

    public void notify(Long memberId, Object data, String comment) {
        sendToClient(memberId, data, comment);
    }

    private <T> void sendToClient(Long memberId, Object data, String comment) {
        SseEmitter emitter = emitterRepository.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(memberId))
                        .name("sse-alarm")
                        .data(data)
                        .comment(comment));
            } catch (IllegalStateException | IOException e) {
                emitterRepository.delete(memberId);
                emitter.completeWithError(e);
            }
        }
    }
}

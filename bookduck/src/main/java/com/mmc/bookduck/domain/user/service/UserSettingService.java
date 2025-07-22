package com.mmc.bookduck.domain.user.service;

import com.mmc.bookduck.domain.user.dto.request.UserNicknameRequestDto;
import com.mmc.bookduck.domain.user.dto.request.UserSettingUpdateRequestDto;
import com.mmc.bookduck.domain.user.dto.response.UserNicknameResponseDto;
import com.mmc.bookduck.domain.user.dto.response.UserSettingInfoResponseDto;
import com.mmc.bookduck.domain.user.dto.response.UserNicknameAvailabilityResponseDto;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.entity.UserSetting;
import com.mmc.bookduck.domain.user.repository.UserSettingRepository;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSettingService {
    private final UserService userService;
    private final UserSettingRepository userSettingRepository;

    @Transactional(readOnly = true)
    public UserSettingInfoResponseDto getUserSettingInfo() {
        User user = userService.getCurrentUser();
        UserSetting userSetting = getUserSettingByUser(user);
        return UserSettingInfoResponseDto.from(user, userSetting);
    }

    @Transactional(readOnly = true)
    public UserSetting getUserSettingByUser(User user) {
        return userSettingRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.USERSETTING_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserNicknameResponseDto getUserNickname() {
        User user = userService.getCurrentUser();
        return new UserNicknameResponseDto(user.getNickname());
    }

    @Transactional(readOnly = true)
    public UserNicknameAvailabilityResponseDto checkNicknameAvailability(UserNicknameRequestDto requestDto) {
        User user = userService.getCurrentUser();
        String nickname = requestDto.nickname();
        if (user.getNickname().equals(nickname))
            return new UserNicknameAvailabilityResponseDto(true);
        return new UserNicknameAvailabilityResponseDto(!userService.existsByNickname(nickname));
    }

    public void updateUserNickname(UserNicknameRequestDto requestDto) {
        String nickname = requestDto.nickname();
        User user = userService.getCurrentUser();
        if (user.getNickname().equals(nickname))
            return;

        try {
            user.updateNickname(nickname); // dirty checking으로 트랜잭션 커밋 시 flush
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    public void updateOptions(UserSettingUpdateRequestDto requestDto) {
        User user = userService.getCurrentUser();
        UserSetting userSetting = getUserSettingByUser(user);

        // null이 아닌 설정 옵션만 업데이트 진행
        updateSetting(requestDto.isPushAlarmEnabled(), userSetting::updateIsPushAlarmEnabled);
        updateSetting(requestDto.isFriendRequestEnabled(), userSetting::updateIsFriendRequestEnabled);
        updateSetting(requestDto.recordFont(), userSetting::updateRecordFont);
        userSettingRepository.save(userSetting);
    }

    // value가 null이 아닐 경우 updateFunction을 실행
    private <T> void updateSetting(T value, Consumer<T> updateFunction) {
        Optional.ofNullable(value).ifPresent(updateFunction);
    }
}
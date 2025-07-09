package com.mmc.bookduck.domain.user.service;

import com.mmc.bookduck.domain.alarm.service.AlarmService;
import com.mmc.bookduck.domain.badge.service.UserBadgeService;
import com.mmc.bookduck.domain.book.service.BookInfoService;
import com.mmc.bookduck.domain.folder.service.FolderService;
import com.mmc.bookduck.domain.friend.service.FriendRequestService;
import com.mmc.bookduck.domain.friend.service.FriendService;
import com.mmc.bookduck.domain.homecard.service.HomeCardService;
import com.mmc.bookduck.domain.item.service.UserItemService;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.global.security.CookieUtil;
import com.mmc.bookduck.global.redis.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserWithdrawService {
    private final UserService userService;
    private final RedisService redisService;
    private final CookieUtil cookieUtil;
    private final FolderService folderService;
    private final BookInfoService bookInfoService;
    private final UserBadgeService userBadgeService;
    private final HomeCardService homeCardService;
    private final UserItemService userItemService;
    private final FriendRequestService friendRequestService;
    private final FriendService friendService;
    private final AlarmService alarmService;

    public void withdrawUser(HttpServletResponse response) {
        User user = userService.getCurrentUser();

        log.info("userId: " + user.getUserId() +" | 이메일: " + user.getEmail() + " 회원이 탈퇴했습니다.");

        // 폴더 & 커스텀책 삭제
        folderService.deleteUserFolder(user);
        bookInfoService.deleteUserCustomBook(user);

        // 뱃지, 아이템, 카드(위젯) 삭제
        userBadgeService.deleteUserBadgesByUser(user);
        userItemService.deletUserItemsByUser(user);
        homeCardService.deleteHomeCardsByUser(user);

        // 친구 요청, 친구, 알림 삭제
        friendRequestService.deleteFriendRequestsByUser(user);
        friendService.deleteFriendsOfUser(user);
        alarmService.deleteAlarmsOfUser(user);

        // 글 삭제

        // 유저 데이터 삭제
        user.clearUserData();;
        userService.saveUser(user);

        String redisKey = "auth:refresh_token:" + user.getEmail();
        redisService.deleteValues(redisKey);
        cookieUtil.deleteCookie(response, "refreshToken");
    }
}

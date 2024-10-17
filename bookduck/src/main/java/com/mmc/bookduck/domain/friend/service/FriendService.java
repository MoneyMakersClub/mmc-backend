package com.mmc.bookduck.domain.friend.service;

import com.mmc.bookduck.domain.friend.dto.common.FriendUnitDto;
import com.mmc.bookduck.domain.friend.dto.request.FriendCreateRequestDto;
import com.mmc.bookduck.domain.friend.dto.response.FriendListResponseDto;
import com.mmc.bookduck.domain.friend.entity.Friend;
import com.mmc.bookduck.domain.friend.entity.FriendRequest;
import com.mmc.bookduck.domain.friend.entity.FriendRequestStatus;
import com.mmc.bookduck.domain.friend.repository.FriendRepository;
import com.mmc.bookduck.domain.friend.repository.FriendRequestRepository;
import com.mmc.bookduck.domain.skin.dto.common.UserSkinEquippedDto;
import com.mmc.bookduck.domain.skin.service.UserSkinService;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final UserService userService;
    private final UserSkinService userSkinService;

    // 친구 요청 수락 (=친구 생성)
    public void createFriend(Long friendRequestId){
        FriendRequest request = friendRequestRepository.findById(friendRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        // currentUser = receiver여야만 수락 가능
        User currentUser = userService.getCurrentUser();
        if (!request.getReceiver().getUserId().equals(currentUser.getUserId())){
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        // 이미 친구인지 확인
        if (friendRepository.findFriendBetweenUsers(request.getSender().getUserId(), request.getReceiver().getUserId()).isPresent()) {
            throw new CustomException(ErrorCode.FRIEND_ALREADY_EXISTS);
        }

        User sender = request.getSender();
        FriendCreateRequestDto friendCreateRequestDto = new FriendCreateRequestDto(friendRequestId);
        Friend friend = friendCreateRequestDto.toEntity(sender, currentUser);
        friendRepository.save(friend);
        request.setFriendRequestStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);
    }

    // 친구 목록 조회
    @Transactional(readOnly = true)
    public FriendListResponseDto getFriendList() {
        User currentUser = userService.getCurrentUser();
        List<FriendUnitDto> friendList = friendRepository.findByUser1UserId(currentUser.getUserId())
                .stream()
                .map(friend -> {
                    UserSkinEquippedDto userSkinEquipped = userSkinService.getEquippedSkinOrDefault(friend.getUser2().getUserId());
                    return FriendUnitDto.from(friend, userSkinEquipped);
                })
                .collect(Collectors.toList());
        return FriendListResponseDto.from(friendList);
    }

    // 친구 삭제
    public void deleteFriend(Long friendId) {
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_NOT_FOUND));

        // 사용자가 user1이거나 user2일 때만 친구 삭제 가능
        User currentUser = userService.getCurrentUser();
        if (!friend.getUser1().getUserId().equals(currentUser.getUserId()) &&
                !friend.getUser2().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        // 친구 요청 상태를 BREAKUP로 변경
        FriendRequest request = friendRequestRepository.findFriendRequestBetweenUsers(
                        friend.getUser1().getUserId(), friend.getUser2().getUserId(), FriendRequestStatus.ACCEPTED)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));
        request.setFriendRequestStatus(FriendRequestStatus.BREAKUP);

        friendRepository.delete(friend);
    }
}

package com.mmc.bookduck.domain.friend.controller;

import com.mmc.bookduck.domain.friend.dto.request.FriendCreateRequestDto;
import com.mmc.bookduck.domain.friend.dto.response.FriendListResponseDto;
import com.mmc.bookduck.domain.friend.entity.Friend;
import com.mmc.bookduck.domain.friend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
@Tag(name = "Friend", description = "Friend 관련 API입니다.")
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    @Operation(summary = "친구 요청 수락", description = "친구 요청을 수락하고 친구를 생성합니다.")
    public ResponseEntity<?> createFriend(@Valid @RequestBody FriendCreateRequestDto requestDto) {
        friendService.createFriend(requestDto.friendRequestId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "친구 목록 조회", description = "현재 사용자의 친구 목록을 조회합니다.")
    public ResponseEntity<?> getFriendList() {
        FriendListResponseDto friendList = friendService.getFriendList();
        return ResponseEntity.ok(friendList);
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "친구 삭제", description = "친구를 삭제합니다.")
    public ResponseEntity<?> deleteFriend(@PathVariable("friendId") final Long friendId) {
        friendService.deleteFriend(friendId);
        return ResponseEntity.ok().build();
    }
}


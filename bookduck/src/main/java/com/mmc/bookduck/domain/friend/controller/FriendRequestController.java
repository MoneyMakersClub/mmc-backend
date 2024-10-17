package com.mmc.bookduck.domain.friend.controller;

import com.mmc.bookduck.domain.friend.dto.request.FriendRequestDto;
import com.mmc.bookduck.domain.friend.dto.response.FriendRequestListResponseDto;
import com.mmc.bookduck.domain.friend.service.FriendRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/friendrequests")
@Tag(name = "FriendRequest", description = "FriendRequset 관련 API입니다.")
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    @PostMapping
    @Operation(summary = "친구 요청 전송", description = "유저가 다른 유저에게 친구 요청을 보냅니다.")
    public ResponseEntity<?> sendFriendRequest(@Valid @RequestBody FriendRequestDto requestDto) {
        friendRequestService.sendFriendRequest(requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendRequestId}")
    @Operation(summary = "친구 요청 취소", description = "보낸 친구 요청을 취소합니다.")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable("friendRequestId") final Long friendRequestId) {
        friendRequestService.cancelFriendRequest(friendRequestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/received")
    @Operation(summary = "받은 친구 요청 목록 조회", description = "받은 친구 요청 목록을 조회합니다.")
    public ResponseEntity<?> getReceivedFriendRequests() {
        FriendRequestListResponseDto receivedList = friendRequestService.getReceivedFriendRequests();
        return ResponseEntity.ok(receivedList);
    }

    @GetMapping("/sent")
    @Operation(summary = "보낸 친구 요청 목록 조회", description = "보낸 친구 요청 목록을 조회합니다.")
    public ResponseEntity<?> getSentFriendRequests() {
        FriendRequestListResponseDto sentList = friendRequestService.getSentFriendRequests();
        return ResponseEntity.ok(sentList);
    }

    @PutMapping("/{friendRequestId}/reject")
    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절합니다.")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable("friendRequestId") final Long friendRequestId){
        friendRequestService.rejectFriendRequest(friendRequestId);
        return ResponseEntity.ok().build();
    }

}

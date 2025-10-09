package com.mmc.bookduck.domain.bookclub.controller;

import com.mmc.bookduck.domain.bookclub.dto.request.ClubCreateRequestDto;
import com.mmc.bookduck.domain.bookclub.dto.request.ClubJoinRequestDto;
import com.mmc.bookduck.domain.bookclub.dto.response.*;
import com.mmc.bookduck.domain.bookclub.entity.ClubStatus;
import com.mmc.bookduck.domain.bookclub.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Club", description = "북클럽 생성 및 가입 관련 API")
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;

    @Operation(summary = "클럽 생성", description = "새로운 북클럽을 생성합니다.")
    @PostMapping
    public ResponseEntity<ClubCreateResponseDto> createClub(@Valid @RequestBody ClubCreateRequestDto requestDto) {
        return ResponseEntity.ok(clubService.createClub(requestDto));
    }

    @Operation(summary = "클럽 가입", description = "클럽 ID를 통해 북클럽에 가입합니다.")
    @PostMapping("/{clubId}/join")
    public ResponseEntity<ClubJoinResponseDto> joinClub(
            @PathVariable Long clubId,
            @Valid @RequestBody ClubJoinRequestDto requestDto) {
        return ResponseEntity.ok(clubService.joinClub(clubId, requestDto));
    }

    @Operation(summary = "가입된 클럽 목록 조회", description = "현재 로그인한 사용자가 속한 모든 클럽 목록과, 각 클럽별 읽지 않은 글 수를 반환합니다.")
    @GetMapping("/joined")
    public ResponseEntity<List<ClubJoinedResponseDto>> getJoinedClubs() {
        return ResponseEntity.ok(clubService.getJoinedClubs());
    }

    @Operation(summary = "클럽 검색", description = "클럽명, 책 제목, 저자명으로 클럽을 검색합니다. 정렬 기준: 정확도순 > 가입인원순 > 최근생성순 > 곧종료순")
    @GetMapping("/search")
    public ResponseEntity<ClubSearchListResponseDto> searchClubs(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "ACTIVE") ClubStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(clubService.searchClubs(keyword, status, pageable));
    }
}

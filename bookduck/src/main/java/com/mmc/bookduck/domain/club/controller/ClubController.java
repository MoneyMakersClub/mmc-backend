package com.mmc.bookduck.domain.club.controller;

import com.mmc.bookduck.domain.club.dto.request.ClubCreateRequestDto;
import com.mmc.bookduck.domain.club.dto.request.ClubJoinRequestDto;
import com.mmc.bookduck.domain.club.dto.request.ClubUpdateRequestDto;
import com.mmc.bookduck.domain.club.dto.response.*;
import com.mmc.bookduck.domain.club.entity.ClubStatus;
import com.mmc.bookduck.domain.club.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Club", description = "북클럽 생성 및 가입 관련 API")
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;

    @Operation(summary = "클럽 생성", description = "새로운 북클럽을 생성합니다.")
    @PostMapping
    public ResponseEntity<Long> createClub(@Valid @RequestBody ClubCreateRequestDto requestDto) {
        Long clubId = clubService.createClub(requestDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()      // 현재 /clubs
                .path("/{clubId}")         // 경로 뒤에 /{clubId} 추가
                .buildAndExpand(clubId)    // clubId 값 대입
                .toUri();
        return ResponseEntity.created(location)  // 201 Created + Location 헤더 포함
                .body(clubId);                  // body에 clubId 리턴
    }

    @Operation(summary = "최근 모집 중인 클럽 보기", description = "최근 생성된, 모집 중인 클럽을 표시합니다.")
    @GetMapping("/new")
    public ResponseEntity<ClubSearchListResponseDto> findRecentActiveClubs(
            @RequestParam(value = "orderBy", defaultValue = "latest") String orderBy,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(clubService.findRecentActiveClubs(pageable, orderBy));
    }

    @Operation(summary = "클럽 검색", description = "클럽명, 책 제목, 저자명으로 클럽을 검색합니다. 정렬 기준: 정확도순 > 가입인원순 > 최근생성순 > 곧종료순")
    @GetMapping("/search")
    public ResponseEntity<ClubSearchListResponseDto> searchClubs(
            @RequestParam @NotBlank String keyword,
            @RequestParam(value = "clubStatus", required = false) ClubStatus clubStatus,
            Pageable pageable) {
        return ResponseEntity.ok(clubService.searchClubs(keyword, clubStatus, pageable));
    }

    @Operation(summary = "클럽 상세 조회", description = "클럽의 상세 정보를 조회합니다.")
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubDetailResponseDto> getClubDetail(@PathVariable Long clubId) {
        return ResponseEntity.ok(clubService.getClubDetail(clubId));
    }

    @Operation(summary = "클럽 정보 수정", description = "클럽 리더가 클럽 정보를 수정합니다.")
    @PatchMapping("/{clubId}")
    public ResponseEntity<ClubUpdateResponseDto> updateClub(
            @PathVariable Long clubId,
            @Valid @RequestBody ClubUpdateRequestDto requestDto) {
        return ResponseEntity.ok(clubService.updateClub(clubId, requestDto));
    }

    @Operation(summary = "클럽 삭제", description = "클럽 리더가 클럽을 삭제합니다. 클럽 멤버가 자신뿐일 때만 삭제가 가능합니다.")
    @DeleteMapping("/{clubId}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long clubId) {
        clubService.deleteClub(clubId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "클럽 가입", description = "클럽 ID를 통해 북클럽에 가입합니다. 비밀번호 확인 절차를 거칩니다.")
    @PostMapping("/{clubId}/members")
    public ResponseEntity<Long> joinClub(
            @PathVariable Long clubId,
            @Valid @RequestBody ClubJoinRequestDto requestDto) {
        Long clubMemberId = clubService.joinClub(clubId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(clubMemberId);
    }

    @Operation(summary = "클럽 멤버 목록 조회", description = "클럽 멤버를 조회합니다.")
    @GetMapping("/{clubId}/members")
    public ResponseEntity<ClubMemberListResponseDto> getClubMembers(@PathVariable Long clubId) {
        return ResponseEntity.ok(clubService.getClubMembers(clubId));
    }

    @Operation(summary = "클럽 멤버 탈퇴", description = "현재 사용자가 클럽에서 탈퇴합니다.")
    @DeleteMapping("/{clubId}/members/me")
    public ResponseEntity<Void> leaveClub(@PathVariable Long clubId) {
        clubService.leaveClub(clubId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "클럽 멤버 강퇴", description = "리더가 특정 멤버를 강퇴합니다.")
    @DeleteMapping("/{clubId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long clubId, @PathVariable Long memberId) {
        clubService.removeMember(clubId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내가 가입된 클럽 목록 조회", description = "현재 로그인한 사용자가 속한 모든 클럽 목록과, 각 클럽별 읽지 않은 글 수를 반환합니다.")
    @GetMapping("/joined")
    public ResponseEntity<ClubJoinedListResponseDto> getJoinedClubs(
            @RequestParam(value = "clubStatus", required = false) ClubStatus clubStatus) {
        return ResponseEntity.ok(clubService.getJoinedClubs(clubStatus));
    }

    @Operation(summary = "클럽 내 게시글 목록 조회", description = "클럽 내의 게시글이나 아카이브를 조회합니다.")
    @GetMapping("/{clubId}/archives")
    public ResponseEntity<ClubArchiveListResponseDto> getClubArchives(
            @PathVariable Long clubId,
            Pageable pageable) {
        return ResponseEntity.ok(clubService.getClubArchives(clubId, pageable));
    }
}

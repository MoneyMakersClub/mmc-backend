package com.mmc.bookduck.domain.bookclub.service;

import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.domain.archive.repository.ExcerptRepository;
import com.mmc.bookduck.domain.archive.repository.ReviewRepository;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.repository.BookInfoRepository;
import com.mmc.bookduck.domain.bookclub.dto.request.ClubCreateRequestDto;
import com.mmc.bookduck.domain.bookclub.dto.response.*;
import com.mmc.bookduck.domain.bookclub.entity.*;
import com.mmc.bookduck.domain.bookclub.repository.ClubMemberReadStatusRepository;
import com.mmc.bookduck.domain.bookclub.repository.ClubMemberRepository;
import com.mmc.bookduck.domain.bookclub.repository.ClubRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.common.BaseTimeEntity;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {
    private final UserService userService;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final BookInfoRepository bookInfoRepository;
    private final ClubMemberReadStatusRepository clubMemberReadStatusRepository;
    private final ClubInviteService clubInviteService;
    private final ExcerptRepository excerptRepository;
    private final ReviewRepository reviewRepository;

    // ① 클럽 생성 + 초대 링크 발급
    public ClubCreateResponseDto createClub(ClubCreateRequestDto requestDto) {
        User currentUser = userService.getCurrentUser();
        BookInfo bookInfo = bookInfoRepository.findById(requestDto.bookInfoId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOOKINFO_NOT_FOUND));

        // 클럽 생성
        Club club = Club.builder()
                .clubName(requestDto.clubName())
                .password(requestDto.password())
                .description(requestDto.description())
                .activeStartAt(requestDto.activeStartAt())
                .activeEndAt(requestDto.activeEndAt())
                .clubStatus(ClubStatus.ACTIVE)
                .bookInfo(bookInfo)
                .maxMember(requestDto.maxMember())
                .build();
        clubRepository.save(club);

        // 방장 등록
        ClubMember leader = ClubMember.builder()
                .club(club)
                .user(currentUser)
                .clubMemberRole(ClubMemberRole.LEADER)
                .build();
        clubMemberRepository.save(leader);

        // 초대 코드 생성 위임
        ClubInvite invite = clubInviteService.createInvite(club.getClubId(), 168); // 168h = 7일

        return ClubCreateResponseDto.from(club, invite);
    }

    @Transactional(readOnly = true)
    public Club getClubById(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ClubArchiveListResponseDto getClubArchives(Long clubId, Pageable pageable) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        // 클럽 멤버 및 현재 사용자 조회
        List<ClubMember> members = clubMemberRepository.findByClub(club);
        User currentUser = userService.getCurrentUser();
        ClubMember currentMember = clubMemberRepository.findByClubAndUser(club, currentUser)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_CLUB_MEMBER));

        // 마지막 읽은 시점
        LocalDateTime lastReadAt = clubMemberReadStatusRepository.findByClubMember(currentMember)
                .map(ClubMemberReadStatus::getLastReadAt)
                .orElse(LocalDateTime.MIN);

        // 클럽 대상 책
        BookInfo targetBook = club.getBookInfo();

        // 유저 ID 목록
        List<Long> memberUserIds = members.stream()
                .map(cm -> cm.getUser().getUserId())
                .toList();

        // Excerpt + Review 모두 조회
        List<Excerpt> excerpts = excerptRepository.findClubExcerpts(
                targetBook.getBookInfoId(),
                memberUserIds,
                club.getActiveStartAt(),
                club.getActiveEndAt()
        );
        List<Review> reviews = reviewRepository.findClubReviews(
                targetBook.getBookInfoId(),
                memberUserIds,
                club.getActiveStartAt(),
                club.getActiveEndAt()
        );

        // 하나로 합치기 (게시물 DTO 변환)
        List<ClubArchiveResponseDto> allPosts = new ArrayList<>();
        excerpts.forEach(e ->
                allPosts.add(ClubArchiveResponseDto.fromExcerpt(e, e.getCreatedTime().isAfter(lastReadAt))));
        reviews.forEach(r ->
                allPosts.add(ClubArchiveResponseDto.fromReview(r, r.getCreatedTime().isAfter(lastReadAt))));

        // 정렬 및 페이지 처리
        allPosts.sort(Comparator.comparing(ClubArchiveResponseDto::createdTime).reversed());
        Page<ClubArchiveResponseDto> dtoPage = new PageImpl<>(allPosts, pageable, allPosts.size());

        return ClubArchiveListResponseDto.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public ClubUnreadSummaryResponseDto getUnreadSummary(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        User currentUser = userService.getCurrentUser();
        ClubMember member = clubMemberRepository.findByClubAndUser(club, currentUser)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_CLUB_MEMBER));

        LocalDateTime lastReadAt = clubMemberReadStatusRepository.findByClubMember(member)
                .map(ClubMemberReadStatus::getLastReadAt)
                .orElse(LocalDateTime.MIN);

        List<Long> memberUserIds = clubMemberRepository.findByClub(club).stream()
                .map(cm -> cm.getUser().getUserId())
                .toList();

        // Excerpt + Review 조회
        List<Excerpt> excerpts = excerptRepository.findClubExcerpts(
                club.getBookInfo().getBookInfoId(), memberUserIds,
                club.getActiveStartAt(), club.getActiveEndAt());
        List<Review> reviews = reviewRepository.findClubReviews(
                club.getBookInfo().getBookInfoId(), memberUserIds,
                club.getActiveStartAt(), club.getActiveEndAt());

        // 모두 합치기
        List<BaseTimeEntity> all = new ArrayList<>();
        all.addAll(excerpts);
        all.addAll(reviews);

        // 안 읽은 것 필터링
        List<BaseTimeEntity> unread = all.stream()
                .filter(a -> a.getCreatedTime().isAfter(lastReadAt))
                .toList();

        BaseTimeEntity latest = all.isEmpty() ? null :
                all.stream().max(Comparator.comparing(BaseTimeEntity::getCreatedTime)).orElse(null);

        return ClubUnreadSummaryResponseDto.from(unread.size(), latest);
    }

    @Transactional(readOnly = true)
    public List<ClubJoinedResponseDto> getJoinedClubs() {
        User currentUser = userService.getCurrentUser();

        // 내가 속한 클럽 목록
        List<ClubMember> memberships = clubMemberRepository.findByUser(currentUser);

        List<ClubJoinedResponseDto> result = new ArrayList<>();

        for (ClubMember membership : memberships) {
            Club club = membership.getClub();

            // 미확인 게시물 요약 계산
            ClubUnreadSummaryResponseDto unreadSummary = getUnreadSummary(club.getClubId());

            int memberCount = Math.toIntExact(clubMemberRepository.countByClub(club));
            String bookTitle = club.getBookInfo().getTitle();

            ClubJoinedResponseDto.LatestPost latestPost = null;
            if (unreadSummary.latestType() != null) {
                latestPost = ClubJoinedResponseDto.LatestPost.builder()
                        .type(unreadSummary.latestType())
                        .id(unreadSummary.latestId())
                        .content(unreadSummary.latestContent())
                        .createdAt(unreadSummary.latestCreatedAt())
                        .build();
            }

            ClubJoinedResponseDto dto = ClubJoinedResponseDto.builder()
                    .clubId(club.getClubId())
                    .clubName(club.getClubName())
                    .bookTitle(bookTitle)
                    .memberCount(memberCount)
                    .unreadCount(unreadSummary.unreadCount())
                    .latestPost(latestPost)
                    .build();

            result.add(dto);
        }

        return result;
    }
}
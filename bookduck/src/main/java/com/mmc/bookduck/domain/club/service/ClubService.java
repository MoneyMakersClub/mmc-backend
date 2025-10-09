package com.mmc.bookduck.domain.club.service;

import com.mmc.bookduck.domain.archive.entity.Excerpt;
import com.mmc.bookduck.domain.archive.entity.Review;
import com.mmc.bookduck.domain.archive.repository.ExcerptRepository;
import com.mmc.bookduck.domain.archive.repository.ReviewRepository;
import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.service.BookInfoService;
import com.mmc.bookduck.domain.club.dto.common.ClubMemberRoleInfo;
import com.mmc.bookduck.domain.club.dto.request.ClubCreateRequestDto;
import com.mmc.bookduck.domain.club.dto.request.ClubJoinRequestDto;
import com.mmc.bookduck.domain.club.dto.request.ClubUpdateRequestDto;
import com.mmc.bookduck.domain.club.dto.response.*;
import com.mmc.bookduck.domain.club.entity.*;
import com.mmc.bookduck.domain.club.repository.ClubMemberReadStatusRepository;
import com.mmc.bookduck.domain.club.repository.ClubRepository;
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
    private final BookInfoService bookInfoService;
    private final ClubMemberReadStatusRepository clubMemberReadStatusRepository;
    private final ExcerptRepository excerptRepository;
    private final ReviewRepository reviewRepository;
    private final ClubMemberService clubMemberService;

    // 클럽 생성
    public ClubCreateResponseDto createClub(ClubCreateRequestDto requestDto) {
        User currentUser = userService.getCurrentUser();
        BookInfo bookInfo = bookInfoService.getBookInfoById(requestDto.bookInfoId());
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
                .allowJoin(true)
                .build();
        clubRepository.save(club);
        // 클럽 리더 생성
        clubMemberService.createClubLeader(club, currentUser);
        return ClubCreateResponseDto.from(club);
    }

    @Transactional(readOnly = true)
    public ClubArchiveListResponseDto getClubArchives(Long clubId, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Club club = getClubById(clubId);
        // 클럽 멤버 및 상태 조회
        ClubMember currentMember = clubMemberService.getClubMemberByClubAndUser(club, currentUser);
        LocalDateTime lastReadAt = getLastReadAt(currentMember);
        BookInfo targetBook = club.getBookInfo();

        // 클럽 멤버 전체 userId 추출
        List<Long> memberUserIds = clubMemberService.getClubMembersByClub(club).stream()
                .map(cm -> cm.getUser().getUserId())
                .toList();

        // 전체 Excerpt / Review 조회 (읽음 여부 필터 제거)
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

        // DTO 변환 + lastReadAt로 읽음 여부 플래그 계산
        List<ClubArchiveResponseDto> allPosts = new ArrayList<>(excerpts.size() + reviews.size());

        for (Excerpt e : excerpts) {
            boolean isUnread = e.getCreatedTime().isAfter(lastReadAt);
            allPosts.add(ClubArchiveResponseDto.fromExcerpt(e, isUnread));
        }
        for (Review r : reviews) {
            boolean isUnread = r.getCreatedTime().isAfter(lastReadAt);
            allPosts.add(ClubArchiveResponseDto.fromReview(r, isUnread));
        }

        // 정렬 및 페이징 (최신순)
        allPosts.sort(Comparator.comparing(ClubArchiveResponseDto::createdTime).reversed());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allPosts.size());
        List<ClubArchiveResponseDto> pageContent = allPosts.subList(start, end);

        Page<ClubArchiveResponseDto> dtoPage = new PageImpl<>(pageContent, pageable, allPosts.size());
        return ClubArchiveListResponseDto.from(dtoPage);
    }

    // TODO: 재확인 필요
    @Transactional(readOnly = true)
    public ClubUnreadSummaryResponseDto getUnreadSummary(Long clubId) {
        User currentUser = userService.getCurrentUser();
        Club club = getClubById(clubId);
        ClubMember currentMember = clubMemberService.getClubMemberByClubAndUser(club, currentUser);
        LocalDateTime lastReadAt = getLastReadAt(currentMember);

        List<Long> memberUserIds = clubMemberService.getMemberUserIds(club);

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

    // 클럽 가입
    public ClubJoinResponseDto joinClub(Long clubId, ClubJoinRequestDto requestDto) {
        User currentUser = userService.getCurrentUser();
        Club club = getClubById(clubId);

        // 클럽 가입 허용 여부 확인
        if (!club.getAllowJoin()) {
            throw new CustomException(ErrorCode.CLUB_JOIN_NOT_ALLOWED);
        }

        // 클럽 상태 확인
        if (club.getClubStatus() != ClubStatus.ACTIVE) {
            throw new CustomException(ErrorCode.CLUB_NOT_ACTIVE);
        }

        // 비밀번호 확인 (비밀번호가 설정된 경우)
        if (club.getPassword() != null && !club.getPassword().equals(requestDto.password())) {
            throw new CustomException(ErrorCode.CLUB_PASSWORD_INCORRECT);
        }

        ClubMember clubMember = clubMemberService.joinToClub(club, currentUser);
        return ClubJoinResponseDto.from(club, clubMember);
    }

    @Transactional(readOnly = true)
    public List<ClubJoinedResponseDto> getJoinedClubs() {
        User currentUser = userService.getCurrentUser();

        // 내가 속한 클럽 목록
        List<ClubMember> memberships = clubMemberService.getClubMembersByUser(currentUser);

        List<ClubJoinedResponseDto> result = new ArrayList<>();

        for (ClubMember membership : memberships) {
            Club club = membership.getClub();

            // 미확인 게시물 요약 계산
            ClubUnreadSummaryResponseDto unreadSummary = getUnreadSummary(club.getClubId());

            int memberCount = Math.toIntExact(clubMemberService.countByClub(club));
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

    // 클럽 검색
    @Transactional(readOnly = true)
    public ClubSearchListResponseDto searchClubs(String keyword, ClubStatus status, Pageable pageable) {
        Page<Club> clubPage = clubRepository.searchClubs(keyword.trim(), status, pageable);
        // Club을 ClubSearchResponseDto로 변환
        Page<ClubSearchResponseDto> dtoPage = clubPage.map(ClubSearchResponseDto::from);
        return ClubSearchListResponseDto.from(dtoPage);
    }

    // 클럽 상세 조회
    @Transactional(readOnly = true)
    public ClubDetailResponseDto getClubDetail(Long clubId) {
        Club club = getClubById(clubId);
        User currentUser = userService.getCurrentUser();
        // 멤버 여부 및 역할을 서비스에 위임
        ClubMemberRoleInfo roleInfo = clubMemberService.getMemberRoleInfo(club, currentUser);
        return ClubDetailResponseDto.from(club, roleInfo.isMember(), roleInfo.memberRole());
    }

    // 클럽 정보 수정
    public ClubUpdateResponseDto updateClub(Long clubId, ClubUpdateRequestDto requestDto) {
        Club club = getClubById(clubId);
        User currentUser = userService.getCurrentUser();
        ClubMember member = clubMemberService.getClubMemberByClubAndUser(club, currentUser);
        // LEADER만 수정 가능
        if (member.getClubMemberRole() != ClubMemberRole.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
        // 정보 업데이트
        if (requestDto.clubName() != null && !requestDto.clubName().trim().isEmpty()) {
            club.updateClubName(requestDto.clubName().trim());
        }
        if (requestDto.description() != null) {
            club.updateDescription(requestDto.description());
        }
        if (requestDto.password() != null) {
            club.updatePassword(requestDto.password());
        }
        if (requestDto.activeStartAt() != null) {
            club.updateActiveStartAt(requestDto.activeStartAt());
        }
        if (requestDto.activeEndAt() != null) {
            club.updateActiveEndAt(requestDto.activeEndAt());
        }
        if (requestDto.maxMember() != null) {
            club.updateMaxMember(requestDto.maxMember());
        }
        if (requestDto.allowJoin() != null) {
            club.updateAllowJoin(requestDto.allowJoin());
        }

        clubRepository.save(club);
        return ClubUpdateResponseDto.from(club);
    }

    // 클럽 삭제 (비활성화)
    public void deleteClub(Long clubId) {
        Club club = getClubById(clubId);

        User currentUser = userService.getCurrentUser();
        ClubMember member = clubMemberService.getClubMemberByClubAndUser(club, currentUser);

        // LEADER만 삭제 가능
        if (member.getClubMemberRole() != ClubMemberRole.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        // 클럽 상태를 DELETED로 변경 (실제 삭제는 하지 않음)
        club.updateStatus(ClubStatus.DELETED);
        clubRepository.save(club);
    }

    // 클럽 멤버 목록 조회
    @Transactional(readOnly = true)
    public ClubMembersResponseDto getClubMembers(Long clubId) {
        Club club = getClubById(clubId);
        User currentUser = userService.getCurrentUser();

        List<ClubMember> members = clubMemberService.getClubMembersByClub(club);
        List<ClubMemberResponseDto> memberDtos = members.stream()
                .map(ClubMemberResponseDto::from)
                .toList();
        return ClubMembersResponseDto.from(club.getClubId(), club.getClubName(), memberDtos);
    }

    // 클럽 탈퇴
    public void leaveClub(Long clubId) {
        Club club = getClubById(clubId);
        User currentUser = userService.getCurrentUser();
        ClubMember member = clubMemberService.getClubMemberByClubAndUser(club, currentUser);
        clubMemberService.deleteClubMember(member);
    }

    // 클럽 멤버 강퇴
    public void removeMember(Long clubId, Long memberId) {
        Club club = getClubById(clubId);

        User currentUser = userService.getCurrentUser();
        ClubMember currentMember =clubMemberService.getClubMemberByClubAndUser(club, currentUser);

        // LEADER만 강퇴 가능
        if (currentMember.getClubMemberRole() != ClubMemberRole.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        ClubMember targetMember = clubMemberService.getClubMemberById(memberId);

        // 같은 클럽의 멤버인지 확인
        if (!targetMember.getClub().getClubId().equals(clubId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

        // LEADER는 강퇴할 수 없음
        if (targetMember.getClubMemberRole() == ClubMemberRole.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
        clubMemberService.deleteClubMember(targetMember);
    }

    @Transactional(readOnly = true)
    public Club getClubById(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));
    }

    // 마지막 읽은 시점
    @Transactional(readOnly = true)
    public LocalDateTime getLastReadAt(ClubMember clubMember) {
        return clubMemberReadStatusRepository.findByClubMember(clubMember)
                .map(ClubMemberReadStatus::getLastReadAt)
                .orElse(LocalDateTime.MIN);
    }
}
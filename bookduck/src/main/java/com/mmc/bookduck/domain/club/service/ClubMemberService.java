package com.mmc.bookduck.domain.club.service;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.book.repository.UserBookRepository;
import com.mmc.bookduck.domain.club.dto.common.ClubMemberRoleInfo;
import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubMember;
import com.mmc.bookduck.domain.club.entity.ClubMemberReadStatus;
import com.mmc.bookduck.domain.club.entity.ClubMemberRole;
import com.mmc.bookduck.domain.club.repository.ClubMemberReadStatusRepository;
import com.mmc.bookduck.domain.club.repository.ClubMemberRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final UserBookRepository userBookRepository;
    private final ClubMemberReadStatusRepository clubMemberReadStatusRepository;

    // 클럽 멤버 생성
    private ClubMember createClubMemberAndAddUserBook(Club club, BookInfo bookInfo, User user, ClubMemberRole role) {
        ClubMember clubMember = ClubMember.builder()
                .club(club)
                .user(user)
                .clubMemberRole(role)
                .build();
        ClubMember savedClubMember = clubMemberRepository.save(clubMember);
        
        // 읽음 상태 저장
        ClubMemberReadStatus clubMemberReadStatus = new ClubMemberReadStatus(savedClubMember, LocalDateTime.now());
        clubMemberReadStatusRepository.save(clubMemberReadStatus);
        
        // UserBook 갱신 혹은 생성
        Optional<UserBook> existingUserBook = userBookRepository.findByUserAndBookInfo(user, bookInfo);
        if (existingUserBook.isEmpty()) {
            UserBook userBook = new UserBook(ReadStatus.READING, user, bookInfo);
            userBookRepository.save(userBook);
        } else {
            UserBook userBook = existingUserBook.get();
            if (userBook.getReadStatus() == ReadStatus.NOT_STARTED) {
                userBook.changeReadStatus(ReadStatus.READING);
                userBookRepository.save(userBook);
            }
        }
        return savedClubMember;
    }

    // 클럽 리더 생성
    public void addLeaderToClub(Club club, BookInfo bookInfo, User user) {
        createClubMemberAndAddUserBook(club, bookInfo, user, ClubMemberRole.LEADER);
    }

    // 클럽 가입하기
    public ClubMember addMemberToClub(Club club, BookInfo bookInfo, User user) {
        // 이미 가입한 멤버인지 확인
        boolean alreadyJoined = clubMemberRepository.existsByClubAndUser(club, user);
        if (alreadyJoined) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_CLUB);
        }
        // 클럽 가입
        return createClubMemberAndAddUserBook(club, bookInfo, user, ClubMemberRole.MEMBER);
    }

    // 클럽 멤버 삭제
    public void deleteClubMember(ClubMember member) {
        clubMemberRepository.delete(member); // cascade REMOVE로 ClubMember, ClubMemberReadStatus 자동 삭제
    }

    @Transactional(readOnly = true)
    public List<Long> getMemberUserIds(Club club) {
        return clubMemberRepository.findByClub(club).stream()
                .map(cm -> cm.getUser().getUserId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClubMember> getClubMembersByClub(Club club) {
        return clubMemberRepository.findByClub(club);
    }

    @Transactional(readOnly = true)
    public long countByClub(Club club) {
        return clubMemberRepository.countByClub(club);
    }

    @Transactional(readOnly = true)
    public List<ClubMember> getClubMembersByUser(User user) {
        return clubMemberRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public ClubMember getClubMemberByClubAndUser(Club club, User user) {
        return clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(()-> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ClubMemberRoleInfo getMemberRoleInfo(Club club, User user) {
        return clubMemberRepository.findByClubAndUser(club, user)
                .map(clubMember -> new ClubMemberRoleInfo(true, clubMember.getClubMemberRole().name()))
                .orElseGet(() -> new ClubMemberRoleInfo(false, null));
    }

    @Transactional(readOnly = true)
    public ClubMember getClubMemberById(Long memberId) {
        return clubMemberRepository.findById(memberId)
                .orElseThrow(()-> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND));
    }

    public long countByClubForUpdate(Club club) {
        return clubMemberRepository.countByClubForUpdate(club.getClubId());
    }
}

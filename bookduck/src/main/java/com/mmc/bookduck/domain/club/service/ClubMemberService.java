package com.mmc.bookduck.domain.club.service;

import com.mmc.bookduck.domain.book.entity.BookInfo;
import com.mmc.bookduck.domain.book.entity.ReadStatus;
import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.book.repository.UserBookRepository;
import com.mmc.bookduck.domain.book.service.BookInfoService;
import com.mmc.bookduck.domain.club.dto.common.ClubMemberRoleInfo;
import com.mmc.bookduck.domain.club.entity.Club;
import com.mmc.bookduck.domain.club.entity.ClubMember;
import com.mmc.bookduck.domain.club.entity.ClubMemberRole;
import com.mmc.bookduck.domain.club.repository.ClubMemberRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final UserBookRepository userBookRepository;

    /**
     * Create and persist a ClubMember with the given role and ensure the user's UserBook for the provided BookInfo
     * exists and has a read status of READING.
     *
     * If no UserBook exists for the user and BookInfo, one is created with ReadStatus.READING. If an existing
     * UserBook has ReadStatus.NOT_STARTED, its status is changed to ReadStatus.READING.
     *
     * @param club the club to join
     * @param bookInfo the book information associated with the club membership
     * @param user the user to add as a club member
     * @param role the role to assign to the new club member
     * @return the persisted ClubMember
     */
    private ClubMember createClubMemberAndAddUserBook(Club club, BookInfo bookInfo, User user, ClubMemberRole role) {
        ClubMember clubMember = ClubMember.builder()
                .club(club)
                .user(user)
                .clubMemberRole(role)
                .build();
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
        return clubMemberRepository.save(clubMember);
    }

    /**
     * Create and persist a club member with the LEADER role and ensure the user's UserBook for the club's book is created or updated.
     *
     * Ensures a ClubMember with role LEADER is saved for the given club and user; also creates a UserBook for the provided BookInfo with ReadStatus.READING if one does not exist or updates an existing UserBook with status NOT_STARTED to READING.
     *
     * @param club the club to which the leader will belong
     * @param bookInfo information about the book associated with the club
     * @param user the user to assign as club leader
     * @return the persisted ClubMember representing the new club leader
     */
    public ClubMember createClubLeader(Club club, BookInfo bookInfo, User user) {
        return createClubMemberAndAddUserBook(club, bookInfo, user, ClubMemberRole.LEADER);
    }

    /**
     * Add a user to the given club with the MEMBER role and ensure the user's UserBook for the club's book is created or updated.
     *
     * @param bookInfo the book information for the club's current book; used to create or update the user's UserBook
     * @return the persisted ClubMember representing the new membership
     * @throws CustomException with ErrorCode.ALREADY_JOINED_CLUB if the user is already a member
     * @throws CustomException with ErrorCode.CLUB_FULL if the club has reached its maximum member capacity
     */
    public ClubMember joinToClub(Club club, BookInfo bookInfo, User user) {
        // 이미 가입한 멤버인지 확인
        boolean alreadyJoined = clubMemberRepository.existsByClubAndUser(club, user);
        if (alreadyJoined) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_CLUB);
        }
        // 최대 인원 확인
        int memberCount = Math.toIntExact(clubMemberRepository.countByClub(club));
        if (memberCount >= club.getMaxMember()) {
            throw new CustomException(ErrorCode.CLUB_FULL);
        }
        // 클럽 가입
        return createClubMemberAndAddUserBook(club, bookInfo, user, ClubMemberRole.MEMBER);
    }

    // 클럽 멤버 삭제
    public void deleteClubMember(ClubMember member) {
        // LEADER인 클럽 멤버는 삭제될 수 없음
        if (member.getClubMemberRole() == ClubMemberRole.LEADER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
        clubMemberRepository.delete(member);
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
}
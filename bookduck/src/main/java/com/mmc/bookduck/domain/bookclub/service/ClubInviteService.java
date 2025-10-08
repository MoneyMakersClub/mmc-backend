package com.mmc.bookduck.domain.bookclub.service;

import com.mmc.bookduck.domain.bookclub.dto.response.ClubInviteAcceptResponseDto;
import com.mmc.bookduck.domain.bookclub.dto.response.ClubInviteValidationResponseDto;
import com.mmc.bookduck.domain.bookclub.entity.Club;
import com.mmc.bookduck.domain.bookclub.entity.ClubInvite;
import com.mmc.bookduck.domain.bookclub.entity.ClubMember;
import com.mmc.bookduck.domain.bookclub.entity.ClubMemberRole;
import com.mmc.bookduck.domain.bookclub.repository.ClubInviteRepository;
import com.mmc.bookduck.domain.bookclub.repository.ClubMemberRepository;
import com.mmc.bookduck.domain.bookclub.repository.ClubRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubInviteService {
    private final ClubInviteRepository clubInviteRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
    private final UserService userService;
    private final InviteCodeGenerator inviteCodeGenerator;

    // ① 초대 링크 생성
    public ClubInvite createInvite(Long clubId, int expireHours) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));
        String inviteCode = inviteCodeGenerator.generateInviteCode();
        LocalDateTime expiresAt = inviteCodeGenerator.defaultExpiry(expireHours);

        ClubInvite invite = ClubInvite.builder()
                .inviteCode(inviteCode)
                .club(club)
                .expiresAt(expiresAt)
                .useCount(0)
                .isActive(true)
                .build();
        return clubInviteRepository.save(invite);
    }

    // ② 초대 링크 검증 + 클럽 정보 조회
    @Transactional(readOnly = true)
    public ClubInviteValidationResponseDto validateInvite(String inviteCode) {
        ClubInvite invite = clubInviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_INVITE_NOT_FOUND));

        if (!invite.getIsActive())
            throw new CustomException(ErrorCode.INVITE_INACTIVE);
        if (invite.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new CustomException(ErrorCode.INVITE_EXPIRED);

        Club club = invite.getClub();
        int memberCount = Math.toIntExact(clubMemberRepository.countByClub(club));
        if (memberCount >= club.getMaxMember())
            throw new CustomException(ErrorCode.CLUB_FULL);

        return ClubInviteValidationResponseDto.from(club, invite);
    }

    /**
     * ③ 초대 수락 및 클럽 가입
     */
    public ClubInviteAcceptResponseDto acceptInvite(String inviteCode) {
        User currentUser = userService.getCurrentUser();
        ClubInvite invite = clubInviteRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_INVITE_NOT_FOUND));

        if (!invite.getIsActive() || invite.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new CustomException(ErrorCode.INVITE_EXPIRED);

        Club club = invite.getClub();

        boolean alreadyJoined = clubMemberRepository.existsByClubAndUser(club, currentUser);
        if (alreadyJoined)
            throw new CustomException(ErrorCode.ALREADY_JOINED_CLUB);

        int memberCount = Math.toIntExact(clubMemberRepository.countByClub(club));
        if (memberCount >= club.getMaxMember())
            throw new CustomException(ErrorCode.CLUB_FULL);

        // 클럽 가입
        ClubMember member = ClubMember.builder()
                .club(club)
                .user(currentUser)
                .clubMemberRole(ClubMemberRole.MEMBER)
                .build();
        clubMemberRepository.save(member);

        // 초대 사용 횟수 증가
        invite.incrementUseCount();
        clubInviteRepository.save(invite);

        return ClubInviteAcceptResponseDto.from(club, member);
    }
}

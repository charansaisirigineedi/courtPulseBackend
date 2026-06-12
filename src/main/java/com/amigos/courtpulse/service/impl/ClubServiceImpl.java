package com.amigos.courtpulse.service.impl;

import com.amigos.courtpulse.dto.club.ClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.ClubMemberResponse;
import com.amigos.courtpulse.dto.club.ClubProfileResponse;
import com.amigos.courtpulse.dto.club.ClubSearchResponse;
import com.amigos.courtpulse.dto.club.CreateClubRequest;
import com.amigos.courtpulse.dto.club.LeaveClubResponse;
import com.amigos.courtpulse.dto.club.MyClubJoinRequestResponse;
import com.amigos.courtpulse.dto.club.MyClubResponse;
import com.amigos.courtpulse.entity.Club;
import com.amigos.courtpulse.entity.ClubJoinRequest;
import com.amigos.courtpulse.entity.ClubMember;
import com.amigos.courtpulse.enums.ClubJoinRequestStatusEnum;
import com.amigos.courtpulse.enums.ClubMemberRoleEnum;
import com.amigos.courtpulse.enums.ClubStatusEnum;
import com.amigos.courtpulse.exception.ClubAccessDeniedException;
import com.amigos.courtpulse.exception.ClubAlreadyMemberException;
import com.amigos.courtpulse.exception.ClubJoinRequestAlreadyExistsException;
import com.amigos.courtpulse.exception.ClubJoinRequestAlreadyReviewedException;
import com.amigos.courtpulse.exception.ClubJoinRequestNotFoundException;
import com.amigos.courtpulse.exception.ClubMembershipNotFoundException;
import com.amigos.courtpulse.exception.ClubNotFoundException;
import com.amigos.courtpulse.exception.ClubOwnerCannotLeaveException;
import com.amigos.courtpulse.mapper.ClubMapper;
import com.amigos.courtpulse.repository.ClubJoinRequestRepository;
import com.amigos.courtpulse.repository.ClubMemberRepository;
import com.amigos.courtpulse.repository.ClubRepository;
import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.exception.PlayerNotFoundException;
import com.amigos.courtpulse.repository.PlayerRepository;
import com.amigos.courtpulse.service.ClubService;
import com.amigos.courtpulse.util.ClubCodeGenerator;
import com.amigos.courtpulse.util.ObjectUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private static final Logger log = LoggerFactory.getLogger(ClubServiceImpl.class);
    private static final int SEARCH_LIMIT = 20;
    private static final List<ClubMemberRoleEnum> REVIEWER_ROLES = List.of(
            ClubMemberRoleEnum.OWNER,
            ClubMemberRoleEnum.ADMIN
    );

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubJoinRequestRepository clubJoinRequestRepository;
    private final PlayerRepository playerRepository;
    private final ClubCodeGenerator clubCodeGenerator;
    private final ClubMapper clubMapper;

    @Override
    @Transactional
    public ClubProfileResponse createClub(CreateClubRequest request, String ownerPlayerCode) {
        Player owner = findPlayerByCode(ownerPlayerCode);

        Club club = Club.builder()
                .clubCode(clubCodeGenerator.generateUniqueCode())
                .clubName(normalizeRequired(request.clubName()))
                .description(normalizeOptional(request.description()))
                .owner(owner)
                .status(ClubStatusEnum.ACTIVE)
                .build();

        Club savedClub = clubRepository.save(club);
        ClubMember ownerMembership = ClubMember.builder()
                .club(savedClub)
                .player(owner)
                .role(ClubMemberRoleEnum.OWNER)
                .build();
        clubMemberRepository.save(ownerMembership);

        log.info("Created club {} owned by player {}", savedClub.getClubCode(), owner.getPlayerCode());
        return clubMapper.toProfileResponse(savedClub);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubProfileResponse getClubByCode(String clubCode) {
        return clubMapper.toProfileResponse(findClubByCode(clubCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubSearchResponse> searchClubs(String query) {
        String normalizedQuery = normalizeOptional(query);
        if (ObjectUtil.isNull(normalizedQuery)) {
            return List.of();
        }

        Pageable limit = PageRequest.of(0, SEARCH_LIMIT);
        return clubRepository.searchClubs(normalizedQuery, limit)
                .stream()
                .map(clubMapper::toSearchResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyClubResponse> getMyClubs(String playerCode) {
        Player player = findPlayerByCode(playerCode);
        return clubMemberRepository.findByPlayerIdOrderByJoinedAtDesc(player.getId())
                .stream()
                .map(clubMapper::toMyClubResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyClubJoinRequestResponse> getMyJoinRequests(String playerCode) {
        Player player = findPlayerByCode(playerCode);
        return clubJoinRequestRepository.findByPlayerIdOrderByCreatedAtDesc(player.getId())
                .stream()
                .map(clubMapper::toMyJoinRequestResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClubJoinRequestResponse createJoinRequest(String clubCode, String playerCode) {
        Club club = findClubByCode(clubCode);
        Player player = findPlayerByCode(playerCode);

        if (clubMemberRepository.existsByClubIdAndPlayerId(club.getId(), player.getId())) {
            throw new ClubAlreadyMemberException(player.getPlayerCode(), club.getClubCode());
        }
        if (clubJoinRequestRepository.existsByClubIdAndPlayerId(club.getId(), player.getId())) {
            throw new ClubJoinRequestAlreadyExistsException(player.getPlayerCode(), club.getClubCode());
        }

        ClubJoinRequest joinRequest = ClubJoinRequest.builder()
                .club(club)
                .player(player)
                .status(ClubJoinRequestStatusEnum.PENDING)
                .build();

        ClubJoinRequest savedRequest = clubJoinRequestRepository.save(joinRequest);
        log.info("Created club join request {} for club {}", savedRequest.getId(), club.getClubCode());
        return clubMapper.toJoinRequestResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubJoinRequestResponse> getPendingJoinRequests(String clubCode, String reviewerPlayerCode) {
        Club club = findClubByCode(clubCode);
        Player reviewer = findPlayerByCode(reviewerPlayerCode);
        validateReviewer(club, reviewer);

        return clubJoinRequestRepository
                .findByClubIdAndStatusOrderByCreatedAtAsc(club.getId(), ClubJoinRequestStatusEnum.PENDING)
                .stream()
                .map(clubMapper::toJoinRequestResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClubJoinRequestResponse approveJoinRequest(Long requestId, String reviewerPlayerCode) {
        ClubJoinRequest joinRequest = findJoinRequestById(requestId);
        Player reviewer = findPlayerByCode(reviewerPlayerCode);
        validateReviewer(joinRequest.getClub(), reviewer);
        validatePending(joinRequest);

        joinRequest.setStatus(ClubJoinRequestStatusEnum.APPROVED);
        joinRequest.setReviewedBy(reviewer);
        joinRequest.setReviewedAt(LocalDateTime.now());

        if (!clubMemberRepository.existsByClubIdAndPlayerId(
                joinRequest.getClub().getId(),
                joinRequest.getPlayer().getId()
        )) {
            ClubMember clubMember = ClubMember.builder()
                    .club(joinRequest.getClub())
                    .player(joinRequest.getPlayer())
                    .role(ClubMemberRoleEnum.MEMBER)
                    .build();
            clubMemberRepository.save(clubMember);
        }

        ClubJoinRequest savedRequest = clubJoinRequestRepository.save(joinRequest);
        log.info("Approved club join request {}", savedRequest.getId());
        return clubMapper.toJoinRequestResponse(savedRequest);
    }

    @Override
    @Transactional
    public ClubJoinRequestResponse rejectJoinRequest(Long requestId, String reviewerPlayerCode) {
        ClubJoinRequest joinRequest = findJoinRequestById(requestId);
        Player reviewer = findPlayerByCode(reviewerPlayerCode);
        validateReviewer(joinRequest.getClub(), reviewer);
        validatePending(joinRequest);

        joinRequest.setStatus(ClubJoinRequestStatusEnum.REJECTED);
        joinRequest.setReviewedBy(reviewer);
        joinRequest.setReviewedAt(LocalDateTime.now());

        ClubJoinRequest savedRequest = clubJoinRequestRepository.save(joinRequest);
        log.info("Rejected club join request {}", savedRequest.getId());
        return clubMapper.toJoinRequestResponse(savedRequest);
    }

    @Override
    @Transactional
    public LeaveClubResponse leaveClub(String clubCode, String playerCode) {
        Club club = findClubByCode(clubCode);
        Player player = findPlayerByCode(playerCode);
        ClubMember clubMember = clubMemberRepository.findByClubIdAndPlayerId(club.getId(), player.getId())
                .orElseThrow(() -> new ClubMembershipNotFoundException(player.getPlayerCode(), club.getClubCode()));

        if (ObjectUtil.isEqual(clubMember.getRole(), ClubMemberRoleEnum.OWNER)) {
            throw new ClubOwnerCannotLeaveException(player.getPlayerCode(), club.getClubCode());
        }

        clubMemberRepository.delete(clubMember);
        log.info("Player {} left club {}", player.getPlayerCode(), club.getClubCode());
        return new LeaveClubResponse(club.getClubCode(), player.getPlayerCode(), "LEFT");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMemberResponse> getClubMembers(String clubCode) {
        Club club = findClubByCode(clubCode);
        return clubMemberRepository.findByClubId(club.getId())
                .stream()
                .map(clubMapper::toMemberResponse)
                .toList();
    }

    private Club findClubByCode(String clubCode) {
        String normalizedClubCode = normalizeRequired(clubCode).toUpperCase(Locale.ROOT);
        return clubRepository.findByClubCode(normalizedClubCode)
                .orElseThrow(() -> new ClubNotFoundException(normalizedClubCode));
    }

    private Player findPlayerByCode(String playerCode) {
        String normalizedPlayerCode = normalizeRequired(playerCode).toUpperCase(Locale.ROOT);
        return playerRepository.findByPlayerCode(normalizedPlayerCode)
                .orElseThrow(() -> new PlayerNotFoundException(normalizedPlayerCode));
    }

    private ClubJoinRequest findJoinRequestById(Long requestId) {
        return clubJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ClubJoinRequestNotFoundException(requestId));
    }

    private void validateReviewer(Club club, Player reviewer) {
        boolean allowed = clubMemberRepository.existsByClubIdAndPlayerIdAndRoleIn(
                club.getId(),
                reviewer.getId(),
                REVIEWER_ROLES
        );
        if (!allowed) {
            throw new ClubAccessDeniedException(reviewer.getPlayerCode(), club.getClubCode());
        }
    }

    private void validatePending(ClubJoinRequest joinRequest) {
        if (ObjectUtil.isNotEqual(joinRequest.getStatus(), ClubJoinRequestStatusEnum.PENDING)) {
            throw new ClubJoinRequestAlreadyReviewedException(joinRequest.getId());
        }
    }

    private String normalizeRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Required value must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

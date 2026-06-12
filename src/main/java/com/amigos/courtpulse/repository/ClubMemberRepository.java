package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.ClubMember;
import com.amigos.courtpulse.enums.ClubMemberRoleEnum;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    boolean existsByClubIdAndPlayerId(Long clubId, Long playerId);

    Optional<ClubMember> findByClubIdAndPlayerId(Long clubId, Long playerId);

    boolean existsByClubIdAndPlayerIdAndRoleIn(
            Long clubId,
            Long playerId,
            Collection<ClubMemberRoleEnum> roles
    );

    List<ClubMember> findByClubId(Long clubId);

    @EntityGraph(attributePaths = "club")
    Page<ClubMember> findByPlayerIdOrderByJoinedAtDesc(Long playerId, Pageable pageable);
}

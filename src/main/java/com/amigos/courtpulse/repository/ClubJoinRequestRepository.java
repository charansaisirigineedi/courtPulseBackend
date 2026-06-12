package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.ClubJoinRequest;
import com.amigos.courtpulse.enums.ClubJoinRequestStatusEnum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubJoinRequestRepository extends JpaRepository<ClubJoinRequest, Long> {

    Optional<ClubJoinRequest> findByClubIdAndPlayerId(Long clubId, Long playerId);

    boolean existsByClubIdAndPlayerId(Long clubId, Long playerId);

    List<ClubJoinRequest> findByClubIdAndStatusOrderByCreatedAtAsc(
            Long clubId,
            ClubJoinRequestStatusEnum status
    );

    @EntityGraph(attributePaths = "club")
    List<ClubJoinRequest> findByPlayerIdOrderByCreatedAtDesc(Long playerId);
}

package com.amigos.courtpulse.repository;

import com.amigos.courtpulse.entity.Club;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByClubCode(String clubCode);

    boolean existsByClubCode(String clubCode);

    @Query("""
            SELECT c
            FROM Club c
            WHERE LOWER(c.clubCode) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.clubName) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY c.clubName ASC, c.clubCode ASC
            """)
    List<Club> searchClubs(@Param("query") String query, Pageable pageable);
}

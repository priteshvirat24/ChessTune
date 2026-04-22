package com.chesstune.core.repository;

import com.chesstune.core.entity.Tournament;
import com.chesstune.core.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatusOrderByStartTimeAsc(TournamentStatus status);

    List<Tournament> findByStatusInOrderByStartTimeAsc(List<TournamentStatus> statuses);
}

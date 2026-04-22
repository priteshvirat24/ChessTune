package com.chesstune.core.repository;

import com.chesstune.core.entity.UpsolveTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpsolveTaskRepository extends JpaRepository<UpsolveTask, Long> {

    List<UpsolveTask> findByUserIdAndIsSolvedFalse(Long userId);

    List<UpsolveTask> findByUserId(Long userId);

    long countByUserIdAndIsSolvedFalse(Long userId);
}

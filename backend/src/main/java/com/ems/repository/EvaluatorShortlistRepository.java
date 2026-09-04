package com.ems.repository;
import com.ems.entity.EvaluatorShortlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EvaluatorShortlistRepository extends JpaRepository<EvaluatorShortlist, Long> {
    List<EvaluatorShortlist> findByCohortCohortId(Long cohortId);
    Optional<EvaluatorShortlist> findByCohortCohortIdAndEvaluatorEvaluatorId(Long cohortId, Long evaluatorId);
}

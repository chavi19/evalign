package com.ems.repository;
import com.ems.entity.EvaluatorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluatorMappingRepository extends JpaRepository<EvaluatorMapping, Long> {
    List<EvaluatorMapping> findByCohortCohortIdAndRound(Long cohortId, String round);
    List<EvaluatorMapping> findByCohortCohortId(Long cohortId);
    List<EvaluatorMapping> findByCandidateCandidateId(Long candidateId);
    long countByEvaluatorEvaluatorIdAndRound(Long evaluatorId, String round);
}

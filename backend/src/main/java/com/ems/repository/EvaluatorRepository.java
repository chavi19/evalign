package com.ems.repository;
import com.ems.entity.Evaluator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EvaluatorRepository extends JpaRepository<Evaluator, Long> {
    Optional<Evaluator> findByEmpId(String empId);
    
    @Query("SELECT DISTINCT e.vertical FROM Evaluator e WHERE e.vertical IS NOT NULL")
    List<String> findDistinctVerticals();
    
    @Query("SELECT DISTINCT e.domain FROM Evaluator e WHERE e.domain IS NOT NULL")
    List<String> findDistinctDomains();
}

package com.ems.config;

import com.ems.entity.*;
import com.ems.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CohortRepository cohortRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private EvaluatorRepository evaluatorRepository;
    
    @Autowired
    private EvaluatorShortlistRepository shortlistRepository;
    
    @Autowired
    private EvaluatorMappingRepository mappingRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedData() {
        if (userRepository.count() > 0) return;

        // 1. User
        User poc = new User();
        poc.setName("Vijayalakshmi J");
        poc.setEmail("admin@example.com");
        poc.setPasswordHash(passwordEncoder.encode("admin123"));
        poc.setRole("POC");
        poc = userRepository.save(poc);

        // 2. Cohorts
        Cohort batch1 = new Cohort();
        batch1.setCohortName("Batch-1");
        batch1.setBatchCode("DS Track");
        batch1.setPoc(poc);
        batch1.setCandidateCount(4);
        batch1.setStartDate(LocalDate.now());
        batch1.setStatus("ACTIVE");
        batch1 = cohortRepository.save(batch1);

        Cohort batch2 = new Cohort();
        batch2.setCohortName("Batch-2");
        batch2.setBatchCode("Full Stack");
        batch2.setPoc(poc);
        batch2.setCandidateCount(3);
        batch2.setStartDate(LocalDate.now());
        batch2.setStatus("ACTIVE");
        batch2 = cohortRepository.save(batch2);

        Cohort batch3 = new Cohort();
        batch3.setCohortName("Batch-3");
        batch3.setBatchCode("Cloud");
        batch3.setPoc(poc);
        batch3.setCandidateCount(3);
        batch3.setStartDate(LocalDate.now());
        batch3.setStatus("ACTIVE");
        batch3 = cohortRepository.save(batch3);

        // 3. Candidates
        Candidate c1 = new Candidate(); c1.setCohort(batch1); c1.setCandidateName("Priya Sharma"); c1 = candidateRepository.save(c1);
        Candidate c2 = new Candidate(); c2.setCohort(batch1); c2.setCandidateName("Rahul Verma"); c2 = candidateRepository.save(c2);
        Candidate c3 = new Candidate(); c3.setCohort(batch1); c3.setCandidateName("Aisha Khan"); c3 = candidateRepository.save(c3);
        Candidate c4 = new Candidate(); c4.setCohort(batch1); c4.setCandidateName("Meera Iqbal"); c4 = candidateRepository.save(c4);
        
        Candidate c5 = new Candidate(); c5.setCohort(batch2); c5.setCandidateName("John Doe"); candidateRepository.save(c5);
        Candidate c6 = new Candidate(); c6.setCohort(batch2); c6.setCandidateName("Jane Smith"); candidateRepository.save(c6);
        Candidate c7 = new Candidate(); c7.setCohort(batch3); c7.setCandidateName("Alice Johnson"); candidateRepository.save(c7);

        // 4. Evaluators
        Evaluator e1 = new Evaluator(); e1.setEmpId("EMP-1042"); e1.setName("Rahul Menon"); e1.setVertical("Finance"); e1.setDomain("Corporate Banking"); e1.setAvailabilityStatus("AVAILABLE"); e1 = evaluatorRepository.save(e1);
        Evaluator e2 = new Evaluator(); e2.setEmpId("EMP-1058"); e2.setName("Sneha Iyer"); e2.setVertical("Finance"); e2.setDomain("Risk & Audit"); e2.setAvailabilityStatus("UNAVAILABLE"); e2.setUnavailableFrom(LocalDate.now()); e2.setUnavailableTo(LocalDate.now().plusDays(5)); e2.setStatusReason("On Leave"); e2.setIsPermanent(false); e2 = evaluatorRepository.save(e2);
        Evaluator e3 = new Evaluator(); e3.setEmpId("EMP-1105"); e3.setName("Arjun Verma"); e3.setVertical("Insurance"); e3.setDomain("Underwriting"); e3.setAvailabilityStatus("AVAILABLE"); e3 = evaluatorRepository.save(e3);
        Evaluator e4 = new Evaluator(); e4.setEmpId("EMP-1128"); e4.setName("Divya Krishnan"); e4.setVertical("Insurance"); e4.setDomain("Claims"); e4.setAvailabilityStatus("AVAILABLE"); e4 = evaluatorRepository.save(e4);
        Evaluator e5 = new Evaluator(); e5.setEmpId("EMP-1134"); e5.setName("Karthik Raghavan"); e5.setVertical("Health"); e5.setDomain("Clinical Ops"); e5.setAvailabilityStatus("UNAVAILABLE"); e5.setUnavailableFrom(LocalDate.now()); e5.setUnavailableTo(LocalDate.now().plusDays(2)); e5.setStatusReason("Client Project Deadline"); e5.setIsPermanent(false); e5 = evaluatorRepository.save(e5);
        Evaluator e6 = new Evaluator(); e6.setEmpId("EMP-1156"); e6.setName("Priya Nair"); e6.setVertical("Health"); e6.setDomain("Pharma Compliance"); e6.setAvailabilityStatus("AVAILABLE"); e6 = evaluatorRepository.save(e6);
        Evaluator e7 = new Evaluator(); e7.setEmpId("EMP-1172"); e7.setName("Vishal Sharma"); e7.setVertical("IT"); e7.setDomain("Cloud Infra"); e7.setAvailabilityStatus("AVAILABLE"); e7 = evaluatorRepository.save(e7);
        Evaluator e8 = new Evaluator(); e8.setEmpId("EMP-1089"); e8.setName("S. Prakash"); e8.setVertical("IT"); e8.setDomain("Data Science"); e8.setAvailabilityStatus("AVAILABLE"); e8 = evaluatorRepository.save(e8);
        Evaluator e9 = new Evaluator(); e9.setEmpId("EMP-1199"); e9.setName("Ananya Roy"); e9.setVertical("Finance"); e9.setDomain("Corporate Banking"); e9.setAvailabilityStatus("UNAVAILABLE"); e9.setStatusReason("Left company"); e9.setIsPermanent(true); e9 = evaluatorRepository.save(e9);

        // 5. Shortlist
        addShortlist(batch1, e1, poc);
        addShortlist(batch1, e2, poc);
        addShortlist(batch1, e3, poc);
        addShortlist(batch1, e4, poc);
        addShortlist(batch1, e5, poc);
        addShortlist(batch1, e6, poc);
        addShortlist(batch1, e7, poc);
        addShortlist(batch1, e8, poc);

        // 6. Mappings
        addMapping(batch1, c1, e8, "INTERIM", 1, "CONFIRMED", poc);
        addMapping(batch1, c2, e1, "INTERIM", 1, "CONFIRMED", poc);
        addMapping(batch1, c3, e3, "INTERIM", 1, "CONFIRMED", poc);
        addMapping(batch1, c3, e7, "FINAL", 1, "CONFIRMED", poc); // Aisha Final attempt 1
        addMapping(batch1, c4, e4, "INTERIM", 1, "CONFIRMED", poc);
    }
    
    private void addShortlist(Cohort cohort, Evaluator evaluator, User poc) {
        EvaluatorShortlist s = new EvaluatorShortlist();
        s.setCohort(cohort);
        s.setEvaluator(evaluator);
        s.setAddedBy(poc);
        shortlistRepository.save(s);
    }
    
    private void addMapping(Cohort cohort, Candidate candidate, Evaluator evaluator, String round, int attempt, String status, User poc) {
        EvaluatorMapping m = new EvaluatorMapping();
        m.setCohort(cohort);
        m.setCandidate(candidate);
        m.setEvaluator(evaluator);
        m.setRound(round);
        m.setAttempt(attempt);
        m.setStatus(status);
        m.setMappedBy(poc);
        mappingRepository.save(m);
    }
}

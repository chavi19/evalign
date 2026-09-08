package com.ems.service;

import com.ems.dto.EvaluatorDTO;
import com.ems.entity.Evaluator;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EvaluatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    @Autowired
    private ExcelUploadService excelUploadService;

    public List<EvaluatorDTO> getEvaluators(String vertical, String domain, String availabilityStatus, LocalDate interviewFrom, LocalDate interviewTo) {
        if (interviewFrom != null && interviewTo != null && interviewFrom.isAfter(interviewTo)) {
            throw new IllegalArgumentException("Interview From Date cannot be after To Date");
        }

        Stream<Evaluator> stream = evaluatorRepository.findAll().stream();
        
        if (vertical != null && !vertical.isEmpty() && !"All".equalsIgnoreCase(vertical)) {
            stream = stream.filter(e -> vertical.equalsIgnoreCase(e.getVertical()));
        }
        if (domain != null && !domain.isEmpty() && !"All".equalsIgnoreCase(domain)) {
            stream = stream.filter(e -> domain.equalsIgnoreCase(e.getDomain()));
        }

        final LocalDate effInterviewFrom = (interviewFrom == null && interviewTo != null) ? interviewTo : interviewFrom;
        final LocalDate effInterviewTo = (interviewTo == null && interviewFrom != null) ? interviewFrom : interviewTo;

        if (effInterviewFrom != null && effInterviewTo != null) {
            // Apply date-range availability logic
            if ("UNAVAILABLE".equalsIgnoreCase(availabilityStatus)) {
                stream = stream.filter(e -> !isAvailableForDateRange(e, effInterviewFrom, effInterviewTo));
            } else {
                // Default when date filter is active: show only evaluators who ARE available for the interview range
                stream = stream.filter(e -> isAvailableForDateRange(e, effInterviewFrom, effInterviewTo));
            }
        } else {
            // No interview date range selected -> standard status filter
            if (availabilityStatus != null && !availabilityStatus.isEmpty() && !"All".equalsIgnoreCase(availabilityStatus)) {
                stream = stream.filter(e -> availabilityStatus.equalsIgnoreCase(e.getAvailabilityStatus()));
            }
        }
        
        return stream.map(this::mapToDTO).collect(Collectors.toList());
    }

    public boolean isAvailableForDateRange(Evaluator e, LocalDate interviewFrom, LocalDate interviewTo) {
        // If evaluator is generally available, they are available
        if ("AVAILABLE".equalsIgnoreCase(e.getAvailabilityStatus())) {
            return true;
        }

        // If evaluator is unavailable:
        // 1. If permanent status (e.g. Left company), they are never available automatically
        if (Boolean.TRUE.equals(e.getIsPermanent())) {
            return false;
        }

        // 2. If temporary unavailability with date range
        if (e.getUnavailableFrom() != null && e.getUnavailableTo() != null) {
            // Check overlap between [interviewFrom, interviewTo] and [unavailableFrom, unavailableTo]
            boolean overlaps = !interviewFrom.isAfter(e.getUnavailableTo()) && !interviewTo.isBefore(e.getUnavailableFrom());
            // If it overlaps with leave period, evaluator is NOT available. If it doesn't overlap, evaluator IS available!
            return !overlaps;
        }

        // If unavailable without specific dates, consider unavailable
        return false;
    }

    public EvaluatorDTO getEvaluatorById(Long id) {
        Evaluator evaluator = evaluatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + id));
        return mapToDTO(evaluator);
    }

    @Transactional
    public EvaluatorDTO updateAvailability(Long id, EvaluatorDTO dto) {
        Evaluator evaluator = evaluatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + id));

        if (dto.getUnavailableFrom() != null && dto.getUnavailableTo() != null && dto.getUnavailableFrom().isAfter(dto.getUnavailableTo())) {
            throw new IllegalArgumentException("Unavailable From Date cannot be after To Date");
        }

        if (dto.getAvailabilityStatus() != null) {
            evaluator.setAvailabilityStatus(dto.getAvailabilityStatus());
        }
        evaluator.setUnavailableFrom(dto.getUnavailableFrom());
        evaluator.setUnavailableTo(dto.getUnavailableTo());
        if (dto.getStatusReason() != null) {
            evaluator.setStatusReason(dto.getStatusReason());
        }
        if (dto.getIsPermanent() != null) {
            evaluator.setIsPermanent(dto.getIsPermanent());
        }
        evaluator.setUpdatedAt(LocalDateTime.now());

        Evaluator saved = evaluatorRepository.save(evaluator);
        excelUploadService.updateEvaluatorStatusInExcel(saved);
        return mapToDTO(saved);
    }

    @Transactional
    public EvaluatorDTO updateStatusReason(Long id, EvaluatorDTO dto) {
        Evaluator evaluator = evaluatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + id));

        if (dto.getUnavailableFrom() != null && dto.getUnavailableTo() != null && dto.getUnavailableFrom().isAfter(dto.getUnavailableTo())) {
            throw new IllegalArgumentException("Unavailable From Date cannot be after To Date");
        }

        evaluator.setStatusReason(dto.getStatusReason());
        evaluator.setIsPermanent(dto.getIsPermanent() != null ? dto.getIsPermanent() : false);
        evaluator.setAvailabilityStatus(dto.getAvailabilityStatus() != null ? dto.getAvailabilityStatus() : "UNAVAILABLE");

        if (Boolean.TRUE.equals(evaluator.getIsPermanent())) {
            evaluator.setUnavailableFrom(null);
            evaluator.setUnavailableTo(null);
        } else {
            evaluator.setUnavailableFrom(dto.getUnavailableFrom());
            evaluator.setUnavailableTo(dto.getUnavailableTo());
        }
        evaluator.setUpdatedAt(LocalDateTime.now());

        Evaluator saved = evaluatorRepository.save(evaluator);
        excelUploadService.updateEvaluatorStatusInExcel(saved);
        return mapToDTO(saved);
    }
    
    public List<String> getVerticals() {
        return evaluatorRepository.findDistinctVerticals();
    }
    
    public List<String> getDomains() {
        return evaluatorRepository.findDistinctDomains();
    }

    public EvaluatorDTO mapToDTO(Evaluator e) {
        EvaluatorDTO dto = new EvaluatorDTO();
        dto.setEvaluatorId(e.getEvaluatorId());
        dto.setEmpId(e.getEmpId());
        dto.setName(e.getName());
        dto.setVertical(e.getVertical());
        dto.setDomain(e.getDomain());
        dto.setAvailabilityStatus(e.getAvailabilityStatus());
        dto.setUnavailableFrom(e.getUnavailableFrom());
        dto.setUnavailableTo(e.getUnavailableTo());
        dto.setStatusReason(e.getStatusReason());
        dto.setIsPermanent(e.getIsPermanent());
        return dto;
    }
}

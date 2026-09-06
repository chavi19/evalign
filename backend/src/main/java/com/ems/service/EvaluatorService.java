package com.ems.service;

import com.ems.dto.EvaluatorDTO;
import com.ems.entity.Evaluator;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EvaluatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    public List<EvaluatorDTO> getEvaluators(String vertical, String domain, String availabilityStatus) {
        Stream<Evaluator> stream = evaluatorRepository.findAll().stream();
        
        if (vertical != null && !vertical.isEmpty() && !"All".equalsIgnoreCase(vertical)) {
            stream = stream.filter(e -> vertical.equalsIgnoreCase(e.getVertical()));
        }
        if (domain != null && !domain.isEmpty() && !"All".equalsIgnoreCase(domain)) {
            stream = stream.filter(e -> domain.equalsIgnoreCase(e.getDomain()));
        }
        if (availabilityStatus != null && !availabilityStatus.isEmpty() && !"All".equalsIgnoreCase(availabilityStatus)) {
            stream = stream.filter(e -> availabilityStatus.equalsIgnoreCase(e.getAvailabilityStatus()));
        }
        
        return stream.map(this::mapToDTO).collect(Collectors.toList());
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

        if (dto.getAvailabilityStatus() != null) {
            evaluator.setAvailabilityStatus(dto.getAvailabilityStatus());
        }
        evaluator.setUnavailableFrom(dto.getUnavailableFrom());
        evaluator.setUnavailableTo(dto.getUnavailableTo());
        return mapToDTO(evaluatorRepository.save(evaluator));
    }
    
    public List<String> getVerticals() {
        return evaluatorRepository.findDistinctVerticals();
    }
    
    public List<String> getDomains() {
        return evaluatorRepository.findDistinctDomains();
    }

    private EvaluatorDTO mapToDTO(Evaluator e) {
        EvaluatorDTO dto = new EvaluatorDTO();
        dto.setEvaluatorId(e.getEvaluatorId());
        dto.setEmpId(e.getEmpId());
        dto.setName(e.getName());
        dto.setVertical(e.getVertical());
        dto.setDomain(e.getDomain());
        dto.setAvailabilityStatus(e.getAvailabilityStatus());
        dto.setUnavailableFrom(e.getUnavailableFrom());
        dto.setUnavailableTo(e.getUnavailableTo());
        return dto;
    }
}

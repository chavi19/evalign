package com.ems.service;

import com.ems.dto.EvaluatorDTO;
import com.ems.entity.Evaluator;
import com.ems.repository.EvaluatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EvaluatorService {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    public List<EvaluatorDTO> getEvaluators(String vertical, String domain, String availabilityStatus) {
        Stream<Evaluator> stream = evaluatorRepository.findAll().stream();
        
        if (vertical != null && !vertical.isEmpty()) {
            stream = stream.filter(e -> vertical.equals(e.getVertical()));
        }
        if (domain != null && !domain.isEmpty()) {
            stream = stream.filter(e -> domain.equals(e.getDomain()));
        }
        if (availabilityStatus != null && !availabilityStatus.isEmpty()) {
            stream = stream.filter(e -> availabilityStatus.equals(e.getAvailabilityStatus()));
        }
        
        return stream.map(this::mapToDTO).collect(Collectors.toList());
    }

    public EvaluatorDTO updateAvailability(Long id, EvaluatorDTO dto) {
        return evaluatorRepository.findById(id).map(e -> {
            e.setAvailabilityStatus(dto.getAvailabilityStatus());
            e.setUnavailableFrom(dto.getUnavailableFrom());
            e.setUnavailableTo(dto.getUnavailableTo());
            return mapToDTO(evaluatorRepository.save(e));
        }).orElse(null);
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

package com.backend.jibli.company;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.review.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService implements ICompanyService {

    private final ICompanyRepository companyRepository;

    @Autowired
    public CompanyService(ICompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyDTO> getCompanyById(Integer id) {
        return companyRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public CompanyDTO createCompany(CompanyDTO dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (dto.getCompanySector() == null || dto.getCompanySector().isBlank()) {
            throw new IllegalArgumentException("Company sector is required");
        }
        if (dto.getCompanyDescription() != null && dto.getCompanyDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        Company company = mapToEntity(dto);
        Company saved = companyRepository.save(company);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public Optional<CompanyDTO> updateCompany(Integer id, CompanyDTO dto) {
        if (dto.getCompanyName() != null && dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (dto.getCompanySector() != null && dto.getCompanySector().isBlank()) {
            throw new IllegalArgumentException("Company sector cannot be empty");
        }
        if (dto.getCompanyDescription() != null && dto.getCompanyDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        return companyRepository.findById(id)
                .map(company -> {
                    if (dto.getCompanyName() != null) company.setCompanyName(dto.getCompanyName());
                    if (dto.getCompanyDescription() != null) company.setCompanyDescription(dto.getCompanyDescription());
                    if (dto.getCompanySector() != null) company.setCompanySector(dto.getCompanySector());
                    // Note: imageUrl is managed via Attachment entities, not directly updated here
                    Company updated = companyRepository.save(company);
                    return mapToDTO(updated);
                });
    }

    @Override
    public List<CompanyDTO> findByUserUserId(Integer userId) {
        return companyRepository.findByUserUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteCompany(Integer id) {
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CompanyDTO mapToDTO(Company company) {
        if (company == null) return null;

        // Extract user IDs
        List<Integer> userIds = company.getUserCompanies() != null
                ? company.getUserCompanies().stream()
                .filter(uc -> uc.getUser() != null)
                .map(uc -> uc.getUser().getUserId())
                .collect(Collectors.toList())
                : Collections.emptyList();

        // Extract image URL (first attachment's fileName, if exists)
        String imageUrl = company.getAttachments() != null && !company.getAttachments().isEmpty()
                ? company.getAttachments().get(0).getFileName()
                : null;

        // Calculate average rating
        Double averageRating = company.getReviews() != null && !company.getReviews().isEmpty()
                ? company.getReviews().stream()
                .filter(r -> r.getRating() != null)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0)
                : 0.0;

        return new CompanyDTO(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCompanyDescription(),
                company.getCompanySector(),
                company.getCreatedAt(),
                company.getLastUpdated(),
                userIds,
                imageUrl,
                averageRating,
                company.getUser() != null ? company.getUser().getUserId() : null


        );
    }

    private Company mapToEntity(CompanyDTO dto) {
        if (dto == null) return null;

        Company company = new Company();
        company.setCompanyName(dto.getCompanyName());
        company.setCompanyDescription(dto.getCompanyDescription());
        company.setCompanySector(dto.getCompanySector());
        return company;
    }
}
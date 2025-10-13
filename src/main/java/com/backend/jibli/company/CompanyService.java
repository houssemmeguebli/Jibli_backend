package com.backend.jibli.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CompanyDTO> getCompanyById(Integer id) {
        return companyRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public CompanyDTO createCompany(CompanyDTO dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (dto.getCompanyDescription() != null && dto.getCompanyDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        Company company = mapToEntity(dto);
        Company saved = companyRepository.save(company);
        return mapToDTO(saved);
    }

    @Override
    public Optional<CompanyDTO> updateCompany(Integer id, CompanyDTO dto) {
        if (dto.getCompanyName() != null && dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (dto.getCompanyDescription() != null && dto.getCompanyDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        return companyRepository.findById(id)
                .map(company -> {
                    if (dto.getCompanyName() != null) company.setName(dto.getCompanyName());
                    if (dto.getCompanyDescription() != null) company.setDescription(dto.getCompanyDescription());
                    Company updated = companyRepository.save(company);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteCompany(Integer id) {
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CompanyDTO mapToDTO(Company company) {
        List<Integer> userCompanyIds = company.getUserCompanies() != null
                ? company.getUserCompanies().stream()
                .map(UserCompany::getUserCompanyId)
                .collect(Collectors.toList())
                : List.of();
        return new CompanyDTO(
                company.getCompanyId(),
                company.getName(),
                company.getDescription(),
                company.getCreatedAt(),
                userCompanyIds
        );
    }

    private Company mapToEntity(CompanyDTO dto) {
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setDescription(dto.getCompanyDescription());
        return company;
    }
}
package com.backend.jibli.company;

import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserCompanyService implements IUserCompanyService {

    private final IUserCompanyRepository userCompanyRepository;
    private final IUserRepository userRepository;
    private final ICompanyRepository companyRepository;

    @Autowired
    public UserCompanyService(IUserCompanyRepository userCompanyRepository, IUserRepository userRepository, ICompanyRepository companyRepository) {
        this.userCompanyRepository = userCompanyRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public List<UserCompanyDTO> getAllUserCompanies() {
        return userCompanyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserCompanyDTO> getUserCompanyById(Integer id) {
        return userCompanyRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public UserCompanyDTO createUserCompany(UserCompanyDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        if (!userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }
        if (!companyRepository.existsById(dto.getCompanyId())) {
            throw new IllegalArgumentException("Company not found");
        }
        if (dto.getRole() == null || dto.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        if (!List.of("ADMIN", "EMPLOYEE", "MANAGER").contains(dto.getRole())) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, EMPLOYEE, or MANAGER");
        }
        if (userCompanyRepository.existsByUserUserIdAndCompanyCompanyId(dto.getUserId(), dto.getCompanyId())) {
            throw new IllegalArgumentException("User is already associated with this company");
        }
        UserCompany userCompany = mapToEntity(dto);
        UserCompany saved = userCompanyRepository.save(userCompany);
        return mapToDTO(saved);
    }

    @Override
    public Optional<UserCompanyDTO> updateUserCompany(Integer id, UserCompanyDTO dto) {
        if (dto.getUserId() != null && !userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }
        if (dto.getCompanyId() != null && !companyRepository.existsById(dto.getCompanyId())) {
            throw new IllegalArgumentException("Company not found");
        }
        if (dto.getRole() != null && dto.getRole().isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        if (dto.getRole() != null && !List.of("ADMIN", "EMPLOYEE", "MANAGER").contains(dto.getRole())) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, EMPLOYEE, or MANAGER");
        }
        return userCompanyRepository.findById(id)
                .map(userCompany -> {
                    if (dto.getUserId() != null) {
                        User user = new User();
                        user.setUserId(dto.getUserId());
                        userCompany.setUser(user);
                    }
                    if (dto.getCompanyId() != null) {
                        Company company = new Company();
                        company.setCompanyId(dto.getCompanyId());
                        userCompany.setCompany(company);
                    }
                    if (dto.getRole() != null) userCompany.setRole(dto.getRole());
                    UserCompany updated = userCompanyRepository.save(userCompany);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteUserCompany(Integer id) {
        if (userCompanyRepository.existsById(id)) {
            userCompanyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<UserCompanyDTO> getUserCompaniesByUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        return userCompanyRepository.findByUserUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserCompanyDTO> getUserCompaniesByCompany(Integer companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Company not found");
        }
        return userCompanyRepository.findByCompanyCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserCompanyDTO mapToDTO(UserCompany userCompany) {
        return new UserCompanyDTO(
                userCompany.getUserCompanyId(),
                userCompany.getUser() != null ? userCompany.getUser().getUserId() : null,
                userCompany.getCompany() != null ? userCompany.getCompany().getCompanyId() : null,
                userCompany.getRole()
        );
    }

    private UserCompany mapToEntity(UserCompanyDTO dto) {
        UserCompany userCompany = new UserCompany();
        if (dto.getUserId() != null) {
            User user = new User();
            user.setUserId(dto.getUserId());
            userCompany.setUser(user);
        }
        if (dto.getCompanyId() != null) {
            Company company = new Company();
            company.setCompanyId(dto.getCompanyId());
            userCompany.setCompany(company);
        }
        userCompany.setRole(dto.getRole());
        return userCompany;
    }
}
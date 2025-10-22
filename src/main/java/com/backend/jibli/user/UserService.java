package com.backend.jibli.user;

import com.backend.jibli.company.IUserCompanyService;
import com.backend.jibli.company.UserCompany;
import com.backend.jibli.company.UserCompanyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IUserCompanyService userCompanyService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(IUserRepository userRepository, IUserCompanyService userCompanyService) {
        this.userRepository = userRepository;
        this.userCompanyService = userCompanyService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDTO> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::mapToDTO);
    }
    @Override
    public List<UserDTO> findAllByUserRole(UserRole userRole) {
        return userRepository.findAllByUserRole(userRole).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public Optional<UserDTO> updateUser(Integer id, UserDTO dto) {
        if (dto.getFullName() != null && dto.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        if (dto.getEmail() != null && !dto.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (dto.getPassword() != null && dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (dto.getUserRole() != null && !isValidRole(dto.getUserRole())) {
            throw new IllegalArgumentException("Role must be USER or ADMIN");
        }
        if (dto.getUserStatus() != null && !isValidStatus(dto.getUserStatus())) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        return userRepository.findById(id)
                .map(user -> {
                    if (dto.getFullName() != null) {
                        if (userRepository.existsByFullName(dto.getFullName()) && !user.getFullName().equals(dto.getFullName())) {
                            throw new IllegalArgumentException("Full name already exists");
                        }
                        user.setFullName(dto.getFullName());
                    }
                    if (dto.getEmail() != null) {
                        if (userRepository.existsByEmail(dto.getEmail()) && !user.getEmail().equals(dto.getEmail())) {
                            throw new IllegalArgumentException("Email already exists");
                        }
                        user.setEmail(dto.getEmail());
                    }
                    if (dto.getPassword() != null) {
                        user.setPassword(passwordEncoder.encode(dto.getPassword()));
                    }
                    if (dto.getPhone() != null) user.setPhone(dto.getPhone());
                    if (dto.getAddress() != null) user.setAddress(dto.getAddress());
                    if (dto.getGender() != null) user.setGender(dto.getGender());
                    if (dto.getDateOfBirth() != null) user.setDateOfBirth(dto.getDateOfBirth());

                    user.setAvailable(dto.isAvailable());

                    if (dto.getUserRole() != null) user.setUserRole(UserRole.valueOf(dto.getUserRole()));
                    if (dto.getUserStatus() != null) user.setUserStatus(UserStatus.valueOf(dto.getUserStatus()));
                    user.setLastUpdated(LocalDateTime.now());
                    User updated = userRepository.save(user);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<UserCompanyDTO> getUserCompanies(Integer userId) {
        return userCompanyService.getUserCompaniesByUser(userId);
    }

    private UserDTO mapToDTO(User user) {
        List<Integer> userCompanyIds = user.getUserCompanies() != null
                ? user.getUserCompanies().stream()
                .map(UserCompany::getUserCompanyId)
                .collect(Collectors.toList())
                : List.of();
        return new UserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getGender(),
                user.getDateOfBirth(),
                null, // Do not expose password
                user.getUserRole() != null ? user.getUserRole().name() : null,
                user.getUserStatus() != null ? user.getUserStatus().name() : null,
                user.getCreatedAt(),
                user.getLastUpdated(),
                userCompanyIds,
                user.isAvailable()
        );
    }

    private User mapToEntity(UserDTO dto) {
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setAvailable(dto.isAvailable());
        user.setUserRole(dto.getUserRole() != null ? UserRole.valueOf(dto.getUserRole()) : null);
        user.setUserStatus(dto.getUserStatus() != null ? UserStatus.valueOf(dto.getUserStatus()) : null);
        return user;
    }

    private boolean isValidRole(String role) {
        return role != null && (role.equals("USER") || role.equals("ADMIN"));
    }

    private boolean isValidStatus(String status) {
        return status != null && (status.equals("ACTIVE") || status.equals("INACTIVE"));
    }
}
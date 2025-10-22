package com.backend.jibli.user;


import com.backend.jibli.company.UserCompanyDTO;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(Integer id);
    Optional<UserDTO> updateUser(Integer id, UserDTO dto);
    boolean deleteUser(Integer id);
    List<UserCompanyDTO> getUserCompanies(Integer userId);
    List<UserDTO> findAllByUserRole(UserRole userRole);


}
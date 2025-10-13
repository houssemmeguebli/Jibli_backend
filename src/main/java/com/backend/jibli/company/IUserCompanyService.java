package com.backend.jibli.company;
import java.util.List;
import java.util.Optional;

public interface IUserCompanyService {
    List<UserCompanyDTO> getAllUserCompanies();
    Optional<UserCompanyDTO> getUserCompanyById(Integer id);
    UserCompanyDTO createUserCompany(UserCompanyDTO dto);
    Optional<UserCompanyDTO> updateUserCompany(Integer id, UserCompanyDTO dto);
    boolean deleteUserCompany(Integer id);
    List<UserCompanyDTO> getUserCompaniesByUser(Integer userId);
    List<UserCompanyDTO> getUserCompaniesByCompany(Integer companyId);
}
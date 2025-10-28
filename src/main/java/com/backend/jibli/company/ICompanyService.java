package com.backend.jibli.company;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ICompanyService {
    List<CompanyDTO> getAllCompanies();
    Optional<CompanyDTO> getCompanyById(Integer id);
    CompanyDTO createCompany(CompanyDTO dto);
    Optional<CompanyDTO> updateCompany(Integer id, CompanyDTO dto);
    List<CompanyDTO> findByUserUserId(Integer userId);

    boolean deleteCompany(Integer id);
    List<CompanyDTO> findByUserUserIdWithProducts(@Param("userId") Integer userId);
    CompanyDTO findByCompanyIdWithProducts(Integer companyId);
    CompanyDTO findByCompanyIdWithReviews( Integer companyId);
    CompanyDTO findByCompanyIdWithCategories(Integer companyId);



}
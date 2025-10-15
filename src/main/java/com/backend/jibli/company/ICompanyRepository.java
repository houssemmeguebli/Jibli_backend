package com.backend.jibli.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICompanyRepository extends JpaRepository<Company,Integer> {

    List<Company> findByUserUserId(Integer userId);
}

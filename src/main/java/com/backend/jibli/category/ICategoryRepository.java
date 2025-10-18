package com.backend.jibli.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findCategoriesByUserUserId(Integer  userId);
}
package com.backend.jibli.company;

import com.backend.jibli.attachment.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final ICompanyService companyService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public CompanyController(ICompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
        List<CompanyDTO> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }
    @GetMapping("/activeCompany")
    public ResponseEntity<List<CompanyDTO>> findAllActiveCompanies() {
        List<CompanyDTO> companies = companyService.findAllActiveCompanies();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Integer id) {
        return companyService.getCompanyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CompanyDTO>>findByUserUserId(@PathVariable Integer userId) {
        List<CompanyDTO> companies = companyService.findByUserUserId(userId);
        return ResponseEntity.ok(companies);

    }
    @GetMapping("/{companyId}/products")
    public ResponseEntity<CompanyDTO> findByCompanyIdWithProducts(@PathVariable Integer companyId) {
        CompanyDTO company = companyService.findByCompanyIdWithProducts(companyId);
        return ResponseEntity.ok(company);
    }
    @GetMapping("/{companyId}/reviews")
    public ResponseEntity<CompanyDTO> findByCompanyIdWithReviews(@PathVariable Integer companyId) {
        CompanyDTO company = companyService.findByCompanyIdWithReviews(companyId);
        return ResponseEntity.ok(company);
    }
    @GetMapping("/{companyId}/categories")
    public ResponseEntity<CompanyDTO> findByCompanyIdWithCategories(@PathVariable Integer companyId) {
        CompanyDTO company = companyService.findByCompanyIdWithCategories(companyId);
        return ResponseEntity.ok(company);
    }



    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody CompanyDTO dto) {
        try {
            CompanyDTO created = companyService.createCompany(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable Integer id, @RequestBody CompanyDTO dto) {
        try {
            return companyService.updateCompany(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Integer id) {
        boolean deleted = companyService.deleteCompany(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }



}
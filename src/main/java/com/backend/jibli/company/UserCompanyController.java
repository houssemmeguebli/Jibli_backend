package com.backend.jibli.company;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-companies")
public class UserCompanyController {

    private final IUserCompanyService userCompanyService;

    @Autowired
    public UserCompanyController(IUserCompanyService userCompanyService) {
        this.userCompanyService = userCompanyService;
    }

    @GetMapping
    public ResponseEntity<List<UserCompanyDTO>> getAllUserCompanies() {
        List<UserCompanyDTO> userCompanies = userCompanyService.getAllUserCompanies();
        return ResponseEntity.ok(userCompanies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserCompanyDTO> getUserCompanyById(@PathVariable Integer id) {
        return userCompanyService.getUserCompanyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserCompanyDTO> createUserCompany(@RequestBody UserCompanyDTO dto) {
        try {
            UserCompanyDTO created = userCompanyService.createUserCompany(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserCompanyDTO> updateUserCompany(@PathVariable Integer id, @RequestBody UserCompanyDTO dto) {
        try {
            return userCompanyService.updateUserCompany(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserCompany(@PathVariable Integer id) {
        boolean deleted = userCompanyService.deleteUserCompany(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCompanyDTO>> getUserCompaniesByUser(@PathVariable Integer userId) {
        try {
            List<UserCompanyDTO> userCompanies = userCompanyService.getUserCompaniesByUser(userId);
            return ResponseEntity.ok(userCompanies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserCompanyDTO>> getUserCompaniesByCompany(@PathVariable Integer companyId) {
        try {
            List<UserCompanyDTO> userCompanies = userCompanyService.getUserCompaniesByCompany(companyId);
            return ResponseEntity.ok(userCompanies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
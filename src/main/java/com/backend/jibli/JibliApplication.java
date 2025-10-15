package com.backend.jibli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class JibliApplication {

    public static void main(String[] args) {
        SpringApplication.run(JibliApplication.class, args);
    }

}

package com.backend.jibli.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI jibliOpenAPI() {
        return new OpenAPI()
                .info(infoAPI());
    }

    public Info infoAPI() {
        return new Info()
                .title("Jibli Backend API")
                .description("API for managing the Jibli platform")
                .contact(contactAPI());
    }

    public Contact contactAPI() {
        return new Contact()
                .name("Equipe Jibli")
                .email("houssem.meguebli@esprit.tn")
                .url("https://www.linkedin.com/in/houssem-meguebli-65a98b166/");
    }

    @Bean
    public GroupedOpenApi userPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only User Management API")
                .pathsToMatch("/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi companyPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Company Management API")
                .pathsToMatch("/companies/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userCompanyPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only UserCompany Management API")
                .pathsToMatch("/user-companies/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Order Management API")
                .pathsToMatch("/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Product Management API")
                .pathsToMatch("/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi categoryPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Category Management API")
                .pathsToMatch("/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Cart Management API")
                .pathsToMatch("/carts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartItemPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only CartItem Management API")
                .pathsToMatch("/cart-items/**")
                .build();
    }

    @Bean
    public GroupedOpenApi attachmentPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Attachment Management API")
                .pathsToMatch("/attachments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only Review Management API")
                .pathsToMatch("/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderItemPublicApi() {
        return GroupedOpenApi.builder()
                .group("Only OrderItem Management API")
                .pathsToMatch("/order-items/**")
                .build();
    }
}
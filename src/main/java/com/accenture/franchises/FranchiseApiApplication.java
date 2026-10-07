package com.accenture.franchises;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "API de Franquicias",
        description = "Gestión de franquicias, sucursales, productos e inventario de stock.",
        version = "v1"))
public class FranchiseApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FranchiseApiApplication.class, args);
    }
}

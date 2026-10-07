package com.foodcourt.plaza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PlazaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlazaServiceApplication.class, args);
    }
}

package br.com.brisacidada.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BrisaCidadaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrisaCidadaApiApplication.class, args);
    }
}

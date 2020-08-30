package com.toomuch2learn.reactive.crud.catalogue;

import com.toomuch2learn.reactive.crud.catalogue.configuration.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    FileStorageProperties.class
})
public class SpringReactiveCrudCatalogueApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringReactiveCrudCatalogueApplication.class, args);
    }
}

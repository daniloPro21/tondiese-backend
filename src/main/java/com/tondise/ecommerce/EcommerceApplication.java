package com.tondise.ecommerce;

import com.tondise.utils.security.keycloak_manager.KeycloakManagerAutoConfiguration;
import com.tondise.utils.storage.s3.MinioS3AutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * {@code com.tondise.utils.config} est scanné en plus du package applicatif
 * pour activer {@code GlobalExceptionHandler} (tondise-util) sans le
 * dupliquer localement. {@code OAuth2SecurityAutoConfiguration} (tondise-util)
 * n'est volontairement pas importé ici : {@link com.tondise.ecommerce.config.security.SecurityConfig}
 * configure directement le resource server avec un convertisseur de rôles
 * realm Keycloak — voir le commentaire de cette classe pour le détail.
 */
@ComponentScan(basePackages = {"com.tondise.ecommerce", "com.tondise.utils.config"})
@Import({
        KeycloakManagerAutoConfiguration.class,
        MinioS3AutoConfiguration.class
})
@SpringBootApplication
@ConfigurationPropertiesScan
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}

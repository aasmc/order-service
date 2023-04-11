package ru.aasmc.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import java.net.URI;

@ConfigurationProperties(prefix = "polar")
public record ClientProperties(
        @NotNull
        URI catalogServiceUri
) {

}
